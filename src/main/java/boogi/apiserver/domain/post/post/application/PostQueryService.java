package boogi.apiserver.domain.post.post.application;

import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.*;
import boogi.apiserver.global.error.exception.EntityNotFoundException;
import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostQueryService {

    private final PostRepository postRepository;

    public UserPostPage getUserPosts(Pageable pageable, Long userId) {
        Page<Post> userPostPage = postRepository.getUserPostPage(pageable, userId);

        return UserPostPage.of(userPostPage);
    }

    public List<HotPost> getHotPosts() {
        return postRepository.getHotPosts()
                .stream()
                .map(HotPost::of)
                .collect(Collectors.toList());
    }

    public Post getPost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(InvalidValueException::new);
        if (post.getCanceledAt() != null) {
            throw new EntityNotFoundException();
        }

        return post;
    }

    public List<Post> getLatestPostOfCommunity(Long communityId) {
        return postRepository.getLatestPostOfCommunity(communityId);
    }

    public Page<Post> getPostsOfCommunity(Pageable pageable, Long communityId) {
        return postRepository.getPostsOfCommunity(pageable, communityId);
    }

    public Page<SearchPostDto> getSearchedPosts(Pageable pageable, PostQueryRequest request, Long userId) {
        return postRepository.getSearchedPosts(pageable, request, userId);
    }
}
