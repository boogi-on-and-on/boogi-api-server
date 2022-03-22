package boogi.apiserver.domain.notice.dto;

import boogi.apiserver.domain.notice.domain.Notice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class NoticeDto {

    protected Long id;
    protected String title;
    protected String createdAt;

    protected NoticeDto(Notice notice) {
        this.id = notice.getId();
        this.title = notice.getTitle();
        this.createdAt = notice.getCreatedAt().toString();
    }

    public static NoticeDto of(Notice notice) {
        return new NoticeDto(notice);
    }
}
