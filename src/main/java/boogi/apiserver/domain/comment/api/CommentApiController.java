package boogi.apiserver.domain.comment.api;

import boogi.apiserver.domain.comment.application.CommentCoreService;
import boogi.apiserver.domain.comment.application.CommentQueryService;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.dto.CreateComment;
import boogi.apiserver.domain.comment.dto.UserCommentPage;
import boogi.apiserver.domain.like.application.LikeCoreService;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.global.argument_resolver.session.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/comments")
public class CommentApiController {

    private final CommentCoreService commentCoreService;
    private final CommentQueryService commentQueryService;

    private final LikeCoreService likeCoreService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserCommentPage> getUserCommentsInfo(@PathVariable Long userId, Pageable pageable) {
        UserCommentPage userCommentsPage = commentQueryService.getUserComments(pageable, userId);

        return ResponseEntity.status(HttpStatus.OK).body(userCommentsPage);
    }

    @PostMapping("/")
    public ResponseEntity<Object> createComment(@Validated @RequestBody CreateComment createComment, @Session Long userId) {
        Comment newComment = commentCoreService.createComment(createComment, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", newComment.getId()
        ));
    }

    @PostMapping("/{commentId}/likes")
    public ResponseEntity<Object> doLikeAtComment(@PathVariable Long commentId, @Session Long userId) {
        Like newLike = likeCoreService.doLikeAtComment(commentId, userId);

        return ResponseEntity.ok().body(Map.of(
                "id", newLike.getId()
        ));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Object> deleteComment(@PathVariable Long commentId, @Session Long userId) {
        commentCoreService.deleteComment(commentId, userId);

        return ResponseEntity.ok().build();
    }
}
