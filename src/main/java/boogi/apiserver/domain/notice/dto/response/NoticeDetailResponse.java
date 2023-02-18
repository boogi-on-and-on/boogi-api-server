package boogi.apiserver.domain.notice.dto.response;

import boogi.apiserver.domain.notice.dto.dto.CommunityNoticeDetailDto;
import boogi.apiserver.domain.notice.dto.dto.NoticeDetailDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.List;


@Getter
public class NoticeDetailResponse {

    List<? extends NoticeDetailDto> notices;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    Boolean manager;

    public NoticeDetailResponse(List<? extends NoticeDetailDto> notices, Boolean manager) {
        this.notices = notices;
        this.manager = manager;
    }

    public static NoticeDetailResponse communityNoticeOf(List<CommunityNoticeDetailDto> communityNoticeDetailDtos,
                                                         Boolean manager) {
        return new NoticeDetailResponse(communityNoticeDetailDtos, manager);
    }

    public static NoticeDetailResponse of(List<NoticeDetailDto> noticeDetailDtos) {
        return new NoticeDetailResponse(noticeDetailDtos, null);
    }
}
