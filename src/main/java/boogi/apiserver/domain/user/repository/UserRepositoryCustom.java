package boogi.apiserver.domain.user.repository;

import boogi.apiserver.domain.user.domain.User;

import java.util.List;

public interface UserRepositoryCustom {
    List<User> findUsersByIds(List<Long> userIds);
}
