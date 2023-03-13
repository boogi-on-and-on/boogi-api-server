package boogi.apiserver.domain.notice.domain;


import boogi.apiserver.domain.notice.exception.InvalidTitleException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Title {

    public static final int MIN_LENGTH = 5;
    public static final int MAX_LENGTH = 30;

    @Column(name = "title")
    private String value;

    public Title(String value) {
        String trimedValue = StringUtils.trimWhitespace(value);
        validate(trimedValue);
        this.value = trimedValue;
    }

    private void validate(String value) {
        if (!StringUtils.hasText(value) ||
                value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new InvalidTitleException();
        }
    }
}
