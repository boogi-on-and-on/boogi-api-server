package boogi.apiserver.domain.user.dao;

import boogi.apiserver.domain.user.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryCustom {
    List<User> findUsersByIds(List<Long> userIds);

    Optional<User> findUserById(Long userId);
}
