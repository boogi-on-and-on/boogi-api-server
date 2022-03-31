package boogi.apiserver.domain.post.post.application;


import boogi.apiserver.domain.community.community.application.CommunityValidationService;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.application.PostHashtagCoreService;
import boogi.apiserver.domain.member.application.MemberValidationService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
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

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post findPost = postRepository.getPostWithCommunityAndMemberByPostId(postId)
                .orElseThrow(EntityNotFoundException::new);

        Long postedCommunityId = findPost.getCommunity().getId();
        MemberType postMemberType = findPost.getMember().getMemberType();
        if (memberValidationService.hasAuth(userId, postedCommunityId, postMemberType)) {
            postHashtagCoreService.removeTagsByPostId(findPost.getId());
            // TODO: 댓글 soft delete -> comment 구현 후 추가
            // TODO: 좋아요 hard delete -> Like 구현 후 추가
            findPost.deletePost();
        }
    }
}
