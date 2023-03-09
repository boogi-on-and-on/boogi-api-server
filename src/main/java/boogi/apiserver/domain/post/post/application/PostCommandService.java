package boogi.apiserver.domain.post.post.application;


import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.exception.CanNotDeleteCommentException;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.like.application.LikeCommandService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.domain.Member;
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

    private final MemberQueryService memberQueryService;
    private final PostMediaQueryService postMediaQueryService;

    private final LikeCommandService likeCommandService;

    public Post createPost(CreatePostRequest request, Long userId) {
        Long communityId = request.getCommunityId();
        Community community = communityRepository.findByCommunityId(communityId);
        Member member = memberQueryService.getMemberOfTheCommunity(userId, communityId);

        final Post newPost = Post.of(community, member, request.getContent());
        postRepository.save(newPost);

        newPost.addTags(request.getHashtags());

        List<PostMedia> unmappedPostMedias =
                postMediaQueryService.getUnmappedPostMediasByUUID(request.getPostMediaIds());
        newPost.addPostMedias(unmappedPostMedias);

        return newPost;
    }

    public Post updatePost(UpdatePostRequest request, Long postId, Long userId) {
        Post findPost = postRepository.findByPostId(postId);
        communityRepository.findByCommunityId(findPost.getCommunityId());

        validatePostUpdatable(userId, findPost);

        List<PostMedia> postMediaAll = postMediaRepository.findByUuidIn(request.getPostMediaIds());
        findPost.updatePost(request.getContent(), request.getHashtags(), postMediaAll);

        return findPost;
    }

    public void deletePost(Long postId, Long userId) {
        final Post findPost = postRepository.findByPostId(postId);

        validatePostDeletable(userId, findPost);

        deleteCommentsOnPost(postId);
        likeCommandService.removePostLikes(findPost.getId());
        postRepository.delete(findPost);
    }

    private void deleteCommentsOnPost(Long postId) {
        commentRepository.findByPostId(postId)
                .forEach(Comment::deleteComment);
    }

    private void validatePostUpdatable(Long userId, Post post) {
        if (!post.isAuthor(userId)) {
            throw new CanNotUpdatePostException();
        }
    }

    private void validatePostDeletable(Long userId, Post post) {
        if (!post.isAuthor(userId) && !post.getMember().isOperator()) {
            throw new CanNotDeleteCommentException();
        }
    }
}
