package boogi.apiserver.domain.notice.dto.response;

import boogi.apiserver.domain.notice.dto.dto.NoticeDto;
import lombok.Getter;

import java.util.List;


@Getter
public class LatestNoticeResponse {

    private List<NoticeDto> notices;

    public LatestNoticeResponse(List<NoticeDto> notices) {
        this.notices = notices;
    }

    public static LatestNoticeResponse from(List<NoticeDto> notices) {
        return new LatestNoticeResponse(notices);
    }
}
