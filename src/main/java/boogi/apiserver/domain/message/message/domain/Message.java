package boogi.apiserver.domain.message.message.domain;

import boogi.apiserver.domain.model.TimeBaseEntity;
import boogi.apiserver.domain.user.domain.User;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "MESSAGE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message extends TimeBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    @JoinColumn(name = "sender_id")
    @ManyToOne(fetch = LAZY)
    private User sender;

    @JoinColumn(name = "receiver_id")
    @ManyToOne(fetch = LAZY)
    private User receiver;

    @Embedded
    private Content content;

    private boolean blocked_message;

    @Builder
    public Message(final User sender, final User receiver, final String content, final Boolean blocked_message) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = new Content(content);
        this.blocked_message = blocked_message;
    }

    public Long getId() {
        return id;
    }

    public User getSender() {
        return sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public String getContent() {
        return content.getValue();
    }

    public boolean isBlocked_message() {
        return blocked_message;
    }
}
