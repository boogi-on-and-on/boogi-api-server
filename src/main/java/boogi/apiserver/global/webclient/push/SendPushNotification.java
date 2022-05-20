package boogi.apiserver.global.webclient.push;

import java.util.List;

public interface SendPushNotification {
    void joinNotification(List<Long>  joinRequestId);

    void rejectNotification(List<Long>  joinRequestId);

    void noticeNotification(Long noticeId);

    void commentNotification(Long commentId);

    void mentionNotification(List<Long> receiverIds, Long entityId, MentionType type);
}
