package side.project.collec.photo.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import side.project.collec.album.Album;

import java.time.LocalDateTime;

@Entity
@Table(name = "photo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "photo_filepath", nullable = false)
    private String photoFilepath;

    @Column(name = "photo_datetime", nullable = false)
    private LocalDateTime photoDatetime;

    @Column(name = "photo_url", nullable = false)
    private String photoUrl;

    @ManyToOne
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @Column(name = "tags", nullable = true)
    private String tags;

    @Builder
    public Photo(String photoFilepath, LocalDateTime photoDatetime, String photoUrl, String tags, Album album) {
        this.photoFilepath = photoFilepath;
        this.photoDatetime = photoDatetime;
        this.photoUrl = photoUrl;
        this.tags = tags;
        this.album = album;
    }
}
