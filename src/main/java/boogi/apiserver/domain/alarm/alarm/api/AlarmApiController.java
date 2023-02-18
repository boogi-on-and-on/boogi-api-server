package boogi.apiserver.domain.alarm.alarm.api;

import boogi.apiserver.domain.alarm.alarm.application.AlarmQueryService;
import boogi.apiserver.domain.alarm.alarm.application.AlarmService;
import boogi.apiserver.domain.alarm.alarm.dto.response.AlarmsResponse;
import boogi.apiserver.global.argument_resolver.session.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/alarms")
public class AlarmApiController {

    private final AlarmQueryService alarmQueryService;
    private final AlarmService alarmService;

    @GetMapping
    public AlarmsResponse getAlarms(@Session Long userId) {
        return alarmQueryService.getAlarms(userId);
    }

    @PostMapping("/{alarmId}/delete")
    public void deleteAlarm(@Session Long userId, @PathVariable Long alarmId) {
        alarmService.deleteAlarm(userId, alarmId);
    }
}
