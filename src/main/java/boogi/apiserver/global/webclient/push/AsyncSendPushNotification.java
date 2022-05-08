package boogi.apiserver.global.webclient.push;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
public class AsyncSendPushNotification implements SendPushNotification {

    @Value("${env.LAPI_URL}")
    private String LAPI_URL;

    private final WebClient client = WebClient.create();

    @Override
    public void joinNotification(Long joinRequestId) {
        log.info("send push. joinRequestId: {}", joinRequestId);

        System.out.println("LAPI_URL = " + LAPI_URL);

        client
                .post()
                .uri(LAPI_URL + "/push")
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(Map.of(
                        "pushType", "join",
                        "entity", Map.of("id", joinRequestId))))
                .retrieve()
                .bodyToMono(String.class)
                .subscribe();
    }

    @Override
    public void rejectNotification(Long joinRequestId) {
        log.info("send push. joinRequestId: {}", joinRequestId);

        System.out.println("LAPI_URL = " + LAPI_URL);

        client
                .post()
                .uri(LAPI_URL + "/push")
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(Map.of(
                        "pushType", "reject",
                        "entity", Map.of("id", joinRequestId))))
                .retrieve()
                .bodyToMono(String.class)
                .subscribe();
    }

    @Override
    public void noticeNotification(Long noticeId) {
        log.info("send push. noticeId: {}", noticeId);

        client
                .post()
                .uri(LAPI_URL + "/push")
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(Map.of(
                        "pushType", "notice",
                        "entity", Map.of("id", noticeId)
                )))
                .retrieve()
                .bodyToMono(String.class)
                .subscribe();
    }

    @Override
    public void commentNotification(Long commentId) {
        log.info("send push. commentId: {}", commentId);

        client
                .post()
                .uri(LAPI_URL + "/push")
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(Map.of(
                        "pushType", "comment",
                        "entity", Map.of("id", commentId)
                )))
                .retrieve()
                .bodyToMono(String.class)
                .subscribe();
    }
}
