package side.project.collec.photo.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import side.project.collec.global.exception.customException.AlbumNotFoundException;
import side.project.collec.global.exception.customException.PhotoNotFoundException;
import side.project.collec.photo.domain.dto.req.PhotoRequestDto;
import side.project.collec.photo.domain.dto.resp.PhotoResponseDto;
import side.project.collec.photo.service.PhotoService;
import side.project.collec.global.exception.codes.SuccessCode;

import java.io.IOException;

@RestController
@RequestMapping("/photo")
@RequiredArgsConstructor
public class PhotoController {
    private final PhotoService photoService;

    @PostMapping("/upload")
    @ExceptionHandler(value = {AlbumNotFoundException.class})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request!"),
            @ApiResponse(responseCode = "403", description = "Forbidden! Already Exist"),
    })
    public ResponseEntity<String> savePhoto(@RequestPart("metadata") PhotoRequestDto requestDto,
                                            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        photoService.savePhoto(requestDto, image);
        return new ResponseEntity<>(SuccessCode.CREATED.getMessage(), SuccessCode.CREATED.getStatus());
    }

    //사진 상세 조회
    @GetMapping("/details")
    @ExceptionHandler(value = {PhotoNotFoundException.class})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request!"),
            @ApiResponse(responseCode = "403", description = "Forbidden! Already Exist"),
            @ApiResponse(responseCode = "404", description = "Photo Not Found!")  // 404 추가
    })
    public ResponseEntity<PhotoResponseDto> getPhoto(@RequestParam("id") Long id) throws IOException {
        PhotoResponseDto photoResponseDto = photoService.photoDetail(id);
        return ResponseEntity.ok(photoResponseDto);
    }

}
