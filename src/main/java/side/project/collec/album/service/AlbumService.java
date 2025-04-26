package side.project.collec.album.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import side.project.collec.global.exception.codes.ErrorCode;
import side.project.collec.global.exception.customException.AlbumNotFoundException;
import side.project.collec.userAlbum.domain.UserAlbum;
import side.project.collec.userAlbum.repository.UserAlbumRepository;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final UserAlbumRepository userAlbumRepository;

    // UserAlbum과 default Album을 함께 처리
    public UserAlbum getUserAlbumWithDefaultAlbum (String deviceUID){
        UserAlbum userAlbum = userAlbumRepository.findByUser_DeviceUID(deviceUID)
                .orElseThrow(() -> new AlbumNotFoundException(ErrorCode.ALBUM_NOT_FOUND));

        if (userAlbum.getAlbums().isEmpty()) {
            throw new AlbumNotFoundException(ErrorCode.USER_ALBUM_NOT_FOUND);
        }

        return userAlbum;
    }

}