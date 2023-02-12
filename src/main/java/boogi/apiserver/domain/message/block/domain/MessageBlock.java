package boogi.apiserver.domain.message.block.domain;

import boogi.apiserver.domain.model.TimeBaseEntity;
import boogi.apiserver.domain.user.domain.User;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "MESSAGE_BLOCK")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class MessageBlock extends TimeBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_block_id")
    private Long id;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = LAZY)
    private User user;

    @JoinColumn(name = "blocked_user_id")
    @ManyToOne(fetch = LAZY)
    private User blockedUser;

    private Boolean blocked;

    public void release() {
        this.blocked = false;
    }

    public void block() {
        this.blocked = true;
    }

    private MessageBlock(User user, User blockedUser) {
        this.user = user;
        this.blockedUser = blockedUser;
        this.blocked = true;
    }

    public static MessageBlock of(User user, User blockedUser) {
        return new MessageBlock(user, blockedUser);
    }
}
