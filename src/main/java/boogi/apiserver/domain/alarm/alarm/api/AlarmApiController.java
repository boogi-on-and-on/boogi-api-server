package boogi.apiserver.domain.alarm.alarm.api;

import boogi.apiserver.domain.alarm.alarm.application.AlarmQueryService;
import boogi.apiserver.domain.alarm.alarm.dto.AlarmListDto;
import boogi.apiserver.global.argument_resolver.session.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/alarms")
public class AlarmApiController {

    private final AlarmQueryService alarmQueryService;

    @GetMapping
    public ResponseEntity<Object> getAlarms(@Session Long userId) {
        List<AlarmListDto> dtos = alarmQueryService.getAlarms(userId);
        return ResponseEntity.ok(Map.of(
                "alarms", dtos
        ));
    }
}
