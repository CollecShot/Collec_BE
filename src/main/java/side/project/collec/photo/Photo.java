package side.project.collec.photo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import side.project.collec.album.Album;
import side.project.collec.tag.Tag;

import java.time.LocalDateTime;
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

    @ManyToOne
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @OneToMany(mappedBy = "photo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Tag> Tags;
}
