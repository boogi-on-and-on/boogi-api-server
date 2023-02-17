package boogi.apiserver.domain.post.post.application;

import boogi.apiserver.domain.like.application.LikeQueryService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
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

    public Post getPost(Long postId) {
        if (postId == null) {
            throw new IllegalArgumentException("postId는 null일 수 없습니다.");
        }
        return postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);
    }

    public PostDetail getPostDetail(Long postId, Long userId) {
        Post findPost = postRepository.getPostWithUserAndMemberAndCommunityByPostId(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 글이 존재하지 않거나, 해당 글이 작성된 커뮤니티가 존재하지 않습니다"));

        Member member = memberQueryService.getViewableMember(userId, findPost.getCommunity());
        List<PostMedia> findPostMedias = postMediaRepository.findByPostId(postId);

        return new PostDetail(
                findPost,
                findPostMedias,
                userId,
                likeQueryService.getPostLikeIdForView(postId, member)
        );
    }

    public HotPosts getHotPosts() {
        List<Post> hotPosts = postRepository.getHotPosts();
        List<HotPost> hots = HotPost.mapOf(hotPosts);
        return HotPosts.from(hots);
    }

    public List<Post> getLatestPostOfCommunity(Long communityId) {
        return postRepository.getLatestPostOfCommunity(communityId);
    }

    public Slice<Post> getPostsOfCommunity(Pageable pageable, Long communityId) {
        return postRepository.getPostsOfCommunity(pageable, communityId);
    }

    public Slice<SearchPostDto> getSearchedPosts(Pageable pageable, PostQueryRequest request, Long userId) {
        return postRepository.getSearchedPosts(pageable, request, userId);
    }

    public UserPostPage getUserPosts(Long userId, Long sessionUserId, Pageable pageable) {
        List<Long> findMemberIds = getMemberIdsForQueryUserPost(userId, sessionUserId);
        Slice<Post> userPostPage = postRepository.getUserPostPageByMemberIds(findMemberIds, pageable);
        return UserPostPage.from(userPostPage);
    }

    private List<Long> getMemberIdsForQueryUserPost(Long userId, Long sessionUserId) {
        if (userId.equals(sessionUserId)) {
            return memberRepository.findMemberIdsForQueryUserPost(sessionUserId);
        }
        userRepository.findByUserId(userId);
        return memberRepository.findMemberIdsForQueryUserPost(userId, sessionUserId);
    }
}
