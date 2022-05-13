package boogi.apiserver.domain.post.post.dao;

import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.PostQueryRequest;
import boogi.apiserver.domain.post.post.dto.SearchPostDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PostRepositoryCustom {

    Page<Post> getUserPostPage(Pageable pageable, Long userId);

    List<Post> getHotPosts();

    List<Post> getLatestPostOfUserJoinedCommunities(Long userId);

    List<Post> getLatestPostOfCommunity(Long communityId);

    Optional<Post> getPostWithUserAndMemberAndCommunityByPostId(Long postId);

    Page<Post> getPostsOfCommunity(Pageable pageable, Long communityId);

    Optional<Post> getPostWithCommunityAndMemberByPostId(Long postId);

    Page<SearchPostDto> getSearchedPosts(Pageable pageable, PostQueryRequest request, Long userId);
}
