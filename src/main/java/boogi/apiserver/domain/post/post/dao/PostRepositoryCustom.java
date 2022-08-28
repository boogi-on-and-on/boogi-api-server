package boogi.apiserver.domain.post.post.dao;

import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.request.PostQueryRequest;
import boogi.apiserver.domain.post.post.dto.response.SearchPostDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Optional;

public interface PostRepositoryCustom {

    Slice<Post> getUserPostPage(Pageable pageable, Long userId);

    List<Post> getHotPosts();

    List<Post> getLatestPostOfCommunity(Long communityId);

    Optional<Post> getPostWithUserAndMemberAndCommunityByPostId(Long postId);

    Slice<Post> getPostsOfCommunity(Pageable pageable, Long communityId);

    Optional<Post> getPostWithCommunityAndMemberByPostId(Long postId);

    Slice<SearchPostDto> getSearchedPosts(Pageable pageable, PostQueryRequest request, Long userId);

    Optional<Post> findPostById(Long postId);

    Slice<Post> getUserPostPageByMemberIds(List<Long> memberIds, Pageable pageable);
}
