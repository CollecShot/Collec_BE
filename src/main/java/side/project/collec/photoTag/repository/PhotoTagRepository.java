package side.project.collec.photoTag.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.project.collec.photoTag.domain.PhotoTag;

public interface PhotoTagRepository extends JpaRepository<PhotoTag, Long> {
}
