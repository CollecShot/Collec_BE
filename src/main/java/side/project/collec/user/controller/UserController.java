package side.project.collec.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import side.project.collec.album.domain.Album;
import side.project.collec.album.domain.dto.res.AlbumSimpleResponseDto;
import side.project.collec.album.service.AlbumService;
import side.project.collec.global.exception.codes.ErrorCode;
import side.project.collec.global.exception.codes.SuccessCode;
import side.project.collec.global.exception.responses.ErrorResponse;
import side.project.collec.global.exception.responses.SuccessResponse;
import side.project.collec.user.domain.User;
import side.project.collec.user.domain.dto.UserRequestDto;
import side.project.collec.user.service.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AlbumService albumService;

    @PostMapping("register")
    public ResponseEntity<?> createUser(@RequestBody UserRequestDto requestDto) {
        try {
            User createdUser = userService.createUserWithDefaultAlbums(requestDto);

            Album defaultAlbum = albumService.getDefaultAlbumForUser(createdUser.getDeviceUID());

            // 기본 앨범을 AlbumResponseDto로 변환
            AlbumSimpleResponseDto dto = new AlbumSimpleResponseDto(defaultAlbum);

            return SuccessResponse.of(SuccessCode.USER_CREATED, dto);
        } catch (Exception e) {
            return ErrorResponse.to(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
