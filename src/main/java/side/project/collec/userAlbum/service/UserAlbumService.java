package side.project.collec.userAlbum.service;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import side.project.collec.album.domain.dto.res.AlbumResponseDto;
import side.project.collec.global.exception.customException.AlbumNotFoundException;
import side.project.collec.photo.domain.Photo;
import side.project.collec.photo.repository.PhotoRepository;
import side.project.collec.userAlbum.domain.UserAlbum;
import side.project.collec.userAlbum.repository.UserAlbumRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAlbumService {

    private final UserAlbumRepository userAlbumRepository;
    private final PhotoRepository photoRepository;


    //홈화면  - 유저별 앨범 조회 . 최신 사진 (대표 사진)
    @Transactional(readOnly = true)
    @ExceptionHandler(value = {AlbumNotFoundException.class})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "403", description = "접근 권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "해당 deviceUID에 대한 앨범이 존재하지 않습니다."),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 발생.")
    })
    public List<AlbumResponseDto> getAlbumsByDeviceUID(String deviceUID) {
        UserAlbum userAlbum = userAlbumRepository.findByUser_DeviceUID(deviceUID)
                .orElseThrow(() -> new IllegalArgumentException("해당 deviceUID에 대한 앨범이 존재하지 않습니다."));

        return userAlbum.getAlbums().stream()
                .map(album -> {
                    // 각 앨범의 최신 사진 조회
                    Photo latestPhoto = photoRepository.findLatestPhotoByAlbumId(album.getId()).orElse(null);
                    return new AlbumResponseDto(album, latestPhoto);
                })
                .collect(Collectors.toList());
    }
}
