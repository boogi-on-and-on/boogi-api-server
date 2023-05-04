package boogi.apiserver.global.webclient.push;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DummyPushNotification implements SendPushNotification{
    @Override
    public void joinNotification(List<Long> joinRequestIds) {
        log.info("pretend to send push. joinRequestIds: {}", joinRequestIds);
    }

    @Override
    public void rejectNotification(List<Long> joinRequestIds) {
        log.info("pretend to send push. joinRequestIds: {}", joinRequestIds);
    }

    @Override
    public void noticeNotification(Long noticeId) {
        log.info("pretend to send push. noticeId: {}", noticeId);
    }

    @Override
    public void commentNotification(Long commentId) {
        log.info("pretend to send push. commentId: {}", commentId);

    }

    @Override
    public void mentionNotification(List<Long> receiverIds, Long entityId, MentionType type) {
        if (receiverIds.isEmpty()) {
            log.info("receiverIds is empty. did not send push.");
            return;
        }

        log.info("pretend to send push. receiverIds: {}, entityId: {} ,mentionType: {}", receiverIds, entityId, type);
    }
}
