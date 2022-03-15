package boogi.apiserver.domain.user.application;

import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.exception.WithdrawnOrCanceledUserException;
import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserValidationService {

    private final UserRepository userRepository;

    public User getUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(InvalidValueException::new);
        if (user.getCanceledAt() != null) {
            throw new WithdrawnOrCanceledUserException();
        }
        return user;
    }

    public User getUserByEmail(String email) {
        Optional<User> byEmail = userRepository.findByEmail(email);
        if (byEmail.isEmpty()) {
            throw new InvalidValueException("해당 이메일은 없는 계정입니다.");
        }
        User user = byEmail.get();
        if (user.getCanceledAt() != null) {
            throw new WithdrawnOrCanceledUserException();
        }
        return user;
    }

}
