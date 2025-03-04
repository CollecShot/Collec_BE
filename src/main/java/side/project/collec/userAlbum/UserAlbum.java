package side.project.collec.userAlbum;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import side.project.collec.album.Album;
import side.project.collec.user.domain.User;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAlbum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "device_uid", referencedColumnName = "deviceUID", nullable = false)
    private User user;

    @OneToMany(mappedBy = "userAlbum", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Album> albums;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public static UserAlbum createDefaultAlbum(User user) {
        return UserAlbum.builder()
                .user(user)
                .build();
    }
}

