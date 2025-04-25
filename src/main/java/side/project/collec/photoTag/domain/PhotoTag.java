package side.project.collec.photoTag.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import side.project.collec.photo.domain.Photo;
import side.project.collec.tag.domain.Tag;

@Entity
@Table(name = "photo_tag")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_id", nullable = false)
    private Photo photo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    public PhotoTag(Photo photo, Tag tag) {
        this.photo = photo;
        this.tag = tag;
    }
}

