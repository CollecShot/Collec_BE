package side.project.collec.photo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import side.project.collec.global.exception.customException.AlbumNotFoundException;
import side.project.collec.global.exception.customException.AlbumPhotoNotFoundException;
import side.project.collec.global.exception.customException.PhotoNotFoundException;
import side.project.collec.photo.domain.dto.req.PhotoRequestDto;
import side.project.collec.photo.domain.dto.res.PhotoResponseDto;
import side.project.collec.photo.service.PhotoService;
import side.project.collec.global.exception.codes.SuccessCode;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/photo")
@RequiredArgsConstructor
@Tag(name = "스크린샷", description = "스크린샷 관련 처리 API")
public class PhotoController {
    private final PhotoService photoService;

    @PostMapping("/upload")
    @Operation(summary = "스크린샷 업로드", description = "스크린샷 이미지를 AI 서버로 보내고 분석값을 DB에 저장합니다.")
    @ExceptionHandler(value = {AlbumNotFoundException.class})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request!"),
            @ApiResponse(responseCode = "403", description = "Forbidden! Already Exist"),
    })
    public ResponseEntity<String> UploadPhoto(@RequestPart("metadata") PhotoRequestDto requestDto,
                                            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        photoService.uploadPhotoExtractInfo(requestDto, image);
        return new ResponseEntity<>(SuccessCode.CREATED.getMessage(), SuccessCode.CREATED.getStatus());
    }

    //사진 상세 조회
    @GetMapping("/details")
    @Operation(summary = "스크린샷 상세 조회", description = "photo_id로 스크린샷 상세 조회합니다.")
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

    // 특정 앨범의 모든 사진 조회 (리스트 반환)
    @GetMapping("/albums")
    @Operation(summary = "앨범 내 모든 스크린샷 조회", description = "특정 앨범 속 모든 스크린샷을 조회합니다.")
    @ExceptionHandler(value = {AlbumPhotoNotFoundException.class})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request!"),
            @ApiResponse(responseCode = "403", description = "Forbidden! Already Exist"),
    })
    public ResponseEntity<List<PhotoResponseDto>> getPhotosByAlbum(@RequestParam("albumId") Long albumId) {
        List<PhotoResponseDto> photos = photoService.getPhotosByAlbumId(albumId);
        return ResponseEntity.ok(photos);
    }

    // 전체 검색
    @GetMapping("/search")
    @Operation(summary = "전체 키워드 검색", description = "모든 앨범에서 키워드를 검색합니다.")
    public ResponseEntity<List<PhotoResponseDto>> searchAllPhotos(
            @RequestParam("deviceUID") String deviceUID,
            @RequestParam("keyword") String keyword) {
        return ResponseEntity.ok(photoService.searchAllPhotos(deviceUID, keyword));

    }

    // 앨범 내 검색
    @GetMapping("/album/{albumId}/search")
    @Operation(summary = "특정 앨범 내 키워드 검색", description = "특정 앨범에서 키워드를 검색합니다.")
    public ResponseEntity<List<PhotoResponseDto>> searchPhotosInAlbum(
            @PathVariable("albumId") Long albumId,
            @RequestParam("keyword") String keyword) {
        return ResponseEntity.ok(photoService.searchPhotosInAlbum(albumId, keyword));
    }
}
