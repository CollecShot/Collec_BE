package side.project.collec.global.exception.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import side.project.collec.global.exception.codes.SuccessCode;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class SuccessResponse<T> {
    private final int status;
    private final String message;
    private T data;

    public static ResponseEntity<SuccessResponse> of(SuccessCode success) {
        return ResponseEntity.status(success.getStatus())
                .body(new SuccessResponse(success.getStatusCode(), success.getMessage()));
    }

    public static <T> ResponseEntity<SuccessResponse<T>> of(SuccessCode success, T data) {

        return ResponseEntity.status(success.getStatus())
                .body(new SuccessResponse<T>(success.getStatusCode(), success.getMessage(), data)); //<T>
    }

    public static <T> ResponseEntity<SuccessResponse<T>> of(T data) {
        return ResponseEntity.status(SuccessCode.OK.getStatus())
                .body(new SuccessResponse<>(SuccessCode.OK.getStatusCode(), SuccessCode.OK.getMessage(), data));
    }
}