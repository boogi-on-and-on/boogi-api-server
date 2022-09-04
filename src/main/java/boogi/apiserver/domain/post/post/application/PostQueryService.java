package boogi.apiserver.domain.post.post.application;

import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.request.PostQueryRequest;
import boogi.apiserver.domain.post.post.dto.response.HotPost;
import boogi.apiserver.domain.post.post.dto.response.SearchPostDto;
import boogi.apiserver.domain.post.post.dto.response.UserPostPage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostQueryService {

    private final PostRepository postRepository;

    public UserPostPage getUserPosts(Pageable pageable, Long userId) {
        Slice<Post> userPostSlice = postRepository.getUserPostPage(pageable, userId);

        return UserPostPage.of(userPostSlice);
    }

    public List<HotPost> getHotPosts() {
        return postRepository.getHotPosts()
                .stream()
                .map(HotPost::of)
                .collect(Collectors.toList());
    }

    public Post getPost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(EntityNotFoundException::new);
        return post;
    }

    public List<Post> getLatestPostOfCommunity(Long communityId) {
        return postRepository.getLatestPostOfCommunity(communityId);
    }

    public Slice<Post> getPostsOfCommunity(Pageable pageable, Long communityId) {
        return postRepository.getPostsOfCommunity(pageable, communityId);
    }

    public Slice<SearchPostDto> getSearchedPosts(Pageable pageable, PostQueryRequest request, Long userId) {
        return postRepository.getSearchedPosts(pageable, request, userId);
    }
}
