package boogi.apiserver.domain.message.message.dto;

import boogi.apiserver.domain.message.message.domain.Message;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.util.time.TimePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Builder
public class MessageRoomResponse {

    private List<MessageRoom> messageRooms;

    public static MessageRoomResponse of(List<MessageRoom> messageRooms) {
        return MessageRoomResponse.builder()
                .messageRooms(messageRooms)
                .build();
    }

    @Getter
    @Builder
    public static class MessageRoom {
        private Long id;
        private String name;
        private String tagNum;
        private String profileImageUrl;
        private RecentMessage recentMessage;

        public static MessageRoom toDto(User user, Message message) {
            return MessageRoom.builder()
                    .id(user.getId())
                    .name(user.getUsername())
                    .tagNum(user.getTagNumber())
                    .profileImageUrl(user.getProfileImageUrl())
                    .recentMessage(RecentMessage.toDto(message))
                    .build();
        }
    }

    @Getter
    @Builder
    static class RecentMessage {
        private String content;

        @JsonFormat(pattern = TimePattern.BASIC_FORMAT_STRING)
        private LocalDateTime receivedAt;

        private static RecentMessage toDto(Message message) {
            return RecentMessage.builder()
                    .content(message.getContent())
                    .receivedAt(message.getCreatedAt())
                    .build();
        }
    }
}
