package side.project.collec.global.exception.customException;

import lombok.Getter;
import side.project.collec.global.exception.GlobalException;
import side.project.collec.global.exception.codes.ErrorCode;

    @Getter
    public class AlbumPhotoNotFoundException extends GlobalException {
        public AlbumPhotoNotFoundException(ErrorCode errorCode) {
            super(errorCode);
        }
    }

