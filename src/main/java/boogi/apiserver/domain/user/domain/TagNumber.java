package boogi.apiserver.domain.user.domain;

import boogi.apiserver.domain.user.exception.InvalidTagNumberException;
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
public class TagNumber {

    public static final int LENGTH = 5;

    @Column(name = "tag_num")
    private String value;

    private static final Pattern PATTERN = Pattern.compile("^#[0-9]{3}[1-9]$");

    public TagNumber(final String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String value) {
        if (!StringUtils.hasText(value) ||
                value.length() != LENGTH ||
                !PATTERN.matcher(value).matches()) {
            throw new InvalidTagNumberException();
        }
    }
}
