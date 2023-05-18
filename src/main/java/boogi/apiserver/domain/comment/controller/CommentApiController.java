package boogi.apiserver.domain.comment.controller;

import boogi.apiserver.domain.comment.application.CommentCommand;
import boogi.apiserver.domain.comment.application.CommentQuery;
import boogi.apiserver.domain.comment.dto.request.CreateCommentRequest;
import boogi.apiserver.domain.comment.dto.response.UserCommentPageResponse;
import boogi.apiserver.domain.like.application.LikeCommand;
import boogi.apiserver.domain.like.application.LikeQuery;
import boogi.apiserver.domain.like.dto.response.LikeMembersAtCommentResponse;
import boogi.apiserver.global.argument_resolver.session.Session;
import boogi.apiserver.global.dto.SimpleIdResponse;
import boogi.apiserver.global.webclient.push.MentionType;
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

    private final CommentCommand commentCommand;
    private final CommentQuery commentQuery;

    private final LikeCommand likeCommand;
    private final LikeQuery likeQuery;

    private final SendPushNotification sendPushNotification;

    @GetMapping("/users")
    public UserCommentPageResponse getUserComments(@RequestParam(required = false) Long userId,
                                                   @Session Long sessionUserId,
                                                   Pageable pageable) {
        Long id = Objects.requireNonNullElse(userId, sessionUserId);
        return commentQuery.getUserComments(id, sessionUserId, pageable);
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public SimpleIdResponse createComment(@Validated @RequestBody CreateCommentRequest request, @Session Long userId) {
        Long newCommentId = commentCommand.createComment(request, userId);

        sendPushNotification.commentNotification(newCommentId);
        if (!request.getMentionedUserIds().isEmpty()) {
            sendPushNotification.mentionNotification(request.getMentionedUserIds(), newCommentId, MentionType.COMMENT);
        }

        return SimpleIdResponse.from(newCommentId);
    }

    @PostMapping("/{commentId}/likes")
    public SimpleIdResponse doCommentLike(@PathVariable Long commentId, @Session Long userId) {
        Long newLikeId = likeCommand.doCommentLike(commentId, userId);

        return SimpleIdResponse.from(newLikeId);
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Long commentId, @Session Long userId) {
        commentCommand.deleteComment(commentId, userId);
    }

    @GetMapping("/{commentId}/likes")
    public LikeMembersAtCommentResponse getLikeMembersAtComment(@PathVariable Long commentId,
                                                                @Session Long userId,
                                                                Pageable pageable) {
        return likeQuery.getLikeMembersAtComment(commentId, userId, pageable);
    }
}
