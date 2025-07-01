package side.project.collec.photo.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import side.project.collec.photo.domain.Photo;

import java.time.LocalDateTime;
import java.util.List;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    // 삭제되지 않은 특정 앨범의 모든 사진 목록 조회
    @Query(value = "SELECT * FROM photo WHERE album_id = :albumId AND is_deleted = false", nativeQuery = true)
    List<Photo> findByAlbumIdAndNotDeleted(@Param("albumId") Long albumId);

    // 삭제되지 않은 특정 앨범의 최신 사진 1개 조회 (캡처 시간 기준 내림차순)
    @Query("SELECT p FROM Photo p WHERE p.album.id = :albumId AND p.isDeleted = false ORDER BY p.photoDatetime DESC")
    List<Photo> findLatestPhotoByAlbumId(@Param("albumId") Long albumId, Pageable pageable);


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

    @Query(value = "SELECT COUNT(*) FROM photo WHERE album_id = :albumId AND is_deleted = false", nativeQuery = true)
    int countByAlbumIdAndNotDeleted(@Param("albumId") Long albumId);


    @Query("SELECT p FROM Photo p WHERE p.album.userAlbum.user.deviceUID = :deviceUID AND p.isDeleted = true")
    List<Photo> findAllDeletedByDeviceUID(@Param("deviceUID") String deviceUID);

    List<Photo> findAllByIsDeletedTrueAndDeletedAtBefore(LocalDateTime threshold);


}
