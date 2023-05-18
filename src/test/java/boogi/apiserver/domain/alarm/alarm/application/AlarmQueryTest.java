package boogi.apiserver.domain.alarm.alarm.application;


import boogi.apiserver.domain.alarm.alarm.domain.Alarm;
import boogi.apiserver.domain.alarm.alarm.dto.dto.AlarmsDto;
import boogi.apiserver.domain.alarm.alarm.dto.response.AlarmsResponse;
import boogi.apiserver.domain.alarm.alarm.repository.AlarmRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.utils.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static boogi.apiserver.utils.fixture.AlarmFixture.ALARM1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AlarmQueryTest {

    @Mock
    AlarmRepository alarmRepository;

    @InjectMocks
    AlarmQuery alarmQuery;

    @Test
    @DisplayName("알림 목록 조회")
    void getAlarms() {
        //given
        User user = UserFixture.YONGJIN.toUser(1L);
        Alarm alarm = ALARM1.toAlarm(1L, user);

        given(alarmRepository.getAlarms(anyLong()))
                .willReturn(List.of(alarm));

        //when
        AlarmsResponse alarmsResponse = alarmQuery.getAlarms(user.getId());

        //then
        List<AlarmsDto> dtos = alarmsResponse.getAlarms();

        assertThat(dtos).hasSize(1);

        AlarmsDto alarmDto = dtos.get(0);
        assertThat(alarmDto.getHead()).isEqualTo(ALARM1.head);
        assertThat(alarmDto.getBody()).isEqualTo(ALARM1.body);
        assertThat(alarmDto.getId()).isEqualTo(alarm.getId());
        assertThat(alarmDto.getCreatedAt()).isEqualTo(ALARM1.createdAt.toString());
    }
}