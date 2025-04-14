package side.project.collec.album.domain.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import side.project.collec.album.domain.Album;
import side.project.collec.photo.domain.Photo;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlbumResponseDto {
    private Long albumId;
    private String albumName;
    private String latestPhotoFilepath;

    public AlbumResponseDto(Album album, Photo latestPhoto) {
        this.albumId = album.getId();
        this.albumName = album.getAlbumName();
        this.latestPhotoFilepath = (latestPhoto != null) ? latestPhoto.getPhotoFilepath() : null;
    }
}
