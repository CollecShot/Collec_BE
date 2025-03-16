package side.project.collec.photo.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;
import side.project.collec.album.Album;
import side.project.collec.album.AlbumRepository;
import side.project.collec.global.exception.customException.AlbumNotFoundException;
import side.project.collec.global.exception.customException.PhotoNotFoundException;

import side.project.collec.photo.domain.Photo;
import side.project.collec.photo.domain.dto.req.PhotoRequestDto;
import side.project.collec.photo.domain.dto.res.AiResponseDto;
import side.project.collec.photo.domain.dto.res.PhotoResponseDto;
import side.project.collec.photo.repository.PhotoRepository;
import side.project.collec.global.exception.codes.ErrorCode;
import side.project.collec.tag.Tag;
import side.project.collec.tag.TagRepository;
import side.project.collec.userAlbum.UserAlbumRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final AlbumRepository albumRepository;
    private final TagRepository tagRepository;
    private final RestTemplate restTemplate;

    @Value("${google.cloud.bucket-name}")
    private String BUCKET_NAME;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    // 사진 저장 함수
    public void savePhoto(PhotoRequestDto requestDto, MultipartFile image) throws IOException {
        Album album = getAlbum(requestDto.getAlbumId());
        String photoUrl = uploadPhoto(image);

        // AI 태그 분석
        List<String> tags = getCategoryFromAI(photoUrl);

        Photo savedPhoto = savePhotoEntity(requestDto, photoUrl, album);
        saveTags(savedPhoto, tags, album);
    }

    // 앨범 ID로 앨범을 찾는 함수
    private Album getAlbum(Long albumId) {
        return albumRepository.findById(albumId)
                .orElseThrow(() -> new AlbumNotFoundException(ErrorCode.ALBUM_NOT_FOUND));
    }

    // 사진 엔티티 저장 함수
    private Photo savePhotoEntity(PhotoRequestDto requestDto, String photoUrl, Album album) {
        Photo savedPhoto = photoRepository.save(Photo.builder()
                .photoFilepath(requestDto.getPhotoFilepath())
                .photoDatetime(requestDto.getPhotoDatetime())
                .photoUrl(photoUrl)
                .album(album)
                .build());

        if (savedPhoto.getId() == null) {
            throw new RuntimeException("Photo 저장 실패! ID가 생성되지 않음.");
        }
        return savedPhoto;
    }

    // 태그 저장 함수
    private void saveTags(Photo savedPhoto, List<String> tags, Album album) {
        List<Tag> tagEntities = tags.stream()
                .filter(tagName -> tagName != null && !tagName.isEmpty())
                .map(tagName -> new Tag(null, tagName, savedPhoto, album))
                .collect(Collectors.toList());

        if (!tagEntities.isEmpty()) {
            tagRepository.saveAll(tagEntities);
        } else {
            System.out.println("저장할 태그가 없습니다.");
        }
    }

    // AI 서버에서 태그 분석 결과 받아오기
    private List<String> getCategoryFromAI(String photoUrl) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("url", photoUrl);

        ResponseEntity<AiResponseDto> response = restTemplate.postForEntity(
                aiServerUrl, requestBody, AiResponseDto.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody().getTags();
        } else {
            throw new RuntimeException("AI 서버 응답 오류: " + response.getStatusCode());
        }
    }

    // 사진 업로드 함수 (Google Cloud Storage)
    public String uploadPhoto(MultipartFile image) throws IOException {
        Storage storage = getGoogleStorage();
        String fileName = generateUniqueFileName(image.getOriginalFilename());
        BlobId blobId = BlobId.of(BUCKET_NAME, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(image.getContentType())
                .build();

        storage.create(blobInfo, image.getInputStream());
        return String.format("https://storage.googleapis.com/%s/%s", BUCKET_NAME, fileName);
    }

    // Google Cloud Storage 객체 생성 함수
    private Storage getGoogleStorage() throws IOException {
        GoogleCredentials credentials = loadGoogleCredentials();
        return StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
    }

    // GCP Credentials 로드 함수
    private GoogleCredentials loadGoogleCredentials() throws IOException {
        ClassPathResource resource = new ClassPathResource("collec-gcp-key.json");
        InputStream credentialsStream = resource.getInputStream();
        return GoogleCredentials.fromStream(credentialsStream);
    }

    // 고유한 파일명 생성 함수
    private String generateUniqueFileName(String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return UUID.randomUUID().toString() + extension;
    }

    // 사진 상세 조회 함수
    public PhotoResponseDto photoDetail(Long id) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(PhotoNotFoundException::new);

        return PhotoResponseDto.builder()
                .id(photo.getId())
                .photoFilepath(photo.getPhotoFilepath())
                .build();
    }

    // 앨범별 사진 조회 함수
    public List<PhotoResponseDto> getPhotosByAlbumId(Long albumId) {
        List<Photo> photos = photoRepository.findByAlbumId(albumId);
        return photos.stream()
                .map(photo -> PhotoResponseDto.builder()
                        .id(photo.getId())
                        .photoFilepath(photo.getPhotoFilepath())
                        .build())
                .collect(Collectors.toList());
    }
}
