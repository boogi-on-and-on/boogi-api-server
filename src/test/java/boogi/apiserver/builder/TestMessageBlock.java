package boogi.apiserver.builder;

import boogi.apiserver.domain.message.block.domain.MessageBlock;

public class TestMessageBlock {

    public MessageBlock.MessageBlockBuilder builder() {
        return MessageBlock.builder()
                .blocked(true);
    }
}
