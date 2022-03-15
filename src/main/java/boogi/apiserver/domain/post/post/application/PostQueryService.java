package boogi.apiserver.domain.post.post.application;

import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.UserPostPage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostQueryService {

    private final PostRepository postRepository;

    public UserPostPage getUserPosts(Pageable pageable, Long userId) {
        Page<Post> userPostPage = postRepository.getUserPostPage(pageable, userId);

        return UserPostPage.of(userPostPage);
    }
}
