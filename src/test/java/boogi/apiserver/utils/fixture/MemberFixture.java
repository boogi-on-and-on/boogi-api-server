package boogi.apiserver.utils.fixture;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.utils.TestTimeReflection;

import java.time.LocalDateTime;

import static boogi.apiserver.domain.member.domain.MemberType.*;
import static boogi.apiserver.utils.fixture.TimeFixture.STANDARD;

public enum MemberFixture {
    SUNDO_POCS(MANAGER, null, STANDARD),
    YONGJIN_POCS(SUB_MANAGER, null, STANDARD.minusDays(1)),
    DEOKHWAN_POCS(NORMAL, null, STANDARD.minusDays(2)),
    YONGJIN_ENGLISH(MANAGER, null, STANDARD.minusDays(3)),
    ;


    public final MemberType memberType;
    public final LocalDateTime bannedAt;
    public final LocalDateTime createdAt;

    public static final Long SUNDO_POCS_ID = 1L;
    public static final Long YONGJIN_POCS_ID = 2L;
    public static final Long DEOKHWAN_POCS_ID = 3L;
    public static final Long YONGJIN_ENGLISH_ID = 4L;

    MemberFixture(MemberType memberType, LocalDateTime bannedAt, LocalDateTime createdAt) {
        this.memberType = memberType;
        this.bannedAt = bannedAt;
        this.createdAt = createdAt;
    }

    public Member toMember(User user, Community community) {
        return toMember(null, user, community);
    }

    public Member toMember(Long id, User user, Community community) {
        Member member = Member.builder()
                .id(id)
                .user(user)
                .community(community)
                .memberType(memberType)
                .bannedAt(bannedAt)
                .build();
        TestTimeReflection.setCreatedAt(member, this.createdAt);
        return member;
    }
}
