package side.project.collec.album.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import side.project.collec.album.domain.dto.req.MovePhotoRequestDto;
import side.project.collec.album.service.AlbumService;
import side.project.collec.global.exception.codes.ErrorCode;
import side.project.collec.global.exception.codes.SuccessCode;
import side.project.collec.global.exception.customException.AlbumNotFoundException;
import side.project.collec.global.exception.customException.PhotoNotFoundException;
import side.project.collec.global.exception.responses.ErrorResponse;
import side.project.collec.global.exception.responses.SuccessResponse;

@RestController
@RequestMapping("/album")
@RequiredArgsConstructor
@Tag(name = "앨범", description = "앨범 관련 API")
public class AlbumController {
    private final AlbumService albumService;

    @PostMapping("/move-photo")
    @Operation(summary = "원하는 앨범으로 이미지 이동", description = "사용자가 수동으로 앨범을 이동합니다.")
    public ResponseEntity<?> movePhotoToAlbum(@RequestBody MovePhotoRequestDto requestDto) {
        try {
            albumService.movePhotoToAlbum(requestDto);
            return SuccessResponse.of(SuccessCode.PHOTO_MOVED_TO_ALBUM, requestDto);
        } catch (AlbumNotFoundException e) {
            return ErrorResponse.to(ErrorCode.ALBUM_NOT_FOUND);
        } catch (PhotoNotFoundException e) {
            return ErrorResponse.to(ErrorCode.PHOTO_NOT_FOUND);
        } catch (Exception e) {
            return ErrorResponse.to(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }



}
