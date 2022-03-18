package boogi.apiserver.domain.post.post.api;

import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.dto.HotPost;
import boogi.apiserver.domain.post.post.dto.UserPostPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/posts")
public class PostApiController {

    private final PostQueryService postQueryService;

    @GetMapping(value = "/user/{userId}")
    public ResponseEntity<UserPostPage> getUserPostsInfo(@PathVariable Long userId, Pageable pageable) {
        UserPostPage userPostsPage = postQueryService.getUserPosts(pageable, userId);

        return ResponseEntity.ok().body(userPostsPage);

    }

    @GetMapping("/hot")
    public ResponseEntity<Object> getHotPosts() {
        List<HotPost> hotPosts = postQueryService.getHotPosts();
        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "hots", hotPosts
        ));
    }
}