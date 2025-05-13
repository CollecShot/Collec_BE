package side.project.collec.photo.domain.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import java.util.List;

@Getter
public class PhotoTrashRequestDto {

    @Schema(description = "휴지통으로 이동할 사진 ID 목록", example = "[1, 2, 3]")
    private List<Long> photoIds;
}