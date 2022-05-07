package boogi.apiserver.global.webclient.requestdto.push;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PushRequestBody {

    private Message message;
    private Receiver receiver;

    @Data
    @AllArgsConstructor
    @Builder
    public static class Message {
        private String body;
        private String head;
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class Receiver {
        private String type;
        private Map<String, Object> condition;
    }


}
