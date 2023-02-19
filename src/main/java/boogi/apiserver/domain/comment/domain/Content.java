package boogi.apiserver.domain.comment.domain;

import boogi.apiserver.domain.comment.exception.InvalidCommentContentException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Content {

    public static final int MIN_LENGTH = 1;
    public static final int MAX_LENGTH = 255;

    @Column(name = "content", unique = true)
    private String value;

    public Content(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String value) {
        if (!StringUtils.hasText(value) ||
                value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new InvalidCommentContentException();
        }
    }
}
