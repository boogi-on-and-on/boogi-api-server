package boogi.apiserver.domain.post.post.dao;

import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.request.PostQueryRequest;
import boogi.apiserver.domain.post.post.dto.dto.SearchPostDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Optional;

public interface PostRepositoryCustom {

    List<Post> getHotPosts();

    List<Post> getLatestPostOfCommunity(Long communityId);

    Optional<Post> getPostWithAll(Long postId);

    Slice<Post> getPostsOfCommunity(Pageable pageable, Long communityId);

    Slice<SearchPostDto> getSearchedPosts(Pageable pageable, PostQueryRequest request, Long userId);

    Slice<Post> getUserPostPageByMemberIds(List<Long> memberIds, Pageable pageable);
}
