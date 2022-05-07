package boogi.apiserver.domain.message.message.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class SendMessage {

    private Long receiverId;
    private String content;
}
