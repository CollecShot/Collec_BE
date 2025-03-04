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
import side.project.collec.photo.domain.Photo;
import side.project.collec.photo.domain.dto.req.PhotoRequestDto;
import side.project.collec.photo.repository.PhotoRepository;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final AlbumRepository albumRepository;

    @Value("${google.cloud.bucket-name}")
    private String BUCKET_NAME;

    public void savePhoto(PhotoRequestDto requestDto, MultipartFile image) throws IOException {
        Album album = albumRepository.findById(requestDto.getAlbumId())
                .orElseThrow(() -> new IllegalArgumentException("Album not found"));

        String photoUrl = uploadPhoto(image);

        Photo photo = Photo.builder()
                .photoFilepath(requestDto.getPhotoFilepath())
                .photoDatetime(requestDto.getPhotoDatetime())
                .photoUrl(photoUrl)
                .album(album)
                .build();

        photoRepository.save(photo);
    }

    public String uploadPhoto(MultipartFile image) throws IOException {
        ClassPathResource resource = new ClassPathResource("collec-gcp-key.json");
        InputStream credentialsStream = resource.getInputStream();
        GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);

        Storage storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();

        String fileName = image.getOriginalFilename();
        BlobId blobId = BlobId.of(BUCKET_NAME, fileName);

        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(image.getContentType())
                .build();

        storage.create(blobInfo, image.getInputStream());

        return String.format("https://storage.googleapis.com/%s/%s", BUCKET_NAME, fileName);
    }
}
