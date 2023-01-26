package boogi.apiserver.domain.post.post.application;


import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.application.PostHashtagCoreService;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.like.application.LikeCoreService;
import boogi.apiserver.domain.like.application.LikeQueryService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.application.MemberValidationService;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.exception.HasNotDeleteAuthorityException;
import boogi.apiserver.domain.member.exception.HasNotUpdateAuthorityException;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.request.CreatePost;
import boogi.apiserver.domain.post.post.dto.request.UpdatePost;
import boogi.apiserver.domain.post.post.dto.response.PostDetail;
import boogi.apiserver.domain.post.post.dto.response.UserPostPage;
import boogi.apiserver.domain.post.post.exception.PostNotFoundException;
import boogi.apiserver.domain.post.postmedia.application.PostMediaQueryService;
import boogi.apiserver.domain.post.postmedia.dao.PostMediaRepository;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.post.postmedia.vo.PostMedias;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.global.error.exception.EntityNotFoundException;
import boogi.apiserver.global.webclient.push.MentionType;
import boogi.apiserver.global.webclient.push.SendPushNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;
    private final PostMediaRepository postMediaRepository;
    private final UserRepository userRepository;

    private final MemberValidationService memberValidationService;

    private final PostQueryService postQueryService;
    private final MemberQueryService memberQueryService;
    private final LikeQueryService likeQueryService;
    private final CommunityQueryService communityQueryService;
    private final PostMediaQueryService postMediaQueryService;

    private final PostHashtagCoreService postHashtagCoreService;
    private final LikeCoreService likeCoreService;

    private final SendPushNotification sendPushNotification;

    @Transactional
    public Post createPost(CreatePost createPost, Long userId) {
        Long communityId = createPost.getCommunityId();
        Community community = communityQueryService.getCommunity(communityId);
        Member member = memberQueryService.getJoinedMember(userId, communityId);

        Post savedPost = postRepository.save(
                Post.of(community, member, createPost.getContent())
        );

        postHashtagCoreService.addTags(savedPost.getId(), createPost.getHashtags());
        postMediaQueryService.getUnmappedPostMediasByUUID(createPost.getPostMediaIds()).stream()
                .forEach(pm -> pm.mapPost(savedPost));

        sendPushNotification.mentionNotification(
                createPost.getMentionedUserIds(),
                savedPost.getId(),
                MentionType.POST
        );

        return savedPost;
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

    @Transactional
    public Post updatePost(UpdatePost updatePost, Long postId, Long userId) {
        Post findPost = postQueryService.getPost(postId);
        communityQueryService.getCommunity(findPost.getCommunityId());

        if (canNotUpdatePost(userId, findPost)) {
            throw new HasNotUpdateAuthorityException();
        }

        postHashtagCoreService.removeTagsByPostId(postId);
        List<PostHashtag> newPostHashtags = postHashtagCoreService
                .addTags(postId, updatePost.getHashtags());

        findPost.updatePost(updatePost.getContent(), newPostHashtags);

        PostMedias findPostMedias = new PostMedias(
                postMediaRepository.findByPostId(postId)
        );

        List<String> postMediaIds = updatePost.getPostMediaIds();
        postMediaRepository.deleteAll(
                findPostMedias.excludedPostMedia(postMediaIds)
        );
        List<PostMedia> findNewPostMedias = postMediaQueryService.getUnmappedPostMediasByUUID(
                findPostMedias.newPostMediaIds(postMediaIds)
        );
        findNewPostMedias.stream().forEach(pm -> pm.mapPost(findPost));

        return findPost;
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post findPost = postRepository.getPostWithCommunityAndMemberByPostId(postId)
                .orElseThrow(PostNotFoundException::new);

        if (canNotDeletePost(userId, findPost)) {
            throw new HasNotDeleteAuthorityException();
        }

        Long findPostId = findPost.getId();
        postHashtagCoreService.removeTagsByPostId(findPostId);

        commentRepository.findByPostId(postId).stream()
                .forEach(c -> c.deleteComment());

        likeCoreService.removePostLikes(findPostId);

        List<PostMedia> postMedias = postMediaRepository.findByPostId(postId);
        postMediaRepository.deleteAllInBatch(postMedias);

        postRepository.delete(findPost);
    }

    public UserPostPage getUserPosts(Long userId, Long sessionUserId, Pageable pageable) {
        List<Long> findMemberIds;

        if (userId.equals(sessionUserId)) {
            findMemberIds = memberRepository.findMemberIdsForQueryUserPostBySessionUserId(sessionUserId);
        } else {
            userRepository.findUserById(userId).orElseThrow(() -> {
                throw new EntityNotFoundException("해당 유저가 존재하지 않습니다.");
            });

            findMemberIds = memberRepository.findMemberIdsForQueryUserPostByUserIdAndSessionUserId(userId, sessionUserId);
        }

        Slice<Post> userPostPage = postRepository.getUserPostPageByMemberIds(findMemberIds, pageable);

        return UserPostPage.of(userPostPage);
    }

    private boolean canNotUpdatePost(Long userId, Post findPost) {
        return !findPost.isAuthor(userId);
    }

    private boolean canNotDeletePost(Long userId, Post findPost) {
        return !findPost.isAuthor(userId)
                && !memberValidationService.hasSubManagerAuthority(userId, findPost.getCommunityId());
    }
}
