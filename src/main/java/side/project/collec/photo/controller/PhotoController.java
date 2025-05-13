package side.project.collec.photo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import side.project.collec.global.exception.codes.ErrorCode;
import side.project.collec.global.exception.customException.AlbumNotFoundException;
import side.project.collec.global.exception.customException.AlbumPhotoNotFoundException;
import side.project.collec.global.exception.customException.PhotoNotFoundException;
import side.project.collec.global.exception.responses.ErrorResponse;
import side.project.collec.global.exception.responses.SuccessResponse;
import side.project.collec.photo.domain.dto.req.PhotoClassifyRequestDto;
import side.project.collec.photo.domain.dto.req.PhotoRequestDto;
import side.project.collec.photo.domain.dto.req.PhotoTrashRequestDto;
import side.project.collec.photo.domain.dto.res.PhotoResponseDto;
import side.project.collec.photo.domain.dto.res.PhotoRestoreRequestDto;
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
    public ResponseEntity<?> uploadPhoto(
            @RequestPart("metadata") @Valid PhotoRequestDto requestDto,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            System.out.println("Received metadata: " + requestDto);

            PhotoResponseDto responseDto = PhotoResponseDto.fromEntity(photoService.uploadPhotoExtractInfo(requestDto, image));
            return SuccessResponse.of(SuccessCode.SCREENSHOT_PROCESSED, responseDto);

        } catch (AlbumNotFoundException ex) {
            return ErrorResponse.to(ErrorCode.ALBUM_NOT_FOUND);
        } catch (Exception ex) {
            return ErrorResponse.to(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/classify")
    @Operation(summary = "AI 분석 후 사진 분류", description = "AI 분석 결과를 바탕으로 사진의 앨범을 재분류합니다.")
    public ResponseEntity<String> classifyPhoto(@RequestBody PhotoClassifyRequestDto requestDto) {
        photoService.classifyPhoto(requestDto);
        return new ResponseEntity<>(SuccessCode.UPDATED.getMessage(), SuccessCode.UPDATED.getStatus());
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

    @PostMapping("/move/trash")
    @Operation(summary = "사진 휴지통으로 이동", description = "사진들을 휴지통으로 이동시킵니다.")
    public ResponseEntity<?> moveToTrash(@RequestBody PhotoTrashRequestDto requestDto) {
        try {
            photoService.moveToTrash(requestDto.getPhotoIds());
            return SuccessResponse.of(SuccessCode.PHOTO_MOVED_TO_TRASH);
        } catch (Exception e) {
            return ErrorResponse.to(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/restore")
    @Operation(summary = "휴지통에서 사진 복원", description = "휴지통에 있던 사진들을 복원합니다.")
    public ResponseEntity<?> restorePhotos(@RequestBody PhotoRestoreRequestDto requestDto) {
        try {
            photoService.restorePhotos(requestDto.getPhotoIds());
            return SuccessResponse.of(SuccessCode.PHOTO_RESTORED_FROM_TRASH);
        } catch (Exception e) {
            return ErrorResponse.to(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/trashes")
    @Operation(summary = "휴지통 사진 목록 조회", description = "휴지통에 있는 사진들을 조회합니다.")
    public ResponseEntity<?> getTrashedPhotos(@RequestParam("deviceUID") String deviceUID) {
        try {
            List<PhotoResponseDto> trashedPhotos = photoService.getTrashedPhotos(deviceUID);
            return SuccessResponse.of(SuccessCode.PHOTO_TRASH_LIST_RETRIEVED, trashedPhotos);
        } catch (Exception e) {
            return ErrorResponse.to(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }





}
