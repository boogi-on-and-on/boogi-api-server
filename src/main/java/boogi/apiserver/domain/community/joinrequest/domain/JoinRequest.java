package boogi.apiserver.domain.community.joinrequest.domain;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.joinrequest.exception.NotPendingJoinRequestException;
import boogi.apiserver.domain.community.joinrequest.exception.UnmatchedJoinRequestCommunityException;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.model.TimeBaseEntity;
import boogi.apiserver.domain.user.domain.User;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
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

    @Builder
    private JoinRequest(Long id, User user, Community community, Member confirmedMember,
                        Member acceptor, JoinRequestStatus status) {
        this.id = id;
        this.user = user;
        this.community = community;
        this.confirmedMember = confirmedMember;
        this.acceptor = acceptor;
        this.status = status;
    }

    private JoinRequest(User user, Community community) {
        this.user = user;
        this.community = community;
    }

    public static JoinRequest of(User user, Community community) {
        return new JoinRequest(user, community);
    }

    public void reject(Member operator) {
        validateNotPending();
        this.acceptor = operator;
        this.status = JoinRequestStatus.REJECT;
    }

    public void confirm(Member operator, Member confirmedMember) {
        validateNotPending();
        this.acceptor = operator;
        this.confirmedMember = confirmedMember;
        this.status = JoinRequestStatus.CONFIRM;
    }

    public void validateJoinRequestCommunity(Long communityId) {
        if (!communityId.equals(this.community.getId())) {
            throw new UnmatchedJoinRequestCommunityException();
        }
    }

    private void validateNotPending() {
        if (!this.status.equals(JoinRequestStatus.PENDING)) {
            throw new NotPendingJoinRequestException();
        }
    }
}
