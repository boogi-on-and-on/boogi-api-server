package boogi.apiserver.domain.post.post.application;

import boogi.apiserver.domain.community.community.repository.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.dto.response.CommunityPostsResponse;
import boogi.apiserver.domain.like.application.LikeQueryService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.repository.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.post.post.repository.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.dto.HotPostDto;
import boogi.apiserver.domain.post.post.dto.dto.LatestCommunityPostDto;
import boogi.apiserver.domain.post.post.dto.dto.SearchPostDto;
import boogi.apiserver.domain.post.post.dto.request.PostQueryRequest;
import boogi.apiserver.domain.post.post.dto.response.HotPostsResponse;
import boogi.apiserver.domain.post.post.dto.response.PostDetailResponse;
import boogi.apiserver.domain.post.post.dto.response.UserPostPageResponse;
import boogi.apiserver.domain.post.post.exception.PostNotFoundException;
import boogi.apiserver.domain.post.postmedia.repository.PostMediaRepository;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostQueryService {

    private final MemberRepository memberRepository;
    private final PostMediaRepository postMediaRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;

    private final MemberQueryService memberQueryService;
    private final LikeQueryService likeQueryService;

    public PostDetailResponse getPostDetail(Long postId, Long userId) {
        Post findPost = postRepository.getPostWithAll(postId)
                .orElseThrow(PostNotFoundException::new);

        Member member = memberQueryService.getViewableMember(userId, findPost.getCommunity());
        List<PostMedia> findPostMedias = postMediaRepository.findByPost(findPost);

        return PostDetailResponse.of(
                findPost,
                findPostMedias,
                userId,
                getPostLikeIdForView(findPost, member)
        );
    }

    public HotPostsResponse getHotPosts() {
        List<Post> hotPosts = postRepository.getHotPosts();
        List<HotPostDto> hots = HotPostDto.mapOf(hotPosts);
        return HotPostsResponse.from(hots);
    }

    public List<LatestCommunityPostDto> getLatestPostOfCommunity(Member member, Community community) {
        if (!community.canViewMember(member)) {
            return null;
        }
        List<Post> latestPostOfCommunity = postRepository.getLatestPostOfCommunity(community.getId());
        return LatestCommunityPostDto.listOf(latestPostOfCommunity);
    }

    public CommunityPostsResponse getPostsOfCommunity(Pageable pageable, Long communityId, Long userId) {
        Community community = communityRepository.findCommunityById(communityId);
        Member member = memberQueryService.getViewableMember(userId, community);

        Slice<Post> postPage = postRepository.getPostsOfCommunity(pageable, communityId);
        return CommunityPostsResponse.of(community.getCommunityName(), userId, postPage, member);
    }

    public Slice<SearchPostDto> getSearchedPosts(Pageable pageable, PostQueryRequest request, Long userId) {
        return postRepository.getSearchedPosts(pageable, request, userId);
    }

    public UserPostPageResponse getUserPosts(Long userId, Long sessionUserId, Pageable pageable) {
        List<Long> findMemberIds = getMemberIdsForQueryUserPost(userId, sessionUserId);
        Slice<Post> userPostPage = postRepository.getUserPostPageByMemberIds(findMemberIds, pageable);
        return UserPostPageResponse.from(userPostPage);
    }

    private List<Long> getMemberIdsForQueryUserPost(Long userId, Long sessionUserId) {
        if (sessionUserId.equals(userId)) {
            return memberRepository.findMemberIdsForQueryUserPost(sessionUserId);
        }
        userRepository.findUserById(userId);
        return memberRepository.findMemberIdsForQueryUserPost(userId, sessionUserId);
    }

    private Long getPostLikeIdForView(Post post, Member member) {
        return member.isNullMember() ? null : likeQueryService.getPostLikeId(post, member);
    }
}
