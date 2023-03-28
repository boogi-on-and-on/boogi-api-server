package boogi.apiserver.domain.comment.domain;

import boogi.apiserver.builder.TestComment;
import boogi.apiserver.builder.TestPost;
import boogi.apiserver.domain.post.post.domain.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommentTest {

    @Test
    @DisplayName("댓글 삭제시 게시글의 댓글 수가 1 감소하고, 댓글 삭제시점이 설정된다.")
    void deleteComment() {
        Post post = TestPost.builder().commentCount(1).build();
        Comment comment = TestComment.builder().post(post).build();

        comment.deleteComment();

        assertThat(comment.getDeletedAt()).isNotNull();
        assertThat(post.getCommentCount()).isZero();
    }
}