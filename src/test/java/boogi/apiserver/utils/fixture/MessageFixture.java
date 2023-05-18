package boogi.apiserver.utils.fixture;

import boogi.apiserver.domain.message.message.domain.Message;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.utils.TestTimeReflection;

import java.time.LocalDateTime;

import static boogi.apiserver.utils.fixture.TimeFixture.*;

public enum MessageFixture {
    MESSAGE1("메시지입니다.", false, STANDARD),
    MESSAGE2("차단된 메시지입니다.", true, STANDARD);

    public final String content;
    public final boolean blocked_message;
    public final LocalDateTime createdAt;

    MessageFixture(String content, boolean blocked_message, LocalDateTime createdAt) {
        this.content = content;
        this.blocked_message = blocked_message;
        this.createdAt = createdAt;
    }

    public Message toMessage(User sender, User receiver) {
        return toMessage(null, sender, receiver);
    }

    public Message toMessage(Long id, User sender, User receiver) {
        Message message = Message.builder()
                .id(id)
                .sender(sender)
                .receiver(receiver)
                .content(this.content)
                .blocked_message(this.blocked_message)
                .build();
        TestTimeReflection.setCreatedAt(message, this.createdAt);
        return message;
    }
}
