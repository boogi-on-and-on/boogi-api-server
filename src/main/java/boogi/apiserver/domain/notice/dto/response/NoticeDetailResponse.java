package boogi.apiserver.domain.notice.dto.response;

import boogi.apiserver.domain.notice.domain.Notice;
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

    public static NoticeDetailResponse communityNoticeOf(List<Notice> notices, Boolean manager) {
        final List<CommunityNoticeDetailDto> dtos = CommunityNoticeDetailDto.listOf(notices);
        return new NoticeDetailResponse(dtos, manager);
    }

    public static NoticeDetailResponse from(List<Notice> notices) {
        List<NoticeDetailDto> noticeDetailDtos = NoticeDetailDto.dtoListFrom(notices);
        return new NoticeDetailResponse(noticeDetailDtos, null);
    }
}
