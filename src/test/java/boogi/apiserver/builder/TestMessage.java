package boogi.apiserver.builder;

import boogi.apiserver.domain.message.message.domain.Message;

public class TestMessage {

    public static Message.MessageBuilder builder() {
        return Message.builder()
                .content("MESSAGE");
    }
}
