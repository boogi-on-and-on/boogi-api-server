package boogi.apiserver.domain.alarm.alarmconfig.repository;

import boogi.apiserver.domain.alarm.alarmconfig.domain.AlarmConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlarmConfigRepository extends JpaRepository<AlarmConfig, Long>, AlarmConfigRepositoryCustom {
}