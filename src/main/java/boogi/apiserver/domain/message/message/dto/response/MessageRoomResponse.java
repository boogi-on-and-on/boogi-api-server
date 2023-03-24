package boogi.apiserver.domain.message.message.dto.response;

import boogi.apiserver.domain.message.message.domain.Message;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.util.time.TimePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Getter
public class MessageRoomResponse {

    private List<MessageRoomDto> messageRooms;

    public MessageRoomResponse(List<MessageRoomDto> messageRooms) {
        this.messageRooms = messageRooms;
    }

    public static MessageRoomResponse of(List<Long> opponentIds,
                                         Map<Long, User> opponentUserMap,
                                         LinkedHashMap<Long, Message> dedupMessages) {
        List<MessageRoomDto> rooms = opponentIds.stream()
                .map(oid -> MessageRoomDto.of(opponentUserMap.get(oid), dedupMessages.get(oid)))
                .collect(Collectors.toList());

        return new MessageRoomResponse(rooms);
    }

    @Getter
    public static class MessageRoomDto {
        private Long id;
        private String name;
        private String tagNum;
        private String profileImageUrl;
        private RecentMessageDto recentMessage;

        @Builder(access = AccessLevel.PRIVATE)
        public MessageRoomDto(Long id, String name, String tagNum, String profileImageUrl, RecentMessageDto recentMessage) {
            this.id = id;
            this.name = name;
            this.tagNum = tagNum;
            this.profileImageUrl = profileImageUrl;
            this.recentMessage = recentMessage;
        }

        public static MessageRoomDto of(User user, Message message) {
            return MessageRoomDto.builder()
                    .id(user.getId())
                    .name(user.getUsername())
                    .tagNum(user.getTagNumber())
                    .profileImageUrl(user.getProfileImageUrl())
                    .recentMessage(RecentMessageDto.from(message))
                    .build();
        }
    }

    @Getter
    public static class RecentMessageDto {
        private String content;

        @JsonFormat(pattern = TimePattern.BASIC_FORMAT_STRING)
        private LocalDateTime receivedAt;

        public RecentMessageDto(String content, LocalDateTime receivedAt) {
            this.content = content;
            this.receivedAt = receivedAt;
        }

        private static RecentMessageDto from(Message message) {
            return new RecentMessageDto(message.getContent(), message.getCreatedAt());
        }
    }
}
