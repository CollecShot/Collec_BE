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
import org.springframework.web.client.RestTemplate;
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
    private final RestTemplate restTemplate;

    @Value("${google.cloud.bucket-name}")
    private String BUCKET_NAME;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    public void savePhoto(PhotoRequestDto requestDto, MultipartFile image) throws IOException {
        Album album = albumRepository.findById(requestDto.getAlbumId())
                .orElseThrow(() -> new AlbumNotFoundException(ErrorCode.ALBUM_NOT_FOUND));

        String photoUrl = uploadPhoto(image);

        // AI 서버로 photoUrl 전송해 카테고리 받기
        String tags = getCategoryFromAI(photoUrl);

        Photo photo = Photo.builder()
                .photoFilepath(requestDto.getPhotoFilepath())
                .photoDatetime(requestDto.getPhotoDatetime())
                .photoUrl(photoUrl)
                .tags(tags)
                .album(album)
                .build();

        photoRepository.save(photo);
    }

    private String getCategoryFromAI(String photoUrl) {
        return restTemplate.postForObject(aiServerUrl, photoUrl, String.class);
    }

    public String uploadPhoto(MultipartFile image) throws IOException {
        GoogleCredentials credentials = loadGoogleCredentials();
        Storage storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();

        String fileName = generateUniqueFileName(image.getOriginalFilename());
        BlobId blobId = BlobId.of(BUCKET_NAME, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(image.getContentType())
                .build();

        storage.create(blobInfo, image.getInputStream());
        return String.format("https://storage.googleapis.com/%s/%s", BUCKET_NAME, fileName);
    }

    private GoogleCredentials loadGoogleCredentials() throws IOException {
        ClassPathResource resource = new ClassPathResource("collec-gcp-key.json");
        InputStream credentialsStream = resource.getInputStream();
        return GoogleCredentials.fromStream(credentialsStream);
    }

    private String generateUniqueFileName(String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueId = UUID.randomUUID().toString();
        return uniqueId + extension;
    }
}
