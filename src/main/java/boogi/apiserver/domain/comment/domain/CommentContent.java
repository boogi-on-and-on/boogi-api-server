package boogi.apiserver.domain.comment.domain;

import boogi.apiserver.domain.comment.exception.InvalidCommentContentException;
import boogi.apiserver.domain.model.Content;
import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentContent extends Content {

    public static final int MIN_LENGTH = 1;
    public static final int MAX_LENGTH = 255;

    public CommentContent(String value) {
        super(value);
    }

    @Override
    protected int min_length() {
        return MIN_LENGTH;
    }

    @Override
    protected int max_length() {
        return MAX_LENGTH;
    }

    @Override
    protected InvalidValueException exception() {
        return new InvalidCommentContentException();
    }
}
