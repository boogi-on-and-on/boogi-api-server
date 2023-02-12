package boogi.apiserver.domain.message.message.domain;

import boogi.apiserver.domain.model.TimeBaseEntity;
import boogi.apiserver.domain.user.domain.User;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "MESSAGE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
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

    private String content;

    private Boolean blocked_message;

    @Builder
    public Message(final User sender, final User receiver, final String content, final Boolean blocked_message) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.blocked_message = blocked_message;
    }
}
