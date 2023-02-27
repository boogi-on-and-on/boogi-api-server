package boogi.apiserver.domain.user.domain;

import boogi.apiserver.domain.user.exception.InvalidUsernameException;
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
public class Username {
    public static final int MIN_LENGTH = 5;
    public static final int MAX_LENGTH = 20;

    private static final Pattern PATTERN = Pattern.compile("^[ㄱ-ㅎ|가-힣]+$");

    @Column(name = "username")
    private String value;

    public Username(final String value) {
        this.value = value;
    }

    private void validate(String value) {
        if (!StringUtils.hasText(value) ||
                value.length() < MIN_LENGTH || value.length() > MAX_LENGTH ||
                !PATTERN.matcher(value).matches()) {
            throw new InvalidUsernameException();
        }
    }

}
