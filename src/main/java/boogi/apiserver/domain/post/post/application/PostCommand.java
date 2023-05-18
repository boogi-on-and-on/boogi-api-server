package boogi.apiserver.domain.post.post.application;


import boogi.apiserver.domain.comment.repository.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.community.community.repository.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.like.application.LikeCommand;
import boogi.apiserver.domain.member.application.MemberQuery;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.exception.CanNotDeletePostException;
import boogi.apiserver.domain.member.exception.CanNotUpdatePostException;
import boogi.apiserver.domain.post.post.repository.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.request.CreatePostRequest;
import boogi.apiserver.domain.post.post.dto.request.UpdatePostRequest;
import boogi.apiserver.domain.post.postmedia.application.PostMediaQuery;
import boogi.apiserver.domain.post.postmedia.repository.PostMediaRepository;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PostCommand {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostMediaRepository postMediaRepository;
    private final CommunityRepository communityRepository;

    private final MemberQuery memberQuery;
    private final PostMediaQuery postMediaQuery;

    private final LikeCommand likeCommand;

    public Long createPost(CreatePostRequest request, Long userId) {
        Long communityId = request.getCommunityId();
        Community community = communityRepository.findCommunityById(communityId);
        Member member = memberQuery.getMember(userId, communityId);

        final Post newPost = Post.of(community, member, request.getContent());
        postRepository.save(newPost);

        newPost.addTags(request.getHashtags());

        List<PostMedia> unmappedPostMedias =
                postMediaQuery.getUnmappedPostMediasByUUID(request.getPostMediaIds());
        newPost.addPostMedias(unmappedPostMedias);

        return newPost.getId();
    }

    public Long updatePost(UpdatePostRequest request, Long postId, Long userId) {
        Post post = postRepository.findPostById(postId);

        validatePostUpdatable(userId, post);

        List<PostMedia> postMediaAll = postMediaRepository.findByUuidIn(request.getPostMediaIds());
        post.updatePost(request.getContent(), request.getHashtags(), postMediaAll);

        return post.getId();
    }

    public void deletePost(Long postId, Long userId) {
        final Post findPost = postRepository.findPostById(postId);

        validatePostDeletable(userId, findPost);

        deleteCommentsOnPost(postId);
        likeCommand.removePostLikes(findPost.getId());
        postRepository.delete(findPost);
    }

    //todo: repository로 내리기
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
        if (post.isAuthor(userId)) {
            return;
        }

        Member sessionMember = memberQuery.getMember(userId, post.getCommunityId());
        if (!sessionMember.isOperator()) {
            throw new CanNotDeletePostException();
        }
    }
}
