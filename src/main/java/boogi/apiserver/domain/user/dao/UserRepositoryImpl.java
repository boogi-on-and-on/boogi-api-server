package boogi.apiserver.domain.user.dao;

import boogi.apiserver.domain.user.domain.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static boogi.apiserver.domain.user.domain.QUser.user;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<User> findUsersByIds(List<Long> userIds) {
        return queryFactory.selectFrom(user)
                .where(
                        user.id.in(userIds)
                )
                .fetch();
    }

}
