package boogi.apiserver.domain.comment.api;

import boogi.apiserver.domain.comment.application.CommentQueryService;
import boogi.apiserver.domain.comment.dto.UserCommentPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/comments")
public class CommentApiController {

    private final CommentQueryService commentQueryService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserCommentPage> getUserCommentsInfo(@PathVariable Long userId, Pageable pageable) {
        UserCommentPage userCommentsPage = commentQueryService.getUserComments(pageable, userId);

        return ResponseEntity.status(HttpStatus.OK).body(userCommentsPage);
    }

}
