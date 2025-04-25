package side.project.collec.album.domain.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import side.project.collec.album.domain.Album;
import side.project.collec.userAlbum.UserAlbum;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlbumSimpleResponseDto {
    private Long userAlbumId;
    private Long albumId;

    public AlbumSimpleResponseDto(UserAlbum userAlbum) {
        this.userAlbumId = userAlbum.getId();
        this.albumId = userAlbum.getAlbums().get(0).getId();
    }
}
