package boogi.apiserver.domain.user.application;

import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.exception.WithdrawnOrCanceledUserException;
import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
