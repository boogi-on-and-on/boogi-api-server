package boogi.apiserver.domain.user.dto.response;

import boogi.apiserver.domain.message.block.dto.dto.MessageBlockedUserDto;
import lombok.Getter;

import java.util.List;

@Getter
public class MessageBlockedUsesResponse {

    private final List<MessageBlockedUserDto> blocked;

    public MessageBlockedUsesResponse(final List<MessageBlockedUserDto> blocked) {
        this.blocked = blocked;
    }

    public static MessageBlockedUsesResponse from(List<MessageBlockedUserDto> blocked) {
        return new MessageBlockedUsesResponse(blocked);
    }
}
