package boogi.apiserver.domain.notice.dto.dto;

import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.global.util.time.TimePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeDto {

    protected Long id;
    protected String title;

    @JsonFormat(pattern = TimePattern.BASIC_FORMAT_STRING)
    protected LocalDateTime createdAt;

    public NoticeDto(Long id, String title, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
    }

    public static NoticeDto from(Notice notice) {
        return new NoticeDto(notice.getId(), notice.getTitle(), notice.getCreatedAt());
    }

    public static List<NoticeDto> listFrom(List<Notice> notices) {
        return notices.stream()
                .map(NoticeDto::from)
                .collect(Collectors.toList());
    }
}
