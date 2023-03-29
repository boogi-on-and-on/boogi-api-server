package boogi.apiserver.domain.alarm.alarm.dao;

import boogi.apiserver.domain.alarm.alarm.domain.Alarm;
import boogi.apiserver.domain.alarm.alarm.exception.AlarmNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long>, AlarmRepositoryCustom {

    default Alarm findAlarmById(Long alarmId) {
        return this.findById(alarmId).orElseThrow(AlarmNotFoundException::new);
    }
}
