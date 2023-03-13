package boogi.apiserver.domain.alarm.alarm.dao;

import boogi.apiserver.domain.alarm.alarm.domain.Alarm;
import boogi.apiserver.domain.alarm.alarm.exception.AlarmNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long>, AlarmRepositoryCustom {

    default Alarm findByAlarmId(Long userId) {
        return this.findById(userId).orElseThrow(AlarmNotFoundException::new);
    }
}
