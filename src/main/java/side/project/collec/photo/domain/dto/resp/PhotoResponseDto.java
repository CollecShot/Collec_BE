package side.project.collec.photo.domain.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import side.project.collec.photo.domain.Photo;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhotoResponseDto {
    private String photoFilepath;


    public static PhotoResponseDto fromEntity(Photo photo) {
        return PhotoResponseDto.builder()
                .photoFilepath(photo.getPhotoFilepath())
                .build();
    }
}
