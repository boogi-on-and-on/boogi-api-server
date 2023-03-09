package boogi.apiserver.domain.post.post.application;

import boogi.apiserver.domain.like.application.LikeQueryService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.dto.HotPostDto;
import boogi.apiserver.domain.post.post.dto.dto.LatestCommunityPostDto;
import boogi.apiserver.domain.post.post.dto.dto.SearchPostDto;
import boogi.apiserver.domain.post.post.dto.request.PostQueryRequest;
import boogi.apiserver.domain.post.post.dto.response.*;
import boogi.apiserver.domain.post.post.exception.PostNotFoundException;
import boogi.apiserver.domain.post.postmedia.dao.PostMediaRepository;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostQueryService {

    private final MemberRepository memberRepository;
    private final PostMediaRepository postMediaRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private final MemberQueryService memberQueryService;
    private final LikeQueryService likeQueryService;

    public PostDetailResponse getPostDetail(Long postId, Long userId) {
        Post findPost = postRepository.getPostWithAll(postId)
                .orElseThrow(PostNotFoundException::new);

        Member member = memberQueryService.getViewableMember(userId, findPost.getCommunity());
        List<PostMedia> findPostMedias = postMediaRepository.findByPostId(postId);

        return PostDetailResponse.of(
                findPost,
                findPostMedias,
                userId,
                getPostLikeIdForView(postId, member)
        );
    }

    public HotPostsResponse getHotPosts() {
        List<Post> hotPosts = postRepository.getHotPosts();
        List<HotPostDto> hots = HotPostDto.mapOf(hotPosts);
        return HotPostsResponse.from(hots);
    }

    public List<LatestCommunityPostDto> getLatestPostOfCommunity(Long communityId) {
        return postRepository.getLatestPostOfCommunity(communityId)
                .stream()
                .map(LatestCommunityPostDto::of)
                .collect(Collectors.toList());
    }

    public Slice<Post> getPostsOfCommunity(Pageable pageable, Long communityId) {
        return postRepository.getPostsOfCommunity(pageable, communityId);
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
        if (userId.equals(sessionUserId)) {
            return memberRepository.findMemberIdsForQueryUserPost(sessionUserId);
        }
        userRepository.findByUserId(userId);
        return memberRepository.findMemberIdsForQueryUserPost(userId, sessionUserId);
    }

    private Long getPostLikeIdForView(Long postId, Member member) {
        return member.isNullMember() ? null : likeQueryService.getPostLikeId(postId, member.getId());
    }
}
