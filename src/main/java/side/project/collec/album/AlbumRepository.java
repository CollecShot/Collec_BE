package side.project.collec.album;

import org.springframework.data.jpa.repository.JpaRepository;
import side.project.collec.photo.domain.Photo;

import java.util.List;
import java.util.Optional;

public interface AlbumRepository extends JpaRepository<Album, Long> {
    Optional<Album> findById(Long albumId);

}
