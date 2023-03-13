package boogi.apiserver.domain.message.message.dto.response;

import boogi.apiserver.domain.message.message.domain.Message;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import boogi.apiserver.global.dto.PaginationDto;
import boogi.apiserver.global.util.time.TimePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MessageResponse {

    private UserBasicProfileDto user;
    private List<MessageInfo> messages;
    private PaginationDto pageInfo;

    public MessageResponse(UserBasicProfileDto user, List<MessageInfo> messages, PaginationDto pageInfo) {
        this.user = user;
        this.messages = messages;
        this.pageInfo = pageInfo;
    }

    public static MessageResponse of(User user, Slice<Message> messagePage, Long userId) {
        List<MessageInfo> messages = messagePage.getContent().stream()
                .map(m -> MessageInfo.of(m, userId))
                .collect(Collectors.toList());
        Collections.reverse(messages);

        return new MessageResponse(UserBasicProfileDto.from(user), messages, PaginationDto.of(messagePage));
    }

    @Getter
    static class MessageInfo {
        private Long id;
        private String content;

        @JsonFormat(pattern = TimePattern.BASIC_FORMAT_STRING)
        private LocalDateTime receivedAt;
        private Boolean me;

        @Builder(access = AccessLevel.PRIVATE)
        public MessageInfo(Long id, String content, LocalDateTime receivedAt, Boolean me) {
            this.id = id;
            this.content = content;
            this.receivedAt = receivedAt;
            this.me = me;
        }

        public static MessageInfo of(Message message, Long userId) {
            Boolean me = Boolean.valueOf(userId.equals(message.getSender().getId()));

            return MessageInfo.builder()
                    .id(message.getId())
                    .content(message.getContent())
                    .receivedAt(message.getCreatedAt())
                    .me(me)
                    .build();
        }
    }
}
