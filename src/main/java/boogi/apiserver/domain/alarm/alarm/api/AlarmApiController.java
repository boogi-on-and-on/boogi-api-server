package boogi.apiserver.domain.alarm.alarm.api;

import boogi.apiserver.domain.alarm.alarm.application.AlarmService;
import boogi.apiserver.domain.alarm.alarm.application.AlarmQueryService;
import boogi.apiserver.domain.alarm.alarm.dto.response.AlarmListDto;
import boogi.apiserver.global.argument_resolver.session.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/alarms")
public class AlarmApiController {

    private final AlarmQueryService alarmQueryService;
    private final AlarmService alarmService;

    @GetMapping
    public ResponseEntity<Object> getAlarms(@Session Long userId) {
        List<AlarmListDto> dtos = alarmQueryService.getAlarms(userId);
        return ResponseEntity.ok(Map.of(
                "alarms", dtos
        ));
    }

    @PostMapping("/{alarmId}/delete")
    public ResponseEntity<Void> deleteAlarm(@Session Long userId, @PathVariable Long alarmId) {
        alarmService.deleteAlarm(userId, alarmId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
