package boogi.apiserver.domain.user.application;

import boogi.apiserver.domain.user.repository.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.response.UserDetailInfoDto;
import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserRepository userRepository;

    //todo: OAuth
    public User getUserByEmail(String email) {
        return userRepository.findByEmailValue(email)
                .orElseThrow(() -> new InvalidValueException("해당 이메일은 없는 계정입니다."));
    }

    public UserDetailInfoDto getUserDetailInfo(Long userId) {
        User user = userRepository.findUserById(userId);
        return UserDetailInfoDto.of(user);
    }
}
