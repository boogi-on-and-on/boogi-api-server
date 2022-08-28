package boogi.apiserver.domain.user.dao;

import boogi.apiserver.domain.user.domain.QUser;
import boogi.apiserver.domain.user.domain.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private final QUser user = QUser.user;

    @Override
    public List<User> findUsersByIds(List<Long> userIds) {
        return queryFactory.selectFrom(user)
                .where(
                        user.id.in(userIds)
                )
                .fetch();
    }

    @Override
    public Optional<User> findUserById(Long userId) {
        User findUser = queryFactory.selectFrom(user)
                .where(
                        user.id.eq(userId)
                )
                .fetchOne();
        return Optional.ofNullable(findUser);
    }
}
