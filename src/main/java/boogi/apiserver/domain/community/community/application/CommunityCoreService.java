package boogi.apiserver.domain.community.community.application;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.dto.response.JoinedCommunities;
import boogi.apiserver.domain.hashtag.community.application.CommunityHashtagCoreService;
import boogi.apiserver.domain.hashtag.community.dao.CommunityHashtagRepository;
import boogi.apiserver.domain.hashtag.community.domain.CommunityHashtag;
import boogi.apiserver.domain.member.application.MemberCoreService;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.postmedia.dao.PostMediaRepository;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.user.application.UserQueryService;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityCoreService {
    private final CommunityRepository communityRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommunityHashtagRepository communityHashtagRepository;
    private final PostMediaRepository postMediaRepository;

    private final CommunityHashtagCoreService communityHashtagCoreService;
    private final MemberCoreService memberCoreService;

    private final CommunityValidationService communityValidationService;

    private final UserQueryService userQueryService;
    private final CommunityQueryService communityQueryService;

    @Transactional
    public Community createCommunity(Community community, List<String> tags, Long userId) {
        User user = userQueryService.getUser(userId);
        communityValidationService.checkPreviousExistsCommunityName(community.getCommunityName());

        communityRepository.save(community);
        communityHashtagCoreService.addTags(community.getId(), tags);

        memberCoreService.joinMember(userId, community.getId(), MemberType.MANAGER);

        return community;
    }

    @Transactional
    public void shutdown(Long communityId) {
        Community community = communityQueryService.getCommunity(communityId);

        memberRepository.findAnyMemberExceptManager(communityId).ifPresent(m -> {
            throw new InvalidValueException("탈퇴하지 않은 부매니저 혹은 일반 맴버가 있습니다.");
        });

        community.shutdown();
    }

    @Transactional
    public void changeScope(Long communityId, Boolean isSecret) {
        Community community = communityQueryService.getCommunity(communityId);

        if (isSecret) {
            community.toPrivate();
        } else {
            community.toPublic();
        }
    }

    @Transactional
    public void changeApproval(Long communityId, Boolean isAuto) {
        Community community = communityQueryService.getCommunity(communityId);

        if (isAuto) {
            community.openAutoApproval();
        } else {
            community.closeAutoApproval();
        }
    }

    @Transactional
    public void update(Long communityId, String description, List<String> newTags) {
        Community community = communityQueryService.getCommunity(communityId);

        community.updateDescription(description);

        List<CommunityHashtag> prevHashtags = community.getHashtags(); //LAZY INIT

        if (Objects.isNull(newTags) || newTags.size() == 0) {
            if (prevHashtags.size() != 0) {
                communityHashtagRepository.deleteAllInBatch(prevHashtags);
            }
        } else {
            if (!isSameHashtags(prevHashtags, newTags)) {
                communityHashtagRepository.deleteAllInBatch(prevHashtags);

                List<CommunityHashtag> newHashtags = newTags.stream()
                        .map(t -> CommunityHashtag.of(t, community))
                        .collect(Collectors.toList());
                communityHashtagRepository.saveAll(newHashtags);
            }
        }
    }

    public boolean isSameHashtags(List<CommunityHashtag> prevHashtags, List<String> newTags) {
        List<String> prevTags = prevHashtags.stream()
                .map(CommunityHashtag::getTag)
                .collect(Collectors.toList());

        prevTags.sort(Comparator.naturalOrder());
        newTags.sort(Comparator.naturalOrder());

        return String.join("", prevTags).equals(String.join("", newTags));
    }

    public JoinedCommunities getJoinedCommunitiesWithLatestPost(Long userId) {
        User findUser = userQueryService.getUser(userId);

        List<Member> findMembers = memberRepository.findWhatIJoined(userId);

        Map<Long, Community> joinedCommunityMap = findMembers.stream()
                .map(m -> m.getCommunity())
                .collect(Collectors.toMap(
                        m1 -> m1.getId(),
                        m2 -> m2,
                        (o, n) -> n,
                        LinkedHashMap::new
                ));

        List<Post> latestPosts = postRepository.getLatestPostByCommunityIds(joinedCommunityMap.keySet());
        Map<Long, Post> latestPostMap = latestPosts.stream()
                .collect(Collectors.toMap(
                        lp1 -> lp1.getCommunity().getId(),
                        lp2 -> lp2,
                        (o, n) -> n,
                        LinkedHashMap::new
                ));

        List<Long> latestPostIds = latestPosts.stream()
                .map(lp -> lp.getId())
                .collect(Collectors.toList());

        List<PostMedia> postMedias = postMediaRepository.getPostMediasByLatestPostIds(latestPostIds);
        Map<Long, String> postMediaUrlMap = postMedias.stream()
                .collect(Collectors.toMap(
                        pm1 -> pm1.getPost().getId(),
                        pm2 -> pm2.getMediaURL(),
                        (o, n) -> n,
                        HashMap::new
                ));

        return JoinedCommunities.of(joinedCommunityMap, latestPostMap, postMediaUrlMap);
    }
}
