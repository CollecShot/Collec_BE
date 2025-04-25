package side.project.collec.photo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import side.project.collec.photo.domain.Photo;

import java.util.List;
import java.util.Optional;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    // 특정 앨범 ID에 속한 모든 사진 조회
    List<Photo> findByAlbumId(Long albumId);

    // 특정 앨범에서 가장 최신 사진 1개 조회
    @Query("SELECT p FROM Photo p WHERE p.album.id = :albumId ORDER BY p.photoDatetime DESC LIMIT 1")
    Optional<Photo> findLatestPhotoByAlbumId(@Param("albumId") Long albumId);

    // 전체 키워드 검색
    @Query("SELECT DISTINCT p FROM Photo p " +
            "JOIN p.album a " +
            "JOIN a.userAlbum ua " +
            "JOIN ua.user u " +
            "LEFT JOIN PhotoTag pt ON pt.photo = p " +
            "LEFT JOIN Tag t ON pt.tag = t " +
            "WHERE u.deviceUID = :deviceUID " +
            "AND (p.caption LIKE %:keyword% OR t.tagName LIKE %:keyword%)")
    List<Photo> searchAllPhotos(@Param("deviceUID") String deviceUID,
                                @Param("keyword") String keyword);

    // 특정 앨범 키워드 검색
    @Query("SELECT DISTINCT p FROM Photo p " +
            "JOIN p.album a " +
            "LEFT JOIN PhotoTag pt ON pt.photo = p " +
            "LEFT JOIN Tag t ON pt.tag = t " +
            "WHERE a.id = :albumId " +
            "AND (p.caption LIKE %:keyword% OR t.tagName LIKE %:keyword%)")
    List<Photo> searchPhotosInAlbum(@Param("albumId") Long albumId,
                                    @Param("keyword") String keyword);

}
