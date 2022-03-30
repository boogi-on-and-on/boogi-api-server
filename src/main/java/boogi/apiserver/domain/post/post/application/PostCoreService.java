package boogi.apiserver.domain.post.post.application;


import boogi.apiserver.domain.community.community.application.CommunityValidationService;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.application.PostHashtagCoreService;
import boogi.apiserver.domain.member.application.MemberValidationService;
import boogi.apiserver.domain.member.domain.Member;
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

    private final CommunityValidationService communityValidationService;
    private final MemberValidationService memberValidationService;

    private final PostHashtagCoreService postHashtagCoreService;

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
        if (communityValidationService.checkOnlyPrivateCommunity(postedCommunityId)) {
            memberValidationService.checkMemberJoinedCommunity(userId, postedCommunityId);
        }

        Long postUserId = postDetail.getUser().getId();
        postDetail.setMe(
                postUserId.equals(userId) ? Boolean.TRUE : Boolean.FALSE);

        return postDetail;
    }
}
