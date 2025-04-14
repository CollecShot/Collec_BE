package side.project.collec.global.exception.responses;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import side.project.collec.global.exception.codes.ErrorCode;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class ErrorResponse {
    private final int status;
    private final String message;

    public static ErrorResponse create() {
        return new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR.getStatus().value(), ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .message(errorCode.getMessage())
                .build();
    }

    public static ResponseEntity<ErrorResponse> to(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getStatus().value())
                .body(ErrorResponse.of(errorCode));
    }
}