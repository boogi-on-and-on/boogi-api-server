package boogi.apiserver.domain.alarm.alarm.controller;

import boogi.apiserver.domain.alarm.alarm.application.AlarmQuery;
import boogi.apiserver.domain.alarm.alarm.application.AlarmCommand;
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

    private final AlarmQuery alarmQuery;
    private final AlarmCommand alarmCommand;

    @GetMapping
    public AlarmsResponse getAlarms(@Session Long userId) {
        return alarmQuery.getAlarms(userId);
    }

    @PostMapping("/{alarmId}/delete")
    public void deleteAlarm(@Session Long userId, @PathVariable Long alarmId) {
        alarmCommand.deleteAlarm(userId, alarmId);
    }
}
