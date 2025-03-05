package side.project.collec.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import side.project.collec.user.domain.User;
import side.project.collec.user.repository.UserRepository;
import side.project.collec.userAlbum.UserAlbum;
import side.project.collec.userAlbum.UserAlbumRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserAlbumRepository userAlbumRepository;

    @Transactional
    public User createUserWithDefaultAlbums(User user) {
        // User 저장
        User savedUser = userRepository.save(user);

        userAlbumRepository.save(UserAlbum.createWithDefaultAlbums(savedUser));

        return savedUser;
    }
}
