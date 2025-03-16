package side.project.collec.photo.domain.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import side.project.collec.photo.domain.Photo;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhotoResponseDto {
    private Long id;
    private String photoFilepath;


    public static PhotoResponseDto fromEntity(Photo photo) {
        return PhotoResponseDto.builder()
                .id(photo.getId())
                .photoFilepath(photo.getPhotoFilepath())
                .build();
    }
}
