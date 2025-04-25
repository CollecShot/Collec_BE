package side.project.collec.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.project.collec.user.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByDeviceUID(String deviceUID);
}
