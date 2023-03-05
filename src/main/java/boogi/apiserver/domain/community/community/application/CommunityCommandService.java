package boogi.apiserver.domain.community.community.application;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.dto.dto.JoinedCommunitiesDto;
import boogi.apiserver.domain.hashtag.community.application.CommunityHashtagService;
import boogi.apiserver.domain.hashtag.community.dao.CommunityHashtagRepository;
import boogi.apiserver.domain.hashtag.community.domain.CommunityHashtag;
import boogi.apiserver.domain.member.application.MemberCommandService;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.postmedia.dao.PostMediaRepository;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommunityCommandService {
    private final CommunityRepository communityRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommunityHashtagRepository communityHashtagRepository;
    private final PostMediaRepository postMediaRepository;
    private final UserRepository userRepository;

    private final CommunityHashtagService communityHashtagService;
    private final MemberCommandService memberCommandService;

    private final CommunityValidationService communityValidationService;

    public Community createCommunity(Community community, List<String> tags, Long userId) {
        User user = userRepository.findByUserId(userId);
        communityValidationService.checkPreviousExistsCommunityName(community.getCommunityName());

        communityRepository.save(community);
        communityHashtagService.addTags(community.getId(), tags);

        memberCommandService.joinMember(userId, community.getId(), MemberType.MANAGER);

        return community;
    }

    public void shutdown(Long communityId) {
        Community community = communityRepository.findByCommunityId(communityId);

        memberRepository.findAnyMemberExceptManager(communityId).ifPresent(m -> {
            throw new InvalidValueException("탈퇴하지 않은 부매니저 혹은 일반 맴버가 있습니다.");
        });

        community.shutdown();
    }

    public void changeScope(Long communityId, Boolean isSecret) {
        Community community = communityRepository.findByCommunityId(communityId);

        if (isSecret) {
            community.toPrivate();
        } else {
            community.toPublic();
        }
    }

    public void changeApproval(Long communityId, Boolean isAuto) {
        Community community = communityRepository.findByCommunityId(communityId);

        if (isAuto) {
            community.openAutoApproval();
        } else {
            community.closeAutoApproval();
        }
    }

    public void update(Long communityId, String description, List<String> newTags) {
        Community community = communityRepository.findByCommunityId(communityId);

        community.updateDescription(description);

        List<CommunityHashtag> prevHashtags = community.getHashtags().getValues(); //LAZY INIT

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

    public JoinedCommunitiesDto getJoinedCommunitiesWithLatestPost(Long userId) {
        User findUser = userRepository.findByUserId(userId);

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

        return JoinedCommunitiesDto.of(joinedCommunityMap, latestPostMap, postMediaUrlMap);
    }
}
