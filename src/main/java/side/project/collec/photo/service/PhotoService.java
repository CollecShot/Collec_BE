package side.project.collec.photo.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import side.project.collec.album.domain.Album;
import side.project.collec.album.AlbumRepository;
import side.project.collec.global.exception.codes.ErrorCode;
import side.project.collec.global.exception.customException.AlbumNotFoundException;
import side.project.collec.global.exception.customException.PhotoNotFoundException;
import side.project.collec.photo.domain.Photo;
import side.project.collec.photo.domain.dto.req.PhotoRequestDto;
import side.project.collec.photo.domain.dto.res.PhotoResponseDto;
import side.project.collec.photo.repository.PhotoRepository;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final AlbumRepository albumRepository;
    private final RestTemplate restTemplate;
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String BUCKET_NAME;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Transactional
    public void savePhoto(PhotoRequestDto requestDto, MultipartFile image) throws IOException {
        String photoUrl = uploadPhoto(image);

        Map<String, Object> aiResponse = getCategoryAndTagsFromAI(photoUrl);

        String category = (String) aiResponse.get("category");
        List<String> tags = (List<String>) aiResponse.get("tags");


        Album categoryAlbum = albumRepository.findByAlbumNameAndUserAlbum_User_DeviceUID(category, requestDto.getDeviceUID())
                .orElseThrow(() -> new AlbumNotFoundException(ErrorCode.ALBUM_NOT_FOUND));

        Photo savedPhoto = savePhotoEntity(requestDto, photoUrl, category, categoryAlbum);

        saveTags(savedPhoto, tags);

    }

    private Map<String, Object> getCategoryAndTagsFromAI(String photoUrl) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("url", photoUrl);

        ResponseEntity<Map> response = restTemplate.postForEntity(aiServerUrl, requestBody, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new RuntimeException("AI 서버 응답 오류: " + response.getStatusCode());
        }
    }

    private Photo savePhotoEntity(PhotoRequestDto requestDto, String photoUrl, String category, Album album) {
        Photo savedPhoto = photoRepository.save(Photo.builder()
                .photoFilepath(requestDto.getPhotoFilepath())
                .photoDatetime(requestDto.getPhotoDatetime())
                .photoUrl(photoUrl)
                .category(category)
                .album(album)
                .build());

        if (savedPhoto.getId() == null) {
            throw new RuntimeException("Photo 저장 실패! ID가 생성되지 않음.");
        }
        return savedPhoto;
    }

    private void saveTags(Photo savedPhoto, List<String> tags) {
        if (tags != null && !tags.isEmpty()) {
            String tagString = String.join(",", tags);
            savedPhoto.setTags(tagString);
            photoRepository.save(savedPhoto);
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
