package boogi.apiserver.domain.user.dao;

import boogi.apiserver.domain.user.domain.QUser;
import boogi.apiserver.domain.user.domain.User;
import com.querydsl.jpa.impl.JPAQueryFactory;

import javax.persistence.EntityManager;
import java.util.List;

public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private final QUser user = QUser.user;

    public UserRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<User> findUsersById(List<Long> userIds) {
        return queryFactory.selectFrom(user)
                .where(
                        user.id.in(userIds),
                        user.canceledAt.isNull()
                )
                .fetch();
    }
}
