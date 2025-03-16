package side.project.collec.photo.domain.dto.res;

import lombok.Data;
import java.util.List;

@Data
public class AiResponseDto {
    private String category;
    private List<String> tags;
    private List<String> caption;
}
