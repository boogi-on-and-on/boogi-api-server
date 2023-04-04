package boogi.apiserver.global.webclient.push;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
public class SyncHttpInvocationPushNotification implements SendPushNotification {

    @Value("${env.LAPI_URL}")
    private String LAPI_URL;

    private final WebClient client = WebClient.create();


    @Override
    public void joinNotification(List<Long> joinRequestIds) {
        final Mono<String> response = client.post()
                .uri(LAPI_URL + "/push")
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(
                        Map.of(
                                "pushType", "join",
                                "entity", Map.of("ids", joinRequestIds)
                        )
                ))
                .retrieve()
                .bodyToMono(String.class);

        log.info("response : {}", response);
    }

    @Override
    public void rejectNotification(List<Long> joinRequestId) {

    }

    @Override
    public void noticeNotification(Long noticeId) {

    }

    @Override
    public void commentNotification(Long commentId) {

    }

    @Override
    public void mentionNotification(List<Long> receiverIds, Long entityId, MentionType type) {
        if (receiverIds.isEmpty()) {
            log.info("receiverIds is empty. did not sended push.");
            return;
        }

        log.info("send push sync. receiverIds: {}, entityId: {} ,mentionType: {}", receiverIds, entityId, type);

        final Mono<String> response = client
                .post()
                .uri(LAPI_URL + "/push")
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(
                        Map.of(
                                "pushType", "mention",
                                "entity", Map.of(
                                        "type", type.getType(),
                                        "id", entityId
                                ),
                                "receiver", Map.of(
                                        "ids", receiverIds
                                )
                        )))
                .retrieve()
                .bodyToMono(String.class);

        final String message = response.block();

        log.info("message : {}", message);
    }
}
