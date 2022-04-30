package boogi.apiserver.domain.alarm.alarm.application;


import boogi.apiserver.domain.alarm.alarm.dao.AlarmRepository;
import boogi.apiserver.domain.alarm.alarm.domain.Alarm;
import boogi.apiserver.domain.alarm.alarm.dto.AlarmListDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AlarmQueryServiceTest {

    @Mock
    AlarmRepository alarmRepository;

    @InjectMocks
    AlarmQueryService alarmQueryService;

    @Test
    void 알림목록_조회() {
        //given
        Alarm alarm = Alarm.builder()
                .id(1L)
                .head("해드1")
                .body("바디1")
                .build();
        alarm.setCreatedAt(LocalDateTime.now());

        given(alarmRepository.getAlarms(anyLong()))
                .willReturn(List.of(alarm));

        //when
        List<AlarmListDto> dtos = alarmQueryService.getAlarms(1L);

        //then
        assertThat(dtos.size()).isEqualTo(1);

        AlarmListDto first = dtos.get(0);
        assertThat(first.getBody()).isEqualTo("바디1");
        assertThat(first.getHead()).isEqualTo("해드1");
        assertThat(first.getId()).isEqualTo(1L);
        assertThat(first.getCreatedAt()).isEqualTo(alarm.getCreatedAt().toString());
    }
}