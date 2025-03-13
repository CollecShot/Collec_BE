package side.project.collec.global.exception.customException;

import side.project.collec.global.exception.codes.ErrorCode;
import lombok.Getter;

@Getter
public class PhotoNotFoundException extends RuntimeException {
    private final ErrorCode errorCode;

    public PhotoNotFoundException() {
        super(ErrorCode.PHOTO_NOT_FOUND.getMessage());
        this.errorCode = ErrorCode.PHOTO_NOT_FOUND;
    }
}