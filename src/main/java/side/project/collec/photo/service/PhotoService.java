package side.project.collec.photo.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import side.project.collec.album.Album;
import side.project.collec.album.AlbumRepository;
import side.project.collec.global.exception.codes.ErrorCode;
import side.project.collec.global.exception.customException.AlbumNotFoundException;
import side.project.collec.global.exception.customException.PhotoNotFoundException;
import side.project.collec.photo.domain.Photo;
import side.project.collec.photo.domain.dto.req.PhotoRequestDto;
import side.project.collec.photo.domain.dto.res.AiResponseDto;
import side.project.collec.photo.domain.dto.res.PhotoResponseDto;
import side.project.collec.photo.repository.PhotoRepository;
import side.project.collec.tag.Tag;
import side.project.collec.tag.TagRepository;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final AlbumRepository albumRepository;
    private final TagRepository tagRepository;
    private final RestTemplate restTemplate;
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String BUCKET_NAME;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    public void savePhoto(PhotoRequestDto requestDto, MultipartFile image) throws IOException {
        Album album = getAlbum(requestDto.getAlbumId());
        String photoUrl = uploadPhoto(image);

        List<String> tags = getCategoryFromAI(photoUrl);

        Photo savedPhoto = savePhotoEntity(requestDto, photoUrl, album);
        saveTags(savedPhoto, tags, album);
    }

    private Album getAlbum(Long albumId) {
        return albumRepository.findById(albumId)
                .orElseThrow(() -> new AlbumNotFoundException(ErrorCode.ALBUM_NOT_FOUND));
    }

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

    private void saveTags(Photo savedPhoto, List<String> tags, Album album) {
        List<Tag> tagEntities = tags.stream()
                .filter(tagName -> tagName != null && !tagName.isEmpty())
                .map(tagName -> new Tag(null, tagName, savedPhoto, album))
                .collect(Collectors.toList());

        if (!tagEntities.isEmpty()) {
            tagRepository.saveAll(tagEntities);
        }
    }

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

    public String uploadPhoto(MultipartFile image) throws IOException {
        String fileName = generateUniqueFileName(image.getOriginalFilename());

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(image.getSize());
        metadata.setContentType(image.getContentType());

        amazonS3.putObject(new PutObjectRequest(BUCKET_NAME, fileName, image.getInputStream(), metadata));

        return amazonS3.getUrl(BUCKET_NAME, fileName).toString();
    }


    private String generateUniqueFileName(String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return UUID.randomUUID().toString() + extension;
    }

    public PhotoResponseDto photoDetail(Long id) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(PhotoNotFoundException::new);

        return PhotoResponseDto.builder()
                .id(photo.getId())
                .photoFilepath(photo.getPhotoFilepath())
                .build();
    }

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
