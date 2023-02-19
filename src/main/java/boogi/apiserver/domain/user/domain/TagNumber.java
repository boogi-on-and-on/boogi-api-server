package boogi.apiserver.domain.user.domain;

import boogi.apiserver.domain.user.exception.InvalidDepartmentException;
import boogi.apiserver.domain.user.exception.InvalidTagNumberException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TagNumber {

    public static final int LENGTH = 5;

    @Column(name = "tag_num")
    private String value;

    //todo: #0001 형식으로 정규식 체크

    public TagNumber(final String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String value) {
        if (!StringUtils.hasText(value) ||
                value.length() != LENGTH) {
            throw new InvalidTagNumberException();
        }
    }
}
