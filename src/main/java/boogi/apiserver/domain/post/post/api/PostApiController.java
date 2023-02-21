package boogi.apiserver.domain.post.post.api;

import boogi.apiserver.domain.comment.application.CommentService;
import boogi.apiserver.domain.comment.dto.response.CommentsAtPostResponse;
import boogi.apiserver.domain.like.application.LikeService;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.like.dto.response.LikeMembersAtPostResponse;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.application.PostService;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.dto.SearchPostDto;
import boogi.apiserver.domain.post.post.dto.request.CreatePostRequest;
import boogi.apiserver.domain.post.post.dto.request.PostQueryRequest;
import boogi.apiserver.domain.post.post.dto.request.UpdatePostRequest;
import boogi.apiserver.domain.post.post.dto.response.HotPostsResponse;
import boogi.apiserver.domain.post.post.dto.response.PostDetailResponse;
import boogi.apiserver.domain.post.post.dto.response.SearchPostsResponse;
import boogi.apiserver.domain.post.post.dto.response.UserPostPageResponse;
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
    public SimpleIdResponse createPost(@Validated @RequestBody CreatePostRequest createPostRequest,
                                       @Session Long sessionUserId) {
        Post newPost = postService.createPost(createPostRequest, sessionUserId);

        return SimpleIdResponse.from(newPost.getId());
    }

    @GetMapping("/{postId}")
    public PostDetailResponse getPostDetail(@PathVariable Long postId, @Session Long sessionUserId) {
        return postQueryService.getPostDetail(postId, sessionUserId);
    }

    @PatchMapping("/{postId}")
    public SimpleIdResponse updatePost(@Validated @RequestBody UpdatePostRequest updatePostRequest,
                                       @PathVariable Long postId,
                                       @Session Long sessionUserId) {
        Post updatedPost = postService.updatePost(updatePostRequest, postId, sessionUserId);

        return SimpleIdResponse.from(updatedPost.getId());
    }

    @DeleteMapping("/{postId}")
    public void deletePost(@PathVariable Long postId, @Session Long sessionUserId) {
        postService.deletePost(postId, sessionUserId);
    }

    @GetMapping("/users")
    public UserPostPageResponse getUserPostsInfo(@RequestParam(required = false) Long userId,
                                                 @Session Long sessionUserId,
                                                 Pageable pageable) {
        Long infoUserid = Objects.requireNonNullElse(userId, sessionUserId);

        return postQueryService.getUserPosts(infoUserid, sessionUserId, pageable);
    }

    @GetMapping("/hot")
    public HotPostsResponse getHotPosts() {
        return postQueryService.getHotPosts();
    }

    @PostMapping("/{postId}/likes")
    public SimpleIdResponse doLikeAtPost(@PathVariable Long postId, @Session Long sessionUserId) {
        Like newLike = likeService.doLikeAtPost(postId, sessionUserId);

        return SimpleIdResponse.from(newLike.getId());
    }

    @GetMapping("/{postId}/likes")
    public LikeMembersAtPostResponse getLikeMembersAtPost(@PathVariable Long postId,
                                                          @Session Long sessionUserId,
                                                          Pageable pageable) {
        return likeService.getLikeMembersAtPost(postId, sessionUserId, pageable);
    }

    @GetMapping("/{postId}/comments")
    public CommentsAtPostResponse getCommentsAtPost(@PathVariable Long postId,
                                                    @Session Long sessionUserId,
                                                    Pageable pageable) {
        return commentService.getCommentsAtPost(postId, sessionUserId, pageable);
    }

    @GetMapping("/search")
    public SearchPostsResponse searchPosts(@ModelAttribute @Validated PostQueryRequest request,
                                           Pageable pageable,
                                           @Session Long sessionUserId) {
        Slice<SearchPostDto> page = postQueryService.getSearchedPosts(pageable, request, sessionUserId);

        return SearchPostsResponse.from(page);
    }
}