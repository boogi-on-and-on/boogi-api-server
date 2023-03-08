package boogi.apiserver.domain.post.post.application;


import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.application.PostHashtagCommandService;
import boogi.apiserver.domain.like.application.LikeCommandService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.application.MemberValidationService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.exception.CanNotDeletePostException;
import boogi.apiserver.domain.member.exception.CanNotUpdatePostException;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.request.CreatePostRequest;
import boogi.apiserver.domain.post.post.dto.request.UpdatePostRequest;
import boogi.apiserver.domain.post.postmedia.application.PostMediaQueryService;
import boogi.apiserver.domain.post.postmedia.dao.PostMediaRepository;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
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

    private final MemberQueryService memberQueryService;
    private final PostMediaQueryService postMediaQueryService;

    private final PostHashtagCommandService postHashtagCommandService;
    private final LikeCommandService likeCommandService;

    public Post createPost(CreatePostRequest request, Long userId) {
        Long communityId = request.getCommunityId();
        Community community = communityRepository.findByCommunityId(communityId);
        Member member = memberQueryService.getJoinedMember(userId, communityId);

        final Post newPost = Post.of(community, member, request.getContent());
        postRepository.save(newPost);

        postHashtagCommandService.addTags(newPost.getId(), request.getHashtags());

        final List<PostMedia> unmappedPostMedias =
                postMediaQueryService.getUnmappedPostMedias(request.getPostMediaIds());
        newPost.addPostMedias(unmappedPostMedias);

        return newPost;
    }

    public Post updatePost(UpdatePostRequest request, Long postId, Long userId) {
        Post findPost = postRepository.findByPostId(postId);
        communityRepository.findByCommunityId(findPost.getCommunityId());

        if (canNotUpdatePost(userId, findPost)) {
            throw new CanNotUpdatePostException();
        }

        List<PostMedia> postMediaAll = postMediaRepository.findByUuidIn(request.getPostMediaIds());
        findPost.updatePost(request.getContent(), request.getHashtags(), postMediaAll);

        return findPost;
    }

    public void deletePost(Long postId, Long userId) {
        final Post findPost = postRepository.findByPostId(postId);

        if (canNotDeletePost(userId, findPost)) {
            throw new CanNotDeletePostException();
        }

        deleteCommentsOnPost(postId);
        likeCommandService.removePostLikes(findPost.getId());
        postRepository.delete(findPost);
    }

    private void deleteCommentsOnPost(final Long postId) {
        commentRepository.findByPostId(postId)
                .forEach(Comment::deleteComment);
    }

    private boolean canNotUpdatePost(Long userId, Post findPost) {
        return !findPost.isAuthor(userId);
    }

    private boolean canNotDeletePost(Long userId, Post findPost) {
        return !findPost.isAuthor(userId)
                && !memberValidationService.hasSubManagerAuthority(userId, findPost.getCommunityId());
    }
}
