package boogi.apiserver.domain.post.post.application;


import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.application.PostHashtagService;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.like.application.LikeCommandService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.application.MemberValidationService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.exception.HasNotDeleteAuthorityException;
import boogi.apiserver.domain.member.exception.HasNotUpdateAuthorityException;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.request.CreatePostRequest;
import boogi.apiserver.domain.post.post.dto.request.UpdatePostRequest;
import boogi.apiserver.domain.post.post.exception.PostNotFoundException;
import boogi.apiserver.domain.post.postmedia.application.PostMediaQueryService;
import boogi.apiserver.domain.post.postmedia.dao.PostMediaRepository;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.post.postmedia.vo.PostMedias;
import boogi.apiserver.global.webclient.push.MentionType;
import boogi.apiserver.global.webclient.push.SendPushNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PostCommandService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostMediaRepository postMediaRepository;
    private final CommunityRepository communityRepository;

    private final MemberValidationService memberValidationService;

    private final PostQueryService postQueryService;
    private final MemberQueryService memberQueryService;
    private final CommunityQueryService communityQueryService;
    private final PostMediaQueryService postMediaQueryService;

    private final PostHashtagService postHashtagService;
    private final LikeCommandService likeCommandService;

    private final SendPushNotification sendPushNotification;

    public Post createPost(CreatePostRequest createPostRequest, Long userId) {
        Long communityId = createPostRequest.getCommunityId();
        Community community = communityRepository.findByCommunityId(communityId);
        Member member = memberQueryService.getJoinedMember(userId, communityId);

        Post savedPost = postRepository.save(
                Post.of(community, member, createPostRequest.getContent())
        );

        postHashtagService.addTags(savedPost.getId(), createPostRequest.getHashtags());
        PostMedias unmappedPostMedias = postMediaQueryService
                .getUnmappedPostMediasByUUID(createPostRequest.getPostMediaIds());
        unmappedPostMedias.mapPost(savedPost);

        sendPushNotification.mentionNotification(
                createPostRequest.getMentionedUserIds(),
                savedPost.getId(),
                MentionType.POST
        );

        return savedPost;
    }

    public Post updatePost(UpdatePostRequest updatePostRequest, Long postId, Long userId) {
        Post findPost = postRepository.findByPostId(postId);
        communityRepository.findByCommunityId(findPost.getCommunityId());

        if (canNotUpdatePost(userId, findPost)) {
            throw new HasNotUpdateAuthorityException();
        }

        postHashtagService.removeTagsByPostId(postId);
        List<PostHashtag> newPostHashtags = postHashtagService
                .addTags(postId, updatePostRequest.getHashtags());

        //추후 수정하기
        findPost.updatePost(updatePostRequest.getContent(), updatePostRequest.getHashtags());

        PostMedias findPostMedias = new PostMedias(
                postMediaRepository.findByPostId(postId)
        );

        List<String> postMediaIds = updatePostRequest.getPostMediaIds();
        postMediaRepository.deleteAll(
                findPostMedias.excludedPostMedia(postMediaIds)
        );
        PostMedias findNewPostMedias = postMediaQueryService.getUnmappedPostMediasByUUID(
                findPostMedias.newPostMediaIds(postMediaIds)
        );
        findNewPostMedias.mapPost(findPost);

        return findPost;
    }

    public void deletePost(Long postId, Long userId) {
        Post findPost = postRepository.getPostWithCommunityAndMemberByPostId(postId)
                .orElseThrow(PostNotFoundException::new);

        if (canNotDeletePost(userId, findPost)) {
            throw new HasNotDeleteAuthorityException();
        }

        Long findPostId = findPost.getId();
        postHashtagService.removeTagsByPostId(findPostId);

        commentRepository.findByPostId(postId).stream()
                .forEach(Comment::deleteComment);

        likeCommandService.removePostLikes(findPostId);

        List<PostMedia> postMedias = postMediaRepository.findByPostId(postId);
        postMediaRepository.deleteAllInBatch(postMedias);

        postRepository.delete(findPost);
    }

    private boolean canNotUpdatePost(Long userId, Post findPost) {
        return !findPost.isAuthor(userId);
    }

    private boolean canNotDeletePost(Long userId, Post findPost) {
        return !findPost.isAuthor(userId)
                && !memberValidationService.hasSubManagerAuthority(userId, findPost.getCommunityId());
    }
}
