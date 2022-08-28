package boogi.apiserver.domain.post.post.api;

import boogi.apiserver.domain.comment.application.CommentCoreService;
import boogi.apiserver.domain.comment.dto.response.CommentsAtPost;
import boogi.apiserver.domain.like.application.LikeCoreService;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.like.dto.response.LikeMembersAtPost;
import boogi.apiserver.domain.post.post.application.PostCoreService;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.request.CreatePost;
import boogi.apiserver.domain.post.post.dto.request.PostQueryRequest;
import boogi.apiserver.domain.post.post.dto.request.UpdatePost;
import boogi.apiserver.domain.post.post.dto.response.HotPost;
import boogi.apiserver.domain.post.post.dto.response.PostDetail;
import boogi.apiserver.domain.post.post.dto.response.SearchPostDto;
import boogi.apiserver.domain.post.post.dto.response.UserPostPage;
import boogi.apiserver.global.argument_resolver.session.Session;
import boogi.apiserver.global.dto.PaginationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/posts")
public class PostApiController {

    private final PostCoreService postCoreService;
    private final PostQueryService postQueryService;

    private final LikeCoreService likeCoreService;

    private final CommentCoreService commentCoreService;

    @PostMapping("/")
    public ResponseEntity<Object> createPost(@Validated @RequestBody CreatePost createPost, @Session Long userId) {
        Post newPost = postCoreService.createPost(createPost, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", newPost.getId()
        ));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDetail> getPostDetail(@PathVariable Long postId, @Session Long userId) {
        PostDetail postDetail = postCoreService.getPostDetail(postId, userId);

        return ResponseEntity.ok().body(postDetail);
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<Object> updatePost(@Validated @RequestBody UpdatePost updatePost,
                                             @PathVariable Long postId,
                                             @Session Long userId) {
        Post updatedPost = postCoreService.updatePost(updatePost, postId, userId);

        return ResponseEntity.ok().body(Map.of(
                "id", updatedPost.getId()
        ));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Object> deletePost(@PathVariable Long postId, @Session Long userId) {
        postCoreService.deletePost(postId, userId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/users")
    public ResponseEntity<UserPostPage> getUserPostsInfo(@RequestParam(required = false) Long userId,
                                                         @Session Long sessionUserId,
                                                         Pageable pageable) {
        Long id = Objects.requireNonNullElse(userId, sessionUserId);
//        UserPostPage userPostsPage = postQueryService.getUserPosts(pageable, id);
        UserPostPage userPostsPage = postCoreService.getUserPosts(id, sessionUserId, pageable);

        return ResponseEntity.ok().body(userPostsPage);
    }

    @GetMapping("/hot")
    public ResponseEntity<Object> getHotPosts() {
        List<HotPost> hotPosts = postQueryService.getHotPosts();

        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "hots", hotPosts
        ));
    }

    @PostMapping("/{postId}/likes")
    public ResponseEntity<Object> doLikeAtPost(@PathVariable Long postId, @Session Long userId) {
        Like newLike = likeCoreService.doLikeAtPost(postId, userId);

        return ResponseEntity.ok().body(Map.of(
                "id", newLike.getId()
        ));
    }

    @GetMapping("/{postId}/likes")
    public ResponseEntity<Object> getLikeMembersAtPost(@PathVariable Long postId, @Session Long userId, Pageable pageable) {
        LikeMembersAtPost likeMembersAtPost = likeCoreService.getLikeMembersAtPost(postId, userId, pageable);

        return ResponseEntity.ok().body(likeMembersAtPost);
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<Object> getCommentsAtPost(@PathVariable Long postId, @Session Long userId, Pageable pageable) {
        CommentsAtPost commentsAtPost = commentCoreService.getCommentsAtPost(postId, userId, pageable);

        return ResponseEntity.ok().body(commentsAtPost);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchPosts(@ModelAttribute @Validated PostQueryRequest request,
                                              Pageable pageable,
                                              @Session Long userId) {
        Slice<SearchPostDto> page = postQueryService.getSearchedPosts(pageable, request, userId);
        PaginationDto pageInfo = PaginationDto.of(page);
        List<SearchPostDto> dtos = page.getContent();

        return ResponseEntity.ok(Map.of(
                "posts", dtos,
                "pageInfo", pageInfo
        ));
    }
}