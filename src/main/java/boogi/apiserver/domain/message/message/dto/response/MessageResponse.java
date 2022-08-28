package boogi.apiserver.domain.message.message.dto.response;

import boogi.apiserver.domain.message.message.domain.Message;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.dto.PaginationDto;
import boogi.apiserver.global.util.time.TimePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class MessageResponse {

    private UserInfo user;
    private List<MessageInfo> messages;
    private PaginationDto pageInfo;

    public static MessageResponse of(User user, Slice<Message> messagePage, Long userId) {
        List<MessageInfo> messages = messagePage.getContent().stream()
                .map(m -> MessageInfo.toDto(m, userId))
                .collect(Collectors.toList());
        Collections.reverse(messages);

        return MessageResponse.builder()
                .user(UserInfo.toDto(user))
                .messages(messages)
                .pageInfo(PaginationDto.of(messagePage))
                .build();
    }

    @Getter
    @Builder
    static class UserInfo {
        private Long id;
        private String name;
        private String tagNum;
        private String profileImageUrl;

        public static UserInfo toDto(User user) {
            return UserInfo.builder()
                    .id(user.getId())
                    .name(user.getUsername())
                    .tagNum(user.getTagNumber())
                    .profileImageUrl(user.getProfileImageUrl())
                    .build();
        }
    }

    @Getter
    @Builder
    static class MessageInfo {
        private Long id;
        private String content;

        @JsonFormat(pattern = TimePattern.BASIC_FORMAT_STRING)
        private LocalDateTime receivedAt;
        private Boolean me;

        public static MessageInfo toDto(Message message, Long userId) {
            return MessageInfo.builder()
                    .id(message.getId())
                    .content(message.getContent())
                    .receivedAt(message.getCreatedAt())
                    .me((userId.equals(message.getSender().getId())) ?
                            Boolean.TRUE : Boolean.FALSE)
                    .build();
        }
    }
}
