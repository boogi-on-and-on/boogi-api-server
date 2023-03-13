package boogi.apiserver.domain.notice.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor
public class NoticeCreateRequest {

    @NotNull(message = "공지사항이 작성될 커뮤니티를 선택해주세요")
    private Long communityId;

    @NotEmpty(message = "제목을 입력해주세요")
    @Size(min = 1, max = 30, message = "최대 30자까지 입력해주세요")
    private String title;

    @NotEmpty(message = "내용을 입력해주세요")
    @Size(min = 1, max = 1000, message = "최대 1000자까지 입력해주세요")
    private String content;

    public NoticeCreateRequest(Long communityId, String title, String content) {
        this.communityId = communityId;
        this.title = title;
        this.content = content;
    }
}
