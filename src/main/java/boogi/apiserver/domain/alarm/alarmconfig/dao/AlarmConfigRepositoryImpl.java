package boogi.apiserver.domain.alarm.alarmconfig.dao;

import boogi.apiserver.domain.alarm.alarmconfig.domain.AlarmConfig;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static boogi.apiserver.domain.alarm.alarmconfig.domain.QAlarmConfig.alarmConfig;


@RequiredArgsConstructor
public class AlarmConfigRepositoryImpl implements AlarmConfigRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<AlarmConfig> getAlarmConfigByUserId(Long userId) {
        List<AlarmConfig> configs = queryFactory.selectFrom(alarmConfig)
                .where(alarmConfig.user.id.eq(userId))
                .limit(1)
                .fetch();

        return Optional.ofNullable(configs.isEmpty() ? null : configs.get(0));
    }
}
