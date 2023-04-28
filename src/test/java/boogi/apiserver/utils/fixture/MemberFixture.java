package boogi.apiserver.utils.fixture;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.user.domain.User;

import java.time.LocalDateTime;

import static boogi.apiserver.domain.member.domain.MemberType.*;

public enum MemberFixture {
    SUNDO_POCS(MANAGER, null),
    YONGJIN_POCS(SUB_MANAGER, null),
    DEOKHWAN_POCS(NORMAL, null);


    private final MemberType memberType;
    private final LocalDateTime bannedAt;

    public static final Long SUNDO_POCS_ID = 1L;
    public static final Long YONGJIN_POCS_ID = 2L;
    public static final Long DEOKHWAN_POCS_ID = 3L;

    MemberFixture(MemberType memberType, LocalDateTime bannedAt) {
        this.memberType = memberType;
        this.bannedAt = bannedAt;
    }

    public Member toMember(User user, Community community) {
        return Member.builder()
                .user(user)
                .community(community)
                .memberType(memberType)
                .bannedAt(bannedAt)
                .build();
    }
}
