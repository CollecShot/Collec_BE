package side.project.collec.album.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import side.project.collec.userAlbum.domain.UserAlbum;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "album_name", nullable = false)
    private String albumName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_album_id", nullable = false)
    private UserAlbum userAlbum;
}
