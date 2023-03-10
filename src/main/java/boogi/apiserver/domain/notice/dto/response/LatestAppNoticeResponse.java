package boogi.apiserver.domain.notice.dto.response;

import boogi.apiserver.domain.notice.dto.dto.NoticeDto;
import lombok.Getter;

import java.util.List;


@Getter
public class LatestAppNoticeResponse {

    private List<NoticeDto> notices;

    public LatestAppNoticeResponse(List<NoticeDto> notices) {
        this.notices = notices;
    }

    public static LatestAppNoticeResponse from(List<NoticeDto> notices) {
        return new LatestAppNoticeResponse(notices);
    }
}
