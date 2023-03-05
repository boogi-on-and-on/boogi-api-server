package boogi.apiserver.domain.comment.api;

import boogi.apiserver.domain.comment.application.CommentCommandService;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.dto.request.CreateCommentRequest;
import boogi.apiserver.domain.comment.dto.response.UserCommentPageResponse;
import boogi.apiserver.domain.like.application.LikeCommandService;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.like.dto.response.LikeMembersAtCommentResponse;
import boogi.apiserver.global.argument_resolver.session.Session;
import boogi.apiserver.global.dto.SimpleIdResponse;
import boogi.apiserver.global.webclient.push.SendPushNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/comments")
public class CommentApiController {

    private final CommentCommandService commentCommandService;

    private final LikeCommandService likeCommandService;

    private final SendPushNotification sendPushNotification;

    @GetMapping("/users")
    public UserCommentPageResponse getUserCommentsInfo(@RequestParam(required = false) Long userId,
                                                       @Session Long sessionUserId,
                                                       Pageable pageable) {
        Long id = Objects.requireNonNullElse(userId, sessionUserId);
        return commentCommandService.getUserComments(id, sessionUserId, pageable);
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public SimpleIdResponse createComment(@Validated @RequestBody CreateCommentRequest createCommentRequest, @Session Long userId) {
        Comment newComment = commentCommandService.createComment(createCommentRequest, userId);

        sendPushNotification.commentNotification(newComment.getId());

        return SimpleIdResponse.from(newComment.getId());
    }

    @PostMapping("/{commentId}/likes")
    public SimpleIdResponse doLikeAtComment(@PathVariable Long commentId, @Session Long userId) {
        Like newLike = likeCommandService.doLikeAtComment(commentId, userId);

        return SimpleIdResponse.from(newLike.getId());
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Long commentId, @Session Long userId) {
        commentCommandService.deleteComment(commentId, userId);
    }

    @GetMapping("/{commentId}/likes")
    public LikeMembersAtCommentResponse getLikeMembersAtComment(@PathVariable Long commentId, @Session Long userId, Pageable pageable) {
        return likeCommandService.getLikeMembersAtComment(commentId, userId, pageable);
    }
}
