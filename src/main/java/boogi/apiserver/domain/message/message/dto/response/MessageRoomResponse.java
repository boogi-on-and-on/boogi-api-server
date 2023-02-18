package boogi.apiserver.domain.message.message.dto.response;

import boogi.apiserver.domain.message.message.domain.Message;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.util.time.TimePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;


@Getter
public class MessageRoomResponse {

    private List<MessageRoom> messageRooms;

    public MessageRoomResponse(List<MessageRoom> messageRooms) {
        this.messageRooms = messageRooms;
    }

    public static MessageRoomResponse from(List<MessageRoom> messageRooms) {
        return new MessageRoomResponse(messageRooms);
    }

    @Getter
    public static class MessageRoom {
        private Long id;
        private String name;
        private String tagNum;
        private String profileImageUrl;
        private RecentMessage recentMessage;

        @Builder(access = AccessLevel.PRIVATE)
        public MessageRoom(Long id, String name, String tagNum, String profileImageUrl, RecentMessage recentMessage) {
            this.id = id;
            this.name = name;
            this.tagNum = tagNum;
            this.profileImageUrl = profileImageUrl;
            this.recentMessage = recentMessage;
        }

        public static MessageRoom of(User user, Message message) {
            return MessageRoom.builder()
                    .id(user.getId())
                    .name(user.getUsername())
                    .tagNum(user.getTagNumber())
                    .profileImageUrl(user.getProfileImageUrl())
                    .recentMessage(RecentMessage.from(message))
                    .build();
        }
    }

    @Getter
    static class RecentMessage {
        private String content;

        @JsonFormat(pattern = TimePattern.BASIC_FORMAT_STRING)
        private LocalDateTime receivedAt;

        public RecentMessage(String content, LocalDateTime receivedAt) {
            this.content = content;
            this.receivedAt = receivedAt;
        }

        private static RecentMessage from(Message message) {
            return new RecentMessage(message.getContent(), message.getCreatedAt());
        }
    }
}
