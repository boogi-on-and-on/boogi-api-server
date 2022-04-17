package boogi.apiserver.domain.notice.dto;

import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.UserBasicProfileDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommunityNoticeDetailDto extends NoticeDetailDto {

    private UserBasicProfileDto user;

    private CommunityNoticeDetailDto(Notice notice, User user) {
        super(notice);
        this.user = UserBasicProfileDto.of(user);
    }

    public static CommunityNoticeDetailDto of(Notice notice, User user) {
        return new CommunityNoticeDetailDto(notice, user);
    }

}
