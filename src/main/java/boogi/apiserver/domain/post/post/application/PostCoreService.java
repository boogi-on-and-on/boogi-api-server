package boogi.apiserver.domain.post.post.application;


import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.community.community.application.CommunityValidationService;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.application.PostHashtagCoreService;
import boogi.apiserver.domain.like.application.LikeCoreService;
import boogi.apiserver.domain.like.dao.LikeRepository;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.member.application.MemberValidationService;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.exception.NotJoinedMemberException;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.PostDetail;
import boogi.apiserver.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostCoreService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    private final CommunityValidationService communityValidationService;
    private final MemberValidationService memberValidationService;

    private final PostHashtagCoreService postHashtagCoreService;
    private final LikeCoreService likeCoreService;


    //TODO: 이미지 업로드 추가
    @Transactional
    public Post createPost(Long userId, Long communityId, String content, List<String> tags) {
        Community community = communityValidationService.checkExistsCommunity(communityId);
        Member member = memberValidationService.checkMemberJoinedCommunity(userId, communityId);

        Post newPost = Post.of(community, member, content);
        postRepository.save(newPost);

        postHashtagCoreService.addTags(newPost.getId(), tags);

        return newPost;
    }

    public PostDetail getPostDetail(Long postId, Long userId) {
        PostDetail postDetail = postRepository.getPostDetailByPostId(postId)
                .orElseThrow(EntityNotFoundException::new);

        Long postedCommunityId = postDetail.getCommunity().getId();
        List<Member> findMemberResult = memberRepository.findByUserIdAndCommunityId(userId, postedCommunityId);
        Member member = (findMemberResult.isEmpty()) ? null : findMemberResult.get(0);

        if (communityValidationService.checkOnlyPrivateCommunity(postedCommunityId) && member == null) {
            throw new NotJoinedMemberException();
        }

        Long findLikeId;
        if (member == null) {   // public 커뮤니티에서 비가입 상태로 글 요청
            findLikeId = null;
        } else {    // public, private 커뮤니티에서 가입 상태로 글 요청
            Like findLike = likeRepository.
                    findPostLikeByPostIdAndMemberId(postId, member.getId())
                    .orElse(null);
            findLikeId = (findLike == null) ? null : findLike.getId();
        }
        postDetail.setLikeId(findLikeId);

        Long postUserId = postDetail.getUser().getId();
        postDetail.setMe(
                postUserId.equals(userId) ? Boolean.TRUE : Boolean.FALSE);

        return postDetail;
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post findPost = postRepository.getPostWithCommunityAndMemberByPostId(postId)
                .orElseThrow(EntityNotFoundException::new);

        Long findPostId = findPost.getId();
        Long postedCommunityId = findPost.getCommunity().getId();
        MemberType postMemberType = findPost.getMember().getMemberType();

        if (memberValidationService.hasAuth(userId, postedCommunityId, postMemberType)) {
            postHashtagCoreService.removeTagsByPostId(findPostId);

            List<Comment> findComments = commentRepository.findAllByPostId(postId);
            findComments.stream().forEach(c -> c.deleteComment());

            likeCoreService.removeAllPostLikes(findPostId);

            findPost.deletePost();
        }
    }
}
