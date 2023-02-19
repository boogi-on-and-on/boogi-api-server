package boogi.apiserver.domain.user.domain;

import boogi.apiserver.domain.user.exception.InvalidDepartmentException;
import boogi.apiserver.domain.user.exception.InvalidIntroduceException;
import boogi.apiserver.domain.user.exception.InvalidUsernameException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Introduce {

    public static final int MIN_LENGTH = 10;
    public static final int MAX_LENGTH = 500;

    @Column(name = "introduce")
    private String value;

    public Introduce(final String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String value) {
        if (!StringUtils.hasText(value) ||
                value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new InvalidIntroduceException();
        }
    }
}
