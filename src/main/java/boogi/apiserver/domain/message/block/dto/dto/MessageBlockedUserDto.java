package boogi.apiserver.domain.message.block.dto.dto;

import boogi.apiserver.domain.user.domain.User;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class MessageBlockedUserDto {
    private Long userId;
    private String nameTag;

    public MessageBlockedUserDto(Long userId, String nameTag) {
        this.userId = userId;
        this.nameTag = nameTag;
    }

    @QueryProjection
    public MessageBlockedUserDto(Long userId, String name, String tag) {
        this(userId, name + tag);
    }

    public static MessageBlockedUserDto from(User user) {
        String nameTag = user.getUsername() + user.getTagNumber();
        return new MessageBlockedUserDto(user.getId(), nameTag);
    }
}
