package side.project.collec.album;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import side.project.collec.userAlbum.UserAlbum;
import side.project.collec.user.domain.User;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String albumName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_uid", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_album_id", nullable = false)
    private UserAlbum userAlbum;
}
