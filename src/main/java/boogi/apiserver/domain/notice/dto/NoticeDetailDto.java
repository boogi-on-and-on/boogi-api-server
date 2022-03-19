package boogi.apiserver.domain.notice.dto;

import boogi.apiserver.domain.notice.domain.Notice;
import lombok.Data;

@Data
public class NoticeDetailDto extends NoticeDto {

    private String content;

    private NoticeDetailDto(Notice notice) {
        super(notice);
        this.content = notice.getContent();
    }

    public static NoticeDetailDto of(Notice notice) {
        return new NoticeDetailDto(notice);
    }
}
