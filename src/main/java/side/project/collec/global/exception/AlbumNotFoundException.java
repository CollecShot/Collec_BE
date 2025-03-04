package side.project.collec.global.exception;

import lombok.Getter;
import side.project.collec.global.exception.codes.ErrorCode;

@Getter
public class AlbumNotFoundException extends GlobalException{
    public AlbumNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
