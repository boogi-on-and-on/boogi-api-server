package boogi.apiserver.domain.user.domain;

import boogi.apiserver.domain.user.exception.InvalidEmailException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.regex.Pattern;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Email {

    public static final int MIN_LENGTH = 5;
    public static final int MAX_LENGTH = 80;

    private static final Pattern PATTERN = Pattern.compile("^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*[.][a-zA-Z]{2,3}$");

    @Column(name = "email")
    private String value;

    public Email(String value) {
        String trimedValue = StringUtils.trimWhitespace(value);
        validate(trimedValue);
        this.value = trimedValue;
    }

    private void validate(String value) {
        if (!StringUtils.hasText(value) ||
                value.length() < MIN_LENGTH || value.length() > MAX_LENGTH ||
                !PATTERN.matcher(value).matches()) {
            throw new InvalidEmailException();
        }
    }
}
