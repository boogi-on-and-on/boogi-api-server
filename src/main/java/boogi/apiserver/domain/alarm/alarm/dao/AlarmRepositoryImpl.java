package boogi.apiserver.domain.alarm.alarm.dao;

import boogi.apiserver.domain.alarm.alarm.domain.Alarm;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static boogi.apiserver.domain.alarm.alarm.domain.QAlarm.alarm;

@RequiredArgsConstructor
public class AlarmRepositoryImpl implements AlarmRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Alarm> getAlarms(Long userId) {
        return queryFactory.selectFrom(alarm)
                .where(
                        alarm.user.id.eq(userId)
                )
                .orderBy(alarm.createdAt.desc())
                .fetch();
    }
}
