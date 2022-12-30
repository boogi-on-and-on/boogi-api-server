package boogi.apiserver.domain.post.post.api;

import boogi.apiserver.domain.comment.application.CommentCoreService;
import boogi.apiserver.domain.comment.dto.response.CommentsAtPost;
import boogi.apiserver.domain.like.application.LikeCoreService;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.like.dto.response.LikeMembersAtPost;
import boogi.apiserver.domain.post.post.application.PostService;
import boogi.apiserver.domain.post.post.application.PostQueryService;
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
import static org.springframework.http.HttpStatus.OK;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/posts")
public class PostApiController {

    private final PostService postService;
    private final PostQueryService postQueryService;

    private final LikeCoreService likeCoreService;

    private final CommentCoreService commentCoreService;

    @PostMapping("/")
    @ResponseStatus(CREATED)
    public SimpleIdResponse createPost(@Validated @RequestBody CreatePost createPost,
                                       @Session Long sessionUserId) {
        Post newPost = postService.createPost(createPost, sessionUserId);

        return new SimpleIdResponse(newPost.getId());
    }

    @GetMapping("/{postId}")
    @ResponseStatus(OK)
    public PostDetail getPostDetail(@PathVariable Long postId, @Session Long sessionUserId) {
        return postService.getPostDetail(postId, sessionUserId);
    }

    @PatchMapping("/{postId}")
    @ResponseStatus(OK)
    public SimpleIdResponse updatePost(@Validated @RequestBody UpdatePost updatePost,
                                       @PathVariable Long postId,
                                       @Session Long sessionUserId) {
        Post updatedPost = postService.updatePost(updatePost, postId, sessionUserId);

        return new SimpleIdResponse(updatedPost.getId());
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(OK)
    public void deletePost(@PathVariable Long postId, @Session Long sessionUserId) {
        postService.deletePost(postId, sessionUserId);
    }

    @GetMapping("/users")
    @ResponseStatus(OK)
    public UserPostPage getUserPostsInfo(@RequestParam(required = false) Long userId,
                                         @Session Long sessionUserId,
                                         Pageable pageable) {
        Long infoUserid = Objects.requireNonNullElse(userId, sessionUserId);

        return postService.getUserPosts(infoUserid, sessionUserId, pageable);
    }

    @GetMapping("/hot")
    @ResponseStatus(OK)
    public HotPosts getHotPosts() {
        return new HotPosts(postQueryService.getHotPosts());
    }

    @PostMapping("/{postId}/likes")
    @ResponseStatus(OK)
    public SimpleIdResponse doLikeAtPost(@PathVariable Long postId, @Session Long sessionUserId) {
        Like newLike = likeCoreService.doLikeAtPost(postId, sessionUserId);

        return new SimpleIdResponse(newLike.getId());
    }

    @GetMapping("/{postId}/likes")
    @ResponseStatus(OK)
    public LikeMembersAtPost getLikeMembersAtPost(@PathVariable Long postId,
                                                  @Session Long sessionUserId,
                                                  Pageable pageable) {
        return likeCoreService.getLikeMembersAtPost(postId, sessionUserId, pageable);
    }

    @GetMapping("/{postId}/comments")
    @ResponseStatus(OK)
    public CommentsAtPost getCommentsAtPost(@PathVariable Long postId,
                                            @Session Long sessionUserId,
                                            Pageable pageable) {
        return commentCoreService.getCommentsAtPost(postId, sessionUserId, pageable);
    }

    @GetMapping("/search")
    @ResponseStatus(OK)
    public SearchPosts searchPosts(@ModelAttribute @Validated PostQueryRequest request,
                                   Pageable pageable,
                                   @Session Long sessionUserId) {
        Slice<SearchPostDto> page = postQueryService.getSearchedPosts(pageable, request, sessionUserId);

        return SearchPosts.from(page);
    }
}