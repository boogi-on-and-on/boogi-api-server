package boogi.apiserver.domain.notice.dto.dto;

import boogi.apiserver.domain.notice.domain.Notice;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeDetailDto extends NoticeDto {

    private String content;

    @Builder(access = AccessLevel.PRIVATE)
    public NoticeDetailDto(Long id, String title, LocalDateTime createdAt, String content) {
        super(id, title, createdAt);
        this.content = content;
    }

    public static NoticeDetailDto from(Notice notice) {
        return NoticeDetailDto.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .createdAt(notice.getCreatedAt())
                .content(notice.getContent())
                .build();
    }

    public static List<NoticeDetailDto> dtoListFrom(List<Notice> notices) {
        return notices.stream()
                .map(NoticeDetailDto::from)
                .collect(Collectors.toList());
    }
}
