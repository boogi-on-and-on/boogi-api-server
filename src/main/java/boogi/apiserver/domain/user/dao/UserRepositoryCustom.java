package boogi.apiserver.domain.user.dao;

import boogi.apiserver.domain.user.domain.User;

import java.util.List;

public interface UserRepositoryCustom {
    List<User> findUsersById(List<Long> userIds);
}
