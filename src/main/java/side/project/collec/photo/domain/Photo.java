package side.project.collec.photo.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import side.project.collec.album.domain.Album;
//import side.project.collec.category.domain.Category;
import side.project.collec.photoTag.domain.PhotoTag;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "caption")
    private String caption;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "category_id", nullable = false)
//    private Category category;

    @OneToMany(mappedBy = "photo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PhotoTag> photoTags = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @Builder
    public Photo(String photoFilepath, LocalDateTime photoDatetime, String photoUrl, String caption, Album album) {
        this.photoFilepath = photoFilepath;
        this.photoDatetime = photoDatetime;
        this.photoUrl = photoUrl;
        this.caption = caption;
        this.album = album;
    }
}
