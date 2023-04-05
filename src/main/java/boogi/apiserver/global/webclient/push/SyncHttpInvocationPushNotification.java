package boogi.apiserver.global.webclient.push;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void mentionNotification(List<Long> receiverIds, Long entityId, MentionType type) {
        if (receiverIds.isEmpty()) {
            log.info("receiverIds is empty. did not sended push.");
            return;
        }

        log.info("send push sync. receiverIds: {}, entityId: {} ,mentionType: {}", receiverIds, entityId, type);


        final BodyInserter<Map<String, Object>, ReactiveHttpOutputMessage> body = BodyInserters.fromValue(
                Map.of(
                        "pushType", "mention",
                        "entity", Map.of(
                                "type", type.getType(),
                                "id", entityId
                        ),
                        "receiver", Map.of(
                                "ids", receiverIds
                        )
                ));

        final RestTemplate restTemplate = new RestTemplate();
        final HttpHeaders header = new HttpHeaders();
        header.add("X-Auth-Token", "08ca361e-4dc9-466c-8480-91b050189349");
        header.setContentType(MediaType.APPLICATION_JSON);

        final Map<String, Object> map = new HashMap<>();
        map.put("pushType", "mention");

        try {
            map.put("entity", Map.of(
                    "type", type.getType(),
                    "id", entityId));

            map.put("receiver", Map.of(
                    "ids", receiverIds
            ));
        } catch (Exception e) {
            throw new RuntimeException();
        }


        final HttpEntity<Object> entity = new HttpEntity<>(map, header);
        log.info("SEND");
        final ResponseEntity<String> exchange = restTemplate.exchange(LAPI_URL + "/push", HttpMethod.POST, entity, String.class);
        log.info(exchange.getBody().

                toString());


//        final Mono<String> response = client
//                .post()
//                .uri(LAPI_URL + "/push")
//                .accept(MediaType.APPLICATION_JSON)
//                .body(body)
//                .retrieve()
//                .bodyToMono(String.class);
//
//        final String message = response.block();

//        log.info("message : {}", message);
    }
}
