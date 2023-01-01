package boogi.apiserver.domain.like.application;

import boogi.apiserver.domain.like.dao.LikeRepository;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LikeQueryService {

    private final LikeRepository likeRepository;


    public Long getPostLikeIdForView(Long postId, Member member) {
        return member.isJoined() ? getPostLikeId(postId, member.getId()) : null;
    }

    public Long getPostLikeId(Long postId, Long memberId) {
        return likeRepository.findPostLikeByPostIdAndMemberId(postId, memberId)
                .map(Like::getId)
                .orElse(null);
    }
}
