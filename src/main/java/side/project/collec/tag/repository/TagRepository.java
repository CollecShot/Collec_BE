package side.project.collec.tag.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import side.project.collec.tag.domain.Tag;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByTagName(String tagName);
}
