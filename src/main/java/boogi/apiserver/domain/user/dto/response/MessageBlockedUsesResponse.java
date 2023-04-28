package boogi.apiserver.domain.user.dto.response;

import boogi.apiserver.domain.message.block.dto.dto.MessageBlockedUserDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageBlockedUsesResponse {

    private List<MessageBlockedUserDto> blocked;

    public MessageBlockedUsesResponse(List<MessageBlockedUserDto> blocked) {
        this.blocked = blocked;
    }

    public static MessageBlockedUsesResponse from(List<MessageBlockedUserDto> blocked) {
        return new MessageBlockedUsesResponse(blocked);
    }
}
