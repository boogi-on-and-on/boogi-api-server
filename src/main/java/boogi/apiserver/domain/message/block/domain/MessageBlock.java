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

    private boolean blocked = true;


    @Builder
    private MessageBlock(Long id, User user, User blockedUser, boolean blocked) {
        this.id = id;
        this.user = user;
        this.blockedUser = blockedUser;
        this.blocked = blocked;
    }

    private MessageBlock(User user, User blockedUser) {
        this.user = user;
        this.blockedUser = blockedUser;
    }

    public static MessageBlock of(User user, User blockedUser) {
        return new MessageBlock(user, blockedUser);
    }

    public void unblock() {
        this.blocked = false;
    }

    public void block() {
        this.blocked = true;
    }
}
