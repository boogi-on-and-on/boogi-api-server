package boogi.apiserver.domain.post.post.api;

import boogi.apiserver.domain.comment.application.CommentService;
import boogi.apiserver.domain.comment.dto.response.CommentsAtPost;
import boogi.apiserver.domain.like.application.LikeService;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.like.dto.response.LikeMembersAtPost;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.application.PostService;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.request.CreatePost;
import boogi.apiserver.domain.post.post.dto.request.PostQueryRequest;
import boogi.apiserver.domain.post.post.dto.request.UpdatePost;
import boogi.apiserver.domain.post.post.dto.response.*;
import boogi.apiserver.global.argument_resolver.session.Session;
import boogi.apiserver.global.dto.SimpleIdResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

import static org.springframework.http.HttpStatus.CREATED;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/posts")
public class PostApiController {

    private final PostService postService;
    private final PostQueryService postQueryService;

    private final LikeService likeService;

    private final CommentService commentService;

    @PostMapping("/")
    @ResponseStatus(CREATED)
    public SimpleIdResponse createPost(@Validated @RequestBody CreatePost createPost,
                                       @Session Long sessionUserId) {
        Post newPost = postService.createPost(createPost, sessionUserId);

        return SimpleIdResponse.from(newPost.getId());
    }

    @GetMapping("/{postId}")
    public PostDetail getPostDetail(@PathVariable Long postId, @Session Long sessionUserId) {
        return postQueryService.getPostDetail(postId, sessionUserId);
    }

    @PatchMapping("/{postId}")
    public SimpleIdResponse updatePost(@Validated @RequestBody UpdatePost updatePost,
                                       @PathVariable Long postId,
                                       @Session Long sessionUserId) {
        Post updatedPost = postService.updatePost(updatePost, postId, sessionUserId);

        return SimpleIdResponse.from(updatedPost.getId());
    }

    @DeleteMapping("/{postId}")
    public void deletePost(@PathVariable Long postId, @Session Long sessionUserId) {
        postService.deletePost(postId, sessionUserId);
    }

    @GetMapping("/users")
    public UserPostPage getUserPostsInfo(@RequestParam(required = false) Long userId,
                                         @Session Long sessionUserId,
                                         Pageable pageable) {
        Long infoUserid = Objects.requireNonNullElse(userId, sessionUserId);

        return postQueryService.getUserPosts(infoUserid, sessionUserId, pageable);
    }

    @GetMapping("/hot")
    public HotPosts getHotPosts() {
        return postQueryService.getHotPosts();
    }

    @PostMapping("/{postId}/likes")
    public SimpleIdResponse doLikeAtPost(@PathVariable Long postId, @Session Long sessionUserId) {
        Like newLike = likeService.doLikeAtPost(postId, sessionUserId);

        return SimpleIdResponse.from(newLike.getId());
    }

    @GetMapping("/{postId}/likes")
    public LikeMembersAtPost getLikeMembersAtPost(@PathVariable Long postId,
                                                  @Session Long sessionUserId,
                                                  Pageable pageable) {
        return likeService.getLikeMembersAtPost(postId, sessionUserId, pageable);
    }

    @GetMapping("/{postId}/comments")
    public CommentsAtPost getCommentsAtPost(@PathVariable Long postId,
                                            @Session Long sessionUserId,
                                            Pageable pageable) {
        return commentService.getCommentsAtPost(postId, sessionUserId, pageable);
    }

    @GetMapping("/search")
    public SearchPosts searchPosts(@ModelAttribute @Validated PostQueryRequest request,
                                   Pageable pageable,
                                   @Session Long sessionUserId) {
        Slice<SearchPostDto> page = postQueryService.getSearchedPosts(pageable, request, sessionUserId);

        return SearchPosts.from(page);
    }
}