package boogi.apiserver.domain.member.domain;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.exception.NotBannedMemberException;
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

    @Builder
    private Member(final Long id, final Community community, final User user, final MemberType memberType,
                   final LocalDateTime bannedAt) {
        this.id = id;
        this.community = community;
        this.user = user;
        this.memberType = memberType;
        this.bannedAt = bannedAt;
    }

    private Member(Community community, User user, MemberType type) {
        this.community = community;
        this.user = user;
        this.memberType = type;
    }

    public static Member of(Community community, User user, MemberType type) {
        community.addMemberCount();

        return new Member(community, user, type);
    }

    public boolean isManager() {
        return this.memberType.hasManagerAuth();
    }

    public boolean isOperator() {
        return this.memberType.hasSubManagerAuth();
    }

    public void ban() {
        if (this.getBannedAt() == null) {
            this.bannedAt = LocalDateTime.now();
        }
    }

    public void release() {
        if (this.getBannedAt() == null) {
            throw new NotBannedMemberException();
        }
        this.bannedAt = null;
    }

    public void delegate(MemberType memberType) {
        this.memberType = memberType;
    }

    public boolean isNullMember() {
        return false;
    }
}
