package boogi.apiserver.domain.notice.dto.response;

import boogi.apiserver.domain.notice.domain.Notice;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NoticeDetailDto extends NoticeDto {

    private String content;

    protected NoticeDetailDto(Notice notice) {
        super(notice);
        this.content = notice.getContent();
    }

    public static NoticeDetailDto of(Notice notice) {
        return new NoticeDetailDto(notice);
    }
}
