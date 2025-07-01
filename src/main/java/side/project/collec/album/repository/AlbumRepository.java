package side.project.collec.album.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import side.project.collec.album.domain.Album;
import side.project.collec.userAlbum.domain.UserAlbum;


import java.util.Optional;

public interface AlbumRepository extends JpaRepository<Album, Long> {
    Optional<Album> findById(Long albumId);

    Optional<Album> findByAlbumNameAndUserAlbum(String albumName, UserAlbum userAlbum);
}
