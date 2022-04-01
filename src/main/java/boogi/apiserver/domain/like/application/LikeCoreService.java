package boogi.apiserver.domain.like.application;

import boogi.apiserver.domain.like.dao.LikeRepository;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.like.exception.AlreadyDoLikeException;
import boogi.apiserver.domain.member.application.MemberValidationService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.exception.NotAuthorizedMemberException;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LikeCoreService {

    private final LikeRepository likeRepository;

    private final PostQueryService postQueryService;

    private final LikeValidationService likeValidationService;
    private final MemberValidationService memberValidationService;

    public List<Like> getPostLikes(Long postId) {
        List<Like> findPostLikes = likeRepository.findPostLikesByPostId(postId);
        if (findPostLikes.isEmpty()) {
            throw new EntityNotFoundException();
        }

        return findPostLikes;
    }

    @Transactional
    public Like doLikeAtPost(Long postId, Long userId) {
        Post findPost = postQueryService.getPost(postId);

        Member member = memberValidationService.checkMemberJoinedCommunity(userId, findPost.getCommunity().getId());

        if (likeValidationService.checkOnlyAlreadyDoPostLike(postId, member.getId())) {
            throw new AlreadyDoLikeException("이미 해당 글에 좋아요를 한 상태입니다");
        }

        Like newLike = Like.postOf(findPost, member);
        return likeRepository.save(newLike);
    }

    @Transactional
    public Like doLikeAtComment(Long commentId, Long userId) {
        // TODO: Comment 구현 후 추가
        throw new RuntimeException("구현안함");
    }

    @Transactional
    public void doUnlike(Long likeId, Long userId) {
        Like findLike = likeRepository.findPostLikeWithMemberByLikeId(likeId)
                .orElseThrow(EntityNotFoundException::new);

        Long DidLikedUser = findLike.getMember().getUser().getId();
        if (DidLikedUser.equals(userId) == false) {
            throw new NotAuthorizedMemberException("요청한 유저가 좋아요를 한 유저와 다릅니다");
        }

        likeRepository.delete(findLike);
    }

    @Transactional
    public void removeAllPostLikes(Long postId) {
        likeRepository.deleteAllPostLikeByPostId(postId);
    }

    @Transactional
    public void removeAllCommentLikes(Long commentId) {
        likeRepository.deleteAllCommentLikeByCommentId(commentId);
    }
}
