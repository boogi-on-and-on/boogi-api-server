package boogi.apiserver.domain.comment.api;

import boogi.apiserver.domain.comment.application.CommentCoreService;
import boogi.apiserver.domain.comment.application.CommentQueryService;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.dto.CreateComment;
import boogi.apiserver.domain.comment.dto.UserCommentPage;
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
}
