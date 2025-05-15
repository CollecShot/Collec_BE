package side.project.collec.global.exception.customException;

import lombok.Getter;
import side.project.collec.global.exception.GlobalException;
import side.project.collec.global.exception.codes.ErrorCode;

@Getter
public class AiModelException extends GlobalException {

    public AiModelException(ErrorCode errorCode) {
        super(errorCode);
    }
}
