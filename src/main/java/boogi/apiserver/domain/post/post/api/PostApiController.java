package boogi.apiserver.domain.post.post.api;

import boogi.apiserver.domain.comment.application.CommentQueryService;
import boogi.apiserver.domain.comment.dto.response.CommentsAtPostResponse;
import boogi.apiserver.domain.like.application.LikeCommandService;
import boogi.apiserver.domain.like.application.LikeQueryService;
import boogi.apiserver.domain.like.dto.response.LikeMembersAtPostResponse;
import boogi.apiserver.domain.post.post.application.PostCommandService;
import boogi.apiserver.domain.post.post.application.PostQueryService;
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
import boogi.apiserver.global.webclient.push.MentionType;
import boogi.apiserver.global.webclient.push.SendPushNotification;
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

    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;

    private final LikeCommandService likeCommandService;
    private final LikeQueryService likeQueryService;

    private final CommentQueryService commentQueryService;

    private final SendPushNotification sendPushNotification;

    @PostMapping("/")
    @ResponseStatus(CREATED)
    public SimpleIdResponse createPost(@Validated @RequestBody CreatePostRequest request,
                                       @Session Long sessionUserId) {
        Long newPostId = postCommandService.createPost(request, sessionUserId);

        sendPushNotification.mentionNotification(
                request.getMentionedUserIds(),
                newPostId,
                MentionType.POST
        );

        return SimpleIdResponse.from(newPostId);
    }

    @PatchMapping("/{postId}")
    public SimpleIdResponse updatePost(@Validated @RequestBody UpdatePostRequest request,
                                       @PathVariable Long postId,
                                       @Session Long sessionUserId) {
        Long updatedPostId = postCommandService.updatePost(request, postId, sessionUserId);

        return SimpleIdResponse.from(updatedPostId);
    }

    @GetMapping("/{postId}")
    public PostDetailResponse getPostDetail(@PathVariable Long postId, @Session Long sessionUserId) {
        return postQueryService.getPostDetail(postId, sessionUserId);
    }

    @DeleteMapping("/{postId}")
    public void deletePost(@PathVariable Long postId, @Session Long sessionUserId) {
        postCommandService.deletePost(postId, sessionUserId);
    }

    @GetMapping("/users")
    public UserPostPageResponse getUserPosts(@RequestParam(required = false) Long userId,
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
        Long newLikeId = likeCommandService.doPostLike(postId, sessionUserId);

        return SimpleIdResponse.from(newLikeId);
    }

    @GetMapping("/{postId}/likes")
    public LikeMembersAtPostResponse getLikeMembersAtPost(@PathVariable Long postId,
                                                          @Session Long sessionUserId,
                                                          Pageable pageable) {
        return likeQueryService.getLikeMembersAtPost(postId, sessionUserId, pageable);
    }

    @GetMapping("/{postId}/comments")
    public CommentsAtPostResponse getCommentsAtPost(@PathVariable Long postId,
                                                    @Session Long sessionUserId,
                                                    Pageable pageable) {
        return commentQueryService.getCommentsAtPost(postId, sessionUserId, pageable);
    }

    @GetMapping("/search")
    public SearchPostsResponse getSearchPosts(@ModelAttribute @Validated PostQueryRequest request,
                                              Pageable pageable,
                                              @Session Long sessionUserId) {
        Slice<SearchPostDto> page = postQueryService.getSearchedPosts(pageable, request, sessionUserId);

        return SearchPostsResponse.from(page);
    }
}