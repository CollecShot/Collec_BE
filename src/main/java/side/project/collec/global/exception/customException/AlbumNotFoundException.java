package side.project.collec.global.exception.customException;

import lombok.Getter;
import side.project.collec.global.exception.GlobalException;
import side.project.collec.global.exception.codes.ErrorCode;

@Getter
public class AlbumNotFoundException extends GlobalException{
    public AlbumNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
