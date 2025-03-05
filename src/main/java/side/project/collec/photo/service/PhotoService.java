package side.project.collec.photo.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import side.project.collec.album.Album;
import side.project.collec.album.AlbumRepository;
import side.project.collec.global.exception.customException.AlbumNotFoundException;
import side.project.collec.photo.domain.Photo;
import side.project.collec.photo.domain.dto.req.PhotoRequestDto;
import side.project.collec.photo.repository.PhotoRepository;
import side.project.collec.global.exception.codes.ErrorCode;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final AlbumRepository albumRepository;

    @Value("${google.cloud.bucket-name}")
    private String BUCKET_NAME;

    public void savePhoto(PhotoRequestDto requestDto, MultipartFile image) throws IOException {
        // 앨범 찾기
        Album album = albumRepository.findById(requestDto.getAlbumId())
                .orElseThrow(() -> new AlbumNotFoundException(ErrorCode.ALBUM_NOT_FOUND));

        // 사진 업로드 후 URL 반환
        String photoUrl = uploadPhoto(image);

        // Photo 객체 생성 후 DB 저장
        Photo photo = Photo.builder()
                .photoFilepath(requestDto.getPhotoFilepath())
                .photoDatetime(requestDto.getPhotoDatetime())
                .photoUrl(photoUrl)
                .album(album)
                .build();

        photoRepository.save(photo);
    }

    /**
     * Google Cloud Storage에 사진을 업로드하는 메소드
     */
    public String uploadPhoto(MultipartFile image) throws IOException {
        // GCP 인증 정보 로드
        GoogleCredentials credentials = loadGoogleCredentials();

        // Google Cloud Storage 서비스 객체 생성
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();

        // 파일 이름 중복 처리 (UUID 사용)
        String fileName = generateUniqueFileName(image.getOriginalFilename());

        // BlobId 및 BlobInfo 설정
        BlobId blobId = BlobId.of(BUCKET_NAME, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(image.getContentType())
                .build();

        // 파일 업로드
        storage.create(blobInfo, image.getInputStream());

        // 파일 URL 반환
        return String.format("https://storage.googleapis.com/%s/%s", BUCKET_NAME, fileName);
    }

    /**
     * Google Cloud 인증 정보를 로드하는 메소드
     */
    private GoogleCredentials loadGoogleCredentials() throws IOException {
        ClassPathResource resource = new ClassPathResource("collec-gcp-key.json");
        InputStream credentialsStream = resource.getInputStream();
        return GoogleCredentials.fromStream(credentialsStream);
    }

    /**
     * 파일 이름 중복 방지를 위한 고유한 파일명 생성
     */
    private String generateUniqueFileName(String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueId = UUID.randomUUID().toString();
        return uniqueId + extension; // UUID로 파일 이름 생성
    }
}
