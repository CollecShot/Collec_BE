package side.project.collec.album;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import side.project.collec.category.Category;
import side.project.collec.user.User;

import java.util.List;

@Entity
@Table(name = "album")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "album_name", nullable = false)
    private String albumName;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)  // user_id를 외래 키로 사용
    private User user;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Category> categories;

}
