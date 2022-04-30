package boogi.apiserver.domain.alarm.alarm.dao;

import boogi.apiserver.domain.alarm.alarm.domain.Alarm;
import boogi.apiserver.domain.alarm.alarm.domain.QAlarm;
import com.querydsl.jpa.impl.JPAQueryFactory;

import javax.persistence.EntityManager;
import java.util.List;

public class AlarmRepositoryCustomImpl implements AlarmRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private final QAlarm alarm = QAlarm.alarm;

    public AlarmRepositoryCustomImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Alarm> getAlarms(Long userId) {
        return queryFactory.selectFrom(alarm)
                .where(
                        alarm.user.id.eq(userId),
                        alarm.canceledAt.isNull()
                )
                .orderBy(alarm.createdAt.desc())
                .fetch();
    }
}
