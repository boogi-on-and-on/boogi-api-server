package boogi.apiserver.domain.member.domain;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.model.TimeBaseEntity;
import boogi.apiserver.domain.user.domain.User;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "MEMBER")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Member extends TimeBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @JoinColumn(name = "community_id")
    @ManyToOne(fetch = LAZY)
    private Community community;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = LAZY)
    private User user;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "member_type")
    private MemberType memberType;

    @Column(name = "banned_at")
    private LocalDateTime bannedAt;

    public void ban() {
        this.bannedAt = LocalDateTime.now();
    }

    public void release() {
        this.bannedAt = null;
    }

    public void delegate(MemberType memberType) {
        this.memberType = memberType;
    }

    private Member(Community community, User user, MemberType type) {
        this.community = community;
        this.user = user;
        this.memberType = type;
    }

    public static Member createNewMember(Community community, User user, MemberType type) {
        community.addMemberCount();
        return new Member(community, user, type);
    }

}
