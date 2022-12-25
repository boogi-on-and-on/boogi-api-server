package boogi.apiserver.domain.message.delete.domain;

import boogi.apiserver.domain.message.message.domain.Message;
import boogi.apiserver.domain.model.TimeBaseEntity;
import boogi.apiserver.domain.user.domain.User;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "MESSAGE_DELETE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
public class MessageDelete extends TimeBaseEntity implements Serializable  {

    @Id
    @JoinColumn(name = "message_id")
    @ManyToOne(fetch = LAZY)
    private Message message;

    @Id
    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = LAZY)
    private User user;
}
