package side.project.collec.userAlbum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.project.collec.user.domain.User;
import side.project.collec.userAlbum.domain.UserAlbum;

import java.util.Optional;

public interface UserAlbumRepository extends JpaRepository<UserAlbum, Long> {
    // deviceUID를 기반으로 UserAlbum 조회
    Optional<UserAlbum> findByUser_DeviceUID(String deviceUID);

    Optional<UserAlbum> findByUser(User user);
}
