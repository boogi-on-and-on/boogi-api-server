package boogi.apiserver.domain.comment.api;

import boogi.apiserver.domain.comment.application.CommentService;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.dto.request.CreateComment;
import boogi.apiserver.domain.comment.dto.response.UserCommentPage;
import boogi.apiserver.domain.like.application.LikeService;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.like.dto.response.LikeMembersAtComment;
import boogi.apiserver.global.argument_resolver.session.Session;
import boogi.apiserver.global.webclient.push.SendPushNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/comments")
public class CommentApiController {

    private final CommentService commentService;

    private final LikeService likeService;

    private final SendPushNotification sendPushNotification;

    @GetMapping("/users")
    public ResponseEntity<UserCommentPage> getUserCommentsInfo(@RequestParam(required = false) Long userId,
                                                               @Session Long sessionUserId,
                                                               Pageable pageable) {
        Long id = Objects.requireNonNullElse(userId, sessionUserId);
        UserCommentPage userComments = commentService.getUserComments(id, sessionUserId, pageable);

        return ResponseEntity.status(HttpStatus.OK).body(userComments);
    }

    @PostMapping("/")
    public ResponseEntity<Object> createComment(@Validated @RequestBody CreateComment createComment, @Session Long userId) {
        Comment newComment = commentService.createComment(createComment, userId);

        sendPushNotification.commentNotification(newComment.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", newComment.getId()
        ));
    }

    @PostMapping("/{commentId}/likes")
    public ResponseEntity<Object> doLikeAtComment(@PathVariable Long commentId, @Session Long userId) {
        Like newLike = likeService.doLikeAtComment(commentId, userId);

        return ResponseEntity.ok().body(Map.of(
                "id", newLike.getId()
        ));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId, @Session Long userId) {
        commentService.deleteComment(commentId, userId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{commentId}/likes")
    public ResponseEntity<Object> getLikeMembersAtComment(@PathVariable Long commentId, @Session Long userId, Pageable pageable) {
        LikeMembersAtComment likeMembersAtComment = likeService.getLikeMembersAtComment(commentId, userId, pageable);

        return ResponseEntity.ok().body(likeMembersAtComment);
    }
}
