package boogi.apiserver.global.webclient.push;

import java.util.List;

public interface SendPushNotification {
    void joinNotification(Long joinRequestId);

    void rejectNotification(Long joinRequestId);

    void noticeNotification(Long noticeId);

    void commentNotification(Long commentId);

    void mentionNotification(List<Long> receiverIds, Long entityId, MentionType type);
}
