package side.project.collec.photo.domain.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoRequestDto {
    private String deviceUID;
    private Long albumId;
    private String photoFilepath;
    private LocalDateTime photoDatetime;
}
