package side.project.collec.photo.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import side.project.collec.album.domain.Album;

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

    @Column(length = 200)
    private String tags;

    @Column(name = "category")
    private String category;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @Builder
    public Photo(String photoFilepath, LocalDateTime photoDatetime, String photoUrl, String category, Album album) {
        this.photoFilepath = photoFilepath;
        this.photoDatetime = photoDatetime;
        this.photoUrl = photoUrl;
        this.category = category;
        this.album = album;
    }
}
