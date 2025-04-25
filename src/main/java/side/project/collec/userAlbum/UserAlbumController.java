package side.project.collec.userAlbum;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import side.project.collec.album.domain.dto.res.AlbumResponseDto;

import java.util.List;

@RestController
@RequestMapping("/user-albums")
@RequiredArgsConstructor
public class UserAlbumController {

    private final UserAlbumService userAlbumService;

    // deviceUID를 기반으로 매핑된 앨범 리스트 조회 (최신 사진 포함)
    @GetMapping
    @Operation(summary = "사용자 앨범 리스트 반환", description = "deviceUID를 기반으로 매핑된 앨범 리스트 조회합니다.")
    public ResponseEntity<List<AlbumResponseDto>> getAlbumsByDeviceUID(@RequestParam("deviceUID") String deviceUID) {
        List<AlbumResponseDto> albums = userAlbumService.getAlbumsByDeviceUID(deviceUID);
        return ResponseEntity.ok(albums);
    }
}
