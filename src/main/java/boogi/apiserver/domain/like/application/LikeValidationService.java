package boogi.apiserver.domain.like.application;

import boogi.apiserver.domain.like.dao.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LikeValidationService {

    private final LikeRepository likeRepository;

    public boolean checkOnlyAlreadyDoPostLike(Long postId, Long memberId) {
        return likeRepository.existsLikeByPostIdAndMemberId(postId, memberId);
    }

    public boolean checkOnlyAlreadyDoCommentLike(Long commentId, Long memberId) {
        return likeRepository.existsLikeByCommentIdAndMemberId(commentId, memberId);
    }
}
