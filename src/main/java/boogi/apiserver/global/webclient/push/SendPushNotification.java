package boogi.apiserver.global.webclient.push;

public interface SendPushNotification {
    void joinNotification(Long joinRequestId);

    void rejectNotification(Long joinRequestId);

    void noticeNotification(Long noticeId);

    void commentNotification(Long commentId);
}
