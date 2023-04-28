package boogi.apiserver.utils.fixture;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.domain.CommunityCategory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public enum CommunityFixture {
    BASEBALL("야구 동아리", "우리는 야구를 좋아하는 사람들입니다.", false,
            CommunityCategory.HOBBY, 10, null, true),

    POCS("컴퓨터공학과 학술 소모임", "우리는 컴퓨터 기술을 공부하는 동아리입니다", false,
            CommunityCategory.ACADEMIC, 20, null, false),

    ENGLISH("영어 공부 소모임", "우리는 영어를 공부합니다", true, CommunityCategory.ACADEMIC,
            30, null, false);

    private final String communityName;
    private final String description;
    private final boolean isPrivate;
    private final CommunityCategory communityCategory;
    private final int memberCount;
    private final LocalDateTime deletedAt;
    private final boolean autoApproval;
    
    public static final Long BASEBALL_ID = 1L;
    public static final Long POCS_ID = 2L;
    public static final Long ENGLISH_ID = 3L;

    CommunityFixture(String communityName, String description, boolean isPrivate, CommunityCategory communityCategory, int memberCount, LocalDateTime deletedAt, boolean autoApproval) {
        this.communityName = communityName;
        this.description = description;
        this.isPrivate = isPrivate;
        this.communityCategory = communityCategory;
        this.memberCount = memberCount;
        this.deletedAt = deletedAt;
        this.autoApproval = autoApproval;
    }

    public Community toCommunity() {
        return toCommunity(null, null);
    }

    public Community toCommunity(Long id, List<String> hashtags) {
        Community community = Community.builder()
                .id(id)
                .communityName(communityName)
                .description(description)
                .isPrivate(isPrivate)
                .category(communityCategory)
                .memberCount(memberCount)
                .deletedAt(deletedAt)
                .autoApproval(autoApproval)
                .hashtags(new ArrayList<>())
                .build();
        community.addTags(hashtags);
        return community;
    }
}
