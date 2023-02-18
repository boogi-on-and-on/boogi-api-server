package boogi.apiserver.domain.alarm.alarm.dto.dto;

import boogi.apiserver.domain.alarm.alarm.domain.Alarm;
import boogi.apiserver.global.util.time.TimePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
public class AlarmsDto {
    private Long id;
    private String head;
    private String body;

    @JsonFormat(pattern = TimePattern.BASIC_FORMAT_STRING)
    private LocalDateTime createdAt;

    @Builder(access = AccessLevel.PRIVATE)
    public AlarmsDto(Long id, String head, String body, LocalDateTime createdAt) {
        this.id = id;
        this.head = head;
        this.body = body;
        this.createdAt = createdAt;
    }

    public static AlarmsDto of(Alarm alarm) {
        return AlarmsDto.builder()
                .id(alarm.getId())
                .head(alarm.getHead())
                .body(alarm.getBody())
                .createdAt(alarm.getCreatedAt())
                .build();
    }
}
