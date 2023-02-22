package boogi.apiserver.builder;

import boogi.apiserver.domain.comment.domain.Comment;

public class TestComment {

    public static Comment.CommentBuilder builder() {
        return Comment.builder()
                .content("COMMENT");
    }
}
