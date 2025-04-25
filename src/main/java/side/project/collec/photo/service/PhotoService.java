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
//import side.project.collec.category.domain.Category;
//import side.project.collec.category.repository.CategoryRepository;
import side.project.collec.global.exception.GlobalException;
import side.project.collec.global.exception.codes.ErrorCode;
import side.project.collec.global.exception.customException.AlbumNotFoundException;
import side.project.collec.global.exception.customException.PhotoNotFoundException;
import side.project.collec.global.exception.customException.UserNotFoundException;
import side.project.collec.photo.domain.Photo;
import side.project.collec.photo.domain.dto.req.PhotoRequestDto;
import side.project.collec.photo.domain.dto.res.PhotoResponseDto;
import side.project.collec.photo.repository.PhotoRepository;
import side.project.collec.photoTag.domain.PhotoTag;
import side.project.collec.photoTag.repository.PhotoTagRepository;
import side.project.collec.tag.domain.Tag;
import side.project.collec.tag.repository.TagRepository;
import side.project.collec.user.domain.User;
import side.project.collec.user.repository.UserRepository;
import side.project.collec.userAlbum.UserAlbum;
import side.project.collec.userAlbum.UserAlbumRepository;

import java.io.IOException;
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
    public void uploadPhotoExtractInfo(PhotoRequestDto requestDto, MultipartFile image) throws IOException {
        String photoUrl = uploadPhoto(image);

        // AI 응답으로부터 category, tags, caption 추출
        Map<String, Object> aiResponse = getInfoFromAI(photoUrl);
        String albumName = (String) aiResponse.get("category"); // == albumName
        @SuppressWarnings("unchecked")
        List<String> tagNames = (List<String>) aiResponse.get("tags");
//      String caption = (String) aiResponse.get("caption");

        // AI 응답값 수정 전까지 임시방편
        @SuppressWarnings("unchecked")
        List<String> captionList = (List<String>) aiResponse.get("caption");

        String caption = "";
        if (captionList != null && !captionList.isEmpty()) {
            // 가장 마지막 항목만 저장 (글 부분)
            caption = captionList.get(captionList.size() - 1);
        }

        // 사용자 조회
        User user = userRepository.findByDeviceUID(requestDto.getDeviceUID())
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));

        // 해당 사용자의 UserAlbum 조회 또는 생성
        UserAlbum userAlbum = userAlbumRepository.findByUser(user)
                .orElseGet(() -> userAlbumRepository.save(UserAlbum.builder()
                        .user(user)
                        .build()));

        // 사용자의 같은 이름의 앨범이 있는지 확인, 없으면 생성
        Album album = albumRepository.findByAlbumNameAndUserAlbum_User_DeviceUID(albumName, requestDto.getDeviceUID())
                .orElseGet(() -> albumRepository.save(Album.builder()
                        .albumName(albumName)
                        .userAlbum(userAlbum)
                        .build()));

        Photo photo = Photo.builder()
                .photoFilepath(requestDto.getPhotoFilepath())
                .photoDatetime(requestDto.getPhotoDatetime())
                .photoUrl(photoUrl)
                 .caption(caption)
                .album(album)
                .build();

        photoRepository.save(photo);

        // Tag 처리 및 PhotoTag 저장
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
    }

    private Map<String, Object> getInfoFromAI(String photoUrl) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("url", photoUrl);

        ResponseEntity<Map> response = restTemplate.postForEntity(aiServerUrl, requestBody, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new GlobalException(ErrorCode.AI_MODEL_ERROR);
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

}
