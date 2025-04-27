package side.project.collec.photo.domain.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoClassifyRequestDto {
    private Long photoId;
    private String deviceUID;

}

