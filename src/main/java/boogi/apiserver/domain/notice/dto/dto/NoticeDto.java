package boogi.apiserver.domain.notice.dto.dto;

import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.global.util.time.TimePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
public class NoticeDto {

    protected Long id;
    protected String title;

    @JsonFormat(pattern = TimePattern.BASIC_FORMAT_STRING)
    protected LocalDateTime createdAt;

    public NoticeDto(final Long id, final String title, final LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
    }

    public static NoticeDto from(Notice notice) {
        return new NoticeDto(notice.getId(), notice.getTitle(), notice.getCreatedAt());
    }
}
