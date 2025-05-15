package side.project.collec.photo.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import side.project.collec.album.domain.Album;
import side.project.collec.album.repository.AlbumRepository;
//import side.project.collec.category.domain.Category;
//import side.project.collec.category.repository.CategoryRepository;
import side.project.collec.global.exception.GlobalException;
import side.project.collec.global.exception.codes.ErrorCode;
import side.project.collec.global.exception.customException.AiModelException;
import side.project.collec.global.exception.customException.AlbumNotFoundException;
import side.project.collec.global.exception.customException.PhotoNotFoundException;
import side.project.collec.global.exception.customException.UserNotFoundException;
import side.project.collec.photo.domain.Photo;
import side.project.collec.photo.domain.dto.req.PhotoClassifyRequestDto;
import side.project.collec.photo.domain.dto.req.PhotoRequestDto;
import side.project.collec.photo.domain.dto.req.PhotoTrashRequestDto;
import side.project.collec.photo.domain.dto.res.PhotoResponseDto;
import side.project.collec.photo.repository.PhotoRepository;
import side.project.collec.photoTag.domain.PhotoTag;
import side.project.collec.photoTag.repository.PhotoTagRepository;
import side.project.collec.tag.domain.Tag;
import side.project.collec.tag.repository.TagRepository;
import side.project.collec.user.domain.User;
import side.project.collec.user.repository.UserRepository;
import side.project.collec.userAlbum.domain.UserAlbum;
import side.project.collec.userAlbum.repository.UserAlbumRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;
    private final UserAlbumRepository userAlbumRepository;
    private final AlbumRepository albumRepository;
    private final TagRepository tagRepository;
    private final PhotoTagRepository photoTagRepository;
    private final RestTemplate restTemplate;
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String BUCKET_NAME;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    @Transactional
    public Photo uploadPhotoExtractInfo(PhotoRequestDto requestDto, MultipartFile image) throws IOException {
        String photoUrl = uploadPhoto(image);

        // 사용자 조회
        User user = userRepository.findByDeviceUID(requestDto.getDeviceUID())
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));

        // 해당 사용자의 UserAlbum 조회 또는 생성
        UserAlbum userAlbum = userAlbumRepository.findByUser(user)
                .orElseGet(() -> userAlbumRepository.save(UserAlbum.builder()
                        .user(user)
                        .build()));

        // albumId로 Album 조회
        Album album = albumRepository.findById(userAlbum.getAlbums().get(0).getId())
                .orElseThrow(() -> new AlbumNotFoundException(ErrorCode.ALBUM_NOT_FOUND));

        // AI 응답으로부터 caption, tags 추출
        Map<String, Object> aiResponse = getInfoFromAI(photoUrl);
