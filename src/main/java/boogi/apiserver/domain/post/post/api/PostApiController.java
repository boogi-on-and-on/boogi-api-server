package boogi.apiserver.domain.post.post.api;

import boogi.apiserver.domain.comment.application.CommentCoreService;
import boogi.apiserver.domain.comment.dto.CommentsAtPost;
import boogi.apiserver.domain.like.application.LikeCoreService;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.like.dto.LikeMembersAtPost;
import boogi.apiserver.domain.post.post.application.PostCoreService;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.CreatePost;
import boogi.apiserver.domain.post.post.dto.HotPost;
import boogi.apiserver.domain.post.post.dto.PostDetail;
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

    private final LikeCoreService likeCoreService;

    private final CommentCoreService commentCoreService;

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

    @DeleteMapping("/{postId}")
    public ResponseEntity<Object> deletePost(@PathVariable Long postId, @Session Long userId) {
        postCoreService.deletePost(postId, userId);

        return ResponseEntity.ok().build();
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
}