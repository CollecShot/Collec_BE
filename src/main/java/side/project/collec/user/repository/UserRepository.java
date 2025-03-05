package side.project.collec.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.project.collec.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByDeviceUID(String deviceUID);
}
