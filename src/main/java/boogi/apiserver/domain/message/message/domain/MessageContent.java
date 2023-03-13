package boogi.apiserver.domain.message.message.domain;

import boogi.apiserver.domain.message.message.exception.InvalidMessageContentException;
import boogi.apiserver.domain.model.Content;
import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;


@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageContent extends Content {

    public static final int MIN_LENGTH = 1;
    public static final int MAX_LENGTH = 255;

    public MessageContent(String value) {
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
        return new InvalidMessageContentException();
    }
}
