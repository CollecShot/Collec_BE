package side.project.collec.photo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.project.collec.photo.domain.Photo;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
}
