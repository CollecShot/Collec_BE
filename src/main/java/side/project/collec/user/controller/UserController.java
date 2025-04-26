package side.project.collec.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import side.project.collec.album.domain.dto.res.AlbumSimpleResponseDto;
import side.project.collec.album.service.AlbumService;
import side.project.collec.global.exception.codes.ErrorCode;
import side.project.collec.global.exception.codes.SuccessCode;
import side.project.collec.global.exception.responses.ErrorResponse;
import side.project.collec.global.exception.responses.SuccessResponse;
import side.project.collec.user.domain.User;
import side.project.collec.user.domain.dto.UserRequestDto;
import side.project.collec.user.service.UserService;
import side.project.collec.userAlbum.domain.UserAlbum;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "회원", description = "회원 관련 API")
public class UserController {

    private final UserService userService;
    private final AlbumService albumService;

    @PostMapping("register")
    @Operation(summary = "회원 등록", description = "회원을 등록하고 자동으로 기본 앨범을 생성합니다.")
    public ResponseEntity<?> createUser(@RequestBody UserRequestDto requestDto) {
        try {
            User createdUser = userService.createUserWithDefaultAlbums(requestDto);

            UserAlbum defaultUserAlbum = albumService.getUserAlbumWithDefaultAlbum(createdUser.getDeviceUID());

            // 기본 앨범을 AlbumResponseDto로 변환
            AlbumSimpleResponseDto dto = new AlbumSimpleResponseDto(defaultUserAlbum);

            return SuccessResponse.of(SuccessCode.USER_CREATED, dto);
        } catch (Exception e) {
            return ErrorResponse.to(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
