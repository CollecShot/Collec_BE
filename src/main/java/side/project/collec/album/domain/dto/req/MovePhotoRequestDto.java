package side.project.collec.album.domain.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovePhotoRequestDto {
    private Long photoId;
    private Long targetedAlbumId;
}
