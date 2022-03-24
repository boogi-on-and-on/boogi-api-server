package boogi.apiserver.domain.post.post.api;

import boogi.apiserver.domain.post.post.application.PostCoreService;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.CreatePost;
import boogi.apiserver.domain.post.post.dto.HotPost;
import boogi.apiserver.domain.post.post.dto.UserPostPage;
import boogi.apiserver.global.argument_resolver.session.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/posts")
public class PostApiController {

    private final PostCoreService postCoreService;
    private final PostQueryService postQueryService;

    @PostMapping("/")
    public ResponseEntity<Object> createPost(@Validated @RequestBody CreatePost createPost, @Session Long userId) {
        Post newPost = postCoreService.createPost(userId, createPost.getCommunityId(), createPost.getContent(), createPost.getHashtags());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", newPost.getId()
        ));
    }

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