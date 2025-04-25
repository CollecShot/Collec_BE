package side.project.collec.global.exception.customException;

import lombok.Getter;
import side.project.collec.global.exception.GlobalException;
import side.project.collec.global.exception.codes.ErrorCode;

@Getter
public class UserNotFoundException extends GlobalException {

    public UserNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}