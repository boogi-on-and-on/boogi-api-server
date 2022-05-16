package boogi.apiserver.domain.community.joinrequest.domain;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.model.TimeBaseEntity;
import boogi.apiserver.domain.user.domain.User;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "JOIN_REQUEST")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class JoinRequest extends TimeBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "join_request_id")
    private Long id;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = LAZY)
    private User user;

    @JoinColumn(name = "community_id")
    @ManyToOne(fetch = LAZY)
    private Community community;

    @JoinColumn(name = "confirmed_member_id")
    @OneToOne(fetch = LAZY)
    private Member confirmedMember; // 가입이 승인된 후 memberId

    @JoinColumn(name = "acceptor_id")
    @ManyToOne(fetch = LAZY)
    private Member acceptor; // 가입요청을 승인한 사람

    @Enumerated(EnumType.STRING)
    private JoinRequestStatus status = JoinRequestStatus.PENDING;

    private JoinRequest(User user, Community community) {
        this.user = user;
        this.community = community;
    }

    public void reject(Member manager) {
        this.acceptor = manager;
        this.status = JoinRequestStatus.REJECT;
    }

    public void confirm(Member manager, Member confirmedMember) {
        this.acceptor = manager;
        this.confirmedMember = confirmedMember;
        this.status = JoinRequestStatus.CONFIRM;
    }

    public static JoinRequest of(User user, Community community) {
        return new JoinRequest(user, community);
    }
}
