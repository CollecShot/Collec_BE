package side.project.collec.photo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import side.project.collec.photo.domain.Photo;

import java.util.List;
import java.util.Optional;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    // 특정 앨범 ID에 속한 모든 사진 조회
    List<Photo> findByAlbumId(Long albumId);
}