//        String caption = (String) aiResponse.get("caption");

        @SuppressWarnings("unchecked")
        List<String> captionList = (List<String>) aiResponse.get("caption");

        String caption = "";
        if (captionList != null && !captionList.isEmpty()) {
            caption = captionList.get(captionList.size() - 1);
        }

        String category = (String) aiResponse.get("category");

        // Photo 생성
        Photo photo = Photo.builder()
                .photoFilepath(requestDto.getPhotoFilepath())
                .photoDatetime(requestDto.getPhotoDatetime())
                .photoUrl(photoUrl)
                .caption(caption)
                .category(category)
                .album(album)
                .build();

        photoRepository.save(photo);

        // Tag 처리 및 PhotoTag 저장
        @SuppressWarnings("unchecked")
        List<String> tagNames = (List<String>) aiResponse.get("tags");

        if (tagNames != null) {
            for (String tagName : tagNames) {
                Tag tag = tagRepository.findByTagName(tagName)
                        .orElseGet(() -> tagRepository.save(Tag.builder()
                                .tagName(tagName)
                                .build()));

                PhotoTag photoTag = new PhotoTag(photo, tag);
                photoTagRepository.save(photoTag);
            }
        }
        return photo;
    }

    @Transactional
    public void classifyPhoto(PhotoClassifyRequestDto requestDto) {
        Photo photo = photoRepository.findById(requestDto.getPhotoId())
                .orElseThrow(PhotoNotFoundException::new);

        User user = userRepository.findByDeviceUID(requestDto.getDeviceUID())
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));

        UserAlbum userAlbum = userAlbumRepository.findByUser(user)
                .orElseThrow(() -> new AlbumNotFoundException(ErrorCode.USER_ALBUM_NOT_FOUND));


        Album album = albumRepository.findByAlbumNameAndUserAlbum(photo.getCategory(), userAlbum)
                .orElseThrow(() -> new AlbumNotFoundException(ErrorCode.ALBUM_NOT_FOUND));

        photo.changeAlbum(album); // changeAlbum 메서드를 사용하여 앨범 업데이트
    }

    private Map<String, Object> getInfoFromAI(String photoUrl) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("url", photoUrl);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(aiServerUrl, requestBody, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new AiModelException(ErrorCode.AI_MODEL_ERROR);
            }
        } catch (HttpStatusCodeException ex) {
            // 4xx, 5xx 에러 모두 잡아서
            throw new AiModelException(ErrorCode.AI_MODEL_ERROR);
        } catch (RestClientException ex) {
            // 네트워크 문제 등 다른 예외
            throw new AiModelException(ErrorCode.AI_MODEL_ERROR);
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
                .photoId(photo.getId())
                .photoFilepath(photo.getPhotoFilepath())
                .build();
    }

    public List<PhotoResponseDto> getPhotosByAlbumId(Long albumId) {
        List<Photo> photos = photoRepository.findByAlbumId(albumId);
        return photos.stream()
                .map(photo -> PhotoResponseDto.builder()
                        .photoId(photo.getId())
                        .photoFilepath(photo.getPhotoFilepath())
                        .build())
                .collect(Collectors.toList());
    }

    public List<PhotoResponseDto> searchAllPhotos(String deviceUID, String keyword) {
        List<Photo> photos = photoRepository.searchAllPhotos(deviceUID, keyword);
        return convertToDtoList(photos);
    }

    public List<PhotoResponseDto> searchPhotosInAlbum(Long albumId, String keyword) {
        albumRepository.findById(albumId)
                .orElseThrow(() -> new AlbumNotFoundException(ErrorCode.ALBUM_NOT_FOUND));
        List<Photo> photos = photoRepository.searchPhotosInAlbum(albumId, keyword);
        return convertToDtoList(photos);
    }

    private List<PhotoResponseDto> convertToDtoList(List<Photo> photos) {
        return photos.stream().map(photo -> PhotoResponseDto.builder()
                        .photoId(photo.getId())
                        .photoFilepath(photo.getPhotoFilepath())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void moveToTrash(List<Long> photoIds) {
        for (Long photoId : photoIds) {
            Photo photo = photoRepository.findById(photoId)
                    .orElseThrow(PhotoNotFoundException::new);
            photo.setDeleted(true);
            photo.setDeletedAt(LocalDateTime.now());
        }
    }


    @Transactional
    public void restorePhotos(List<Long> photoIds) {
        for (Long photoId : photoIds) {
            Photo photo = photoRepository.findById(photoId)
                    .orElseThrow(PhotoNotFoundException::new);
            photo.setDeleted(false);
            photo.setDeletedAt(null);
        }
    }

    @Transactional(readOnly = true)
    public List<PhotoResponseDto> getTrashedPhotos(String deviceUID) {
        List<Photo> trashedPhotos = photoRepository
                .findAllDeletedByDeviceUID(deviceUID);

        return trashedPhotos.stream()
                .map(PhotoResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void deleteExpiredTrashedPhotos() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        List<Photo> expired = photoRepository.findAllByIsDeletedTrueAndDeletedAtBefore(threshold);
        photoRepository.deleteAll(expired);
    }

    public void emptyTrash(String deviceUID) {
        userRepository.findByDeviceUID(deviceUID)
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));
        List<Photo> trashedPhotos = photoRepository.findAllDeletedByDeviceUID(deviceUID);
        photoRepository.deleteAll(trashedPhotos);
    }

    public void deleteSelectedFromTrash(List<Long> photoIds) {
        List<Photo> photosToDelete = photoRepository.findAllById(photoIds).stream()
                .filter(Photo::isDeleted) // is_deleted = true 인 경우만
                .collect(Collectors.toList());
        photoRepository.deleteAll(photosToDelete);
    }







}
