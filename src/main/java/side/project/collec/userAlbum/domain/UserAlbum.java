package side.project.collec.userAlbum.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import side.project.collec.album.domain.Album;
import side.project.collec.user.domain.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_uid", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "userAlbum", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Album> albums = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // 기본 앨범이 포함된 UserAlbum 객체를 생성하는 정적 메서드
    public static UserAlbum createWithDefaultAlbums(User user) {
        UserAlbum userAlbum = UserAlbum.builder()
                .user(user)
                .build();
        userAlbum.addDefaultAlbums();
        return userAlbum;
    }

    // 기본 앨범을 UserAlbum에 추가하는 메서드
    private void addDefaultAlbums() {
        String[] defaultAlbumNames = {"기타", "쇼핑", "문서", "예약", "장소", "쿠폰", "대화기록", "노래", "동물", "인물"};

        for (String name : defaultAlbumNames) {
            Album album = Album.builder()
                    .albumName(name)
                    .userAlbum(this)
                    .build();
            this.albums.add(album);
        }
    }
}
