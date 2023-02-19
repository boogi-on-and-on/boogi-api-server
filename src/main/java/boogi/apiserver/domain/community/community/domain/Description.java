package boogi.apiserver.domain.community.community.domain;

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
public class Description {

    public static final int MIN_LENGTH = 10;
    public static final int MAX_LENGTH = 500;

    @Column(name = "description")
    private String value;

    public Description(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String value) {
        if (!!StringUtils.hasText(value) ||
                value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new InvalidCommentContentException();
        }
    }
}
