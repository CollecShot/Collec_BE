package side.project.collec.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import side.project.collec.user.domain.User;
import side.project.collec.user.domain.dto.UserRequestDto;
import side.project.collec.user.repository.UserRepository;
import side.project.collec.userAlbum.UserAlbum;
import side.project.collec.userAlbum.UserAlbumRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserAlbumRepository userAlbumRepository;

    @Transactional
    public User createUserWithDefaultAlbums(UserRequestDto userDto) {
        User user = User.builder()
                .deviceUID(userDto.getDeviceUID())
                .build();

        User savedUser = userRepository.save(user);
        userAlbumRepository.save(UserAlbum.createWithDefaultAlbums(savedUser));

        return savedUser;
    }

}


