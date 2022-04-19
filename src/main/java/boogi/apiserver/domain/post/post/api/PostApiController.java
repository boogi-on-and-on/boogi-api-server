package boogi.apiserver.domain.post.post.api;

import boogi.apiserver.domain.post.post.application.PostCoreService;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.*;
import boogi.apiserver.global.argument_resolver.session.Session;
import boogi.apiserver.global.dto.PagnationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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

    @GetMapping("/{postId}")
    public ResponseEntity<PostDetail> getPostDetail(@PathVariable Long postId, @Session Long userId) {
        PostDetail postDetail = postCoreService.getPostDetail(postId, userId);

        return ResponseEntity.ok().body(postDetail);
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

    @GetMapping("/search")
    public ResponseEntity<Object> searchPosts(@ModelAttribute @Validated PostQueryRequest request, Pageable pageable) {
        Page<SearchPostDto> page = postQueryService.getSearchedPosts(pageable, request);
        PagnationDto pageInfo = PagnationDto.of(page);
        List<SearchPostDto> dtos = page.getContent();

        return ResponseEntity.ok(Map.of(
                "posts", dtos,
                "pageInfo", pageInfo
        ));
    }
}