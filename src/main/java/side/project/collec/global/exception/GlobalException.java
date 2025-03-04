package side.project.collec.global.exception;

import lombok.Getter;
import side.project.collec.global.exception.codes.ErrorCode;

@Getter
public class GlobalException extends RuntimeException {

    private final ErrorCode errorCode;

    public GlobalException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
