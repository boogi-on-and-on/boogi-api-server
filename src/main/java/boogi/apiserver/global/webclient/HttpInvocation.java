package boogi.apiserver.global.webclient;

import boogi.apiserver.global.webclient.requestdto.push.PushRequestBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
public class HttpInvocation {

    @Value("${env.LAPI_URL}")
    private String LAPI_URL;

    public void sendPush(PushRequestBody body) {
        log.info("[sendPush] Http Request to LAPI: {}", body);

        Mono<String> mono = WebClient.create()
                .post()
                .uri(LAPI_URL + "/push")
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(String.class);

        mono.subscribe();
    }
}
