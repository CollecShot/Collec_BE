package side.project.collec.album.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import side.project.collec.album.domain.Album;
import side.project.collec.userAlbum.domain.UserAlbum;


import java.util.Optional;

public interface AlbumRepository extends JpaRepository<Album, Long> {
    Optional<Album> findById(Long albumId);

    @Query(value = """
    SELECT * FROM album
    WHERE device_uid = :deviceUID
    ORDER BY user_album_id ASC, id ASC
    LIMIT 1
    """, nativeQuery = true)
    Optional<Album> findFirstAlbumByDeviceUID(@Param("deviceUID") String deviceUID);

    Optional<Album> findByAlbumNameAndUserAlbum(String albumName, UserAlbum userAlbum);
}
