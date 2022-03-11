package boogi.apiserver.domain.post.post.dao;

import boogi.apiserver.domain.post.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {

    Page<Post> getUserPostPage(Pageable pageable, Long userId);
}
