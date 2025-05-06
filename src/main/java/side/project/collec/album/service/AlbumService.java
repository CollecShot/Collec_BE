package side.project.collec.album.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import side.project.collec.album.domain.Album;
import side.project.collec.album.domain.dto.req.MovePhotoRequestDto;
import side.project.collec.album.repository.AlbumRepository;
import side.project.collec.global.exception.codes.ErrorCode;
import side.project.collec.global.exception.customException.AlbumNotFoundException;
import side.project.collec.global.exception.customException.PhotoNotFoundException;
import side.project.collec.photo.domain.Photo;
import side.project.collec.photo.repository.PhotoRepository;
import side.project.collec.userAlbum.domain.UserAlbum;
import side.project.collec.userAlbum.repository.UserAlbumRepository;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final UserAlbumRepository userAlbumRepository;
    private final AlbumRepository albumRepository;
    private final PhotoRepository photoRepository;

    // UserAlbum과 default Album을 함께 처리
    public UserAlbum getUserAlbumWithDefaultAlbum (String deviceUID){
        UserAlbum userAlbum = userAlbumRepository.findByUser_DeviceUID(deviceUID)
                .orElseThrow(() -> new AlbumNotFoundException(ErrorCode.ALBUM_NOT_FOUND));

        if (userAlbum.getAlbums().isEmpty()) {
            throw new AlbumNotFoundException(ErrorCode.USER_ALBUM_NOT_FOUND);
        }

        return userAlbum;
    }

    @Transactional
    public void movePhotoToAlbum(MovePhotoRequestDto requestDto) {
        Photo photo = photoRepository.findById(requestDto.getPhotoId())
                .orElseThrow(PhotoNotFoundException::new);

        // 이동 대상 앨범 조회
        Album newAlbum = albumRepository.findById(requestDto.getTargetedAlbumId())
                .orElseThrow(() -> new AlbumNotFoundException(ErrorCode.ALBUM_NOT_FOUND));

        // 앨범 변경
        photo.changeAlbum(newAlbum); // Photo 엔티티에 changeAlbum 메서드 구현 필요

        // photoRepository.save(photo); (JPA 영속성 컨텍스트로 인해 save 생략 가능)
    }

    public int countScreenshot(Long albumId) {
        return photoRepository.countByAlbumId(albumId);
    }

}