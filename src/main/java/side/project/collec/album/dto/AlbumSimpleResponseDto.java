package side.project.collec.album.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import side.project.collec.album.Album;

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
