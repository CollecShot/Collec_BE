package side.project.collec.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import side.project.collec.userAlbum.UserAlbum;

import java.util.List;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(name = "device_uid", nullable = false, unique = true)  // deviceUID를 PK로 설정
    private String deviceUID;  // deviceUID가 기본 키로 설정됩니다.

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<UserAlbum> userAlbums;

}
