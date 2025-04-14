package side.project.collec.album.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import side.project.collec.album.AlbumRepository;
import side.project.collec.album.domain.Album;
import side.project.collec.global.exception.codes.ErrorCode;
import side.project.collec.global.exception.customException.AlbumNotFoundException;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;

    // deviceUID에 해당하는 앨범을 반환 (해당 앨범이 없으면 예외 발생)
    public Album getDefaultAlbumForUser(String deviceUID) {
        return albumRepository.findFirstAlbumByDeviceUID(deviceUID)
                .orElseThrow(() -> new AlbumNotFoundException(ErrorCode.ALBUM_NOT_FOUND));
    }
}