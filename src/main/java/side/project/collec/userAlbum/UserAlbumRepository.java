package side.project.collec.userAlbum;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAlbumRepository extends JpaRepository<UserAlbum, Long> {
    // deviceUID를 기반으로 UserAlbum 조회
    Optional<UserAlbum> findByUser_DeviceUID(String deviceUID);

}
