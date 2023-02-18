package boogi.apiserver.domain.user.dto.response;

import boogi.apiserver.domain.message.block.dto.response.MessageBlockedUserDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class MessageBlockedUsesResponse {

    private final List<MessageBlockedUserDto> blocked;

    @Builder(access = AccessLevel.PRIVATE)
    private MessageBlockedUsesResponse(final List<MessageBlockedUserDto> blocked) {
        this.blocked = blocked;
    }

    public static MessageBlockedUsesResponse from(List<MessageBlockedUserDto> blocked) {
        return MessageBlockedUsesResponse.builder()
                .blocked(blocked)
                .build();
    }
}
