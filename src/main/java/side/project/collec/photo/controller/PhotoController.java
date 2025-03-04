package side.project.collec.photo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import side.project.collec.photo.domain.dto.req.PhotoRequestDto;
import side.project.collec.photo.service.PhotoService;
import side.project.collec.global.exception.codes.SuccessCode;

import java.io.IOException;

@RestController
@RequestMapping("/photo")
@RequiredArgsConstructor
public class PhotoController {
    private final PhotoService photoService;

    @PostMapping("/upload")
    public ResponseEntity<String> savePhoto(@RequestPart("metadata") PhotoRequestDto requestDto,
                                            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        photoService.savePhoto(requestDto, image);
        return new ResponseEntity<>(SuccessCode.CREATED.getMessage(), SuccessCode.CREATED.getStatus());
    }

}
