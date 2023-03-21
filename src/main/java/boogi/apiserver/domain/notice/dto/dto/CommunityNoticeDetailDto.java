package boogi.apiserver.domain.notice.dto.dto;

import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CommunityNoticeDetailDto extends NoticeDetailDto {

    private UserBasicProfileDto user;

    @Builder(access = AccessLevel.PRIVATE)
    public CommunityNoticeDetailDto(Long id, String title, LocalDateTime createdAt, String content, UserBasicProfileDto user) {
        super(id, title, createdAt, content);
        this.user = user;
    }

    public static CommunityNoticeDetailDto of(Notice notice) {
        return CommunityNoticeDetailDto.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .createdAt(notice.getCreatedAt())
                .content(notice.getContent())
                .user(UserBasicProfileDto.from(notice.getMember().getUser()))
                .build();
    }

    public static List<CommunityNoticeDetailDto> listOf(List<Notice> notices) {
        return notices.stream()
                .map(CommunityNoticeDetailDto::of)
                .collect(Collectors.toList());
    }
}
