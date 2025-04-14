package side.project.collec.album.domain.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import side.project.collec.album.domain.Album;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlbumSimpleResponseDto {
    private Long albumId;

    public AlbumSimpleResponseDto(Album album) {
        this.albumId = album.getId();
    }
}
