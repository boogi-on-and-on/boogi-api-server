package boogi.apiserver.domain.message.message.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@Getter
@NoArgsConstructor
public class SendMessageRequest {

    @NotNull(message = "수신자를 선택해주세요")
    private Long receiverId;

    @NotEmpty(message = "내용을 입력해주세요")
    @Size(max = 255, message = "255자 이내로 입력해주세요")
    private String content;

    public SendMessageRequest(Long receiverId, String content) {
        this.receiverId = receiverId;
        this.content = content;
    }
}
