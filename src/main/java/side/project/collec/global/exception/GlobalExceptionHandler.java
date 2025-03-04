package side.project.collec.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import side.project.collec.global.exception.codes.ErrorCode;
import side.project.collec.global.exception.responses.ErrorResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 커스텀에러 핸들링
     */
    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(GlobalException globalException) {
        ErrorCode errorCode = globalException.getErrorCode();
        log.info("CustomError.{}: {}", globalException.getClass().getSimpleName(), globalException.getMessage());
        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }

    /**
     * 전역 에러 핸들링
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        log.info("Error.{}: {}1", exception.getClass().getSimpleName(), exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.create());
    }

}