package boogi.apiserver.domain.user.domain;

import boogi.apiserver.domain.report.exception.InvalidReportContentException;
import boogi.apiserver.domain.user.exception.InvalidDepartmentException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Department {

    public static final int MIN_LENGTH = 3;
    public static final int MAX_LENGTH = 20;

    //todo: 한글만 오도록 정규식

    @Column(name = "department")
    private String value;

    public Department(final String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String value) {
        if (!StringUtils.hasText(value) ||
                value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
            throw new InvalidDepartmentException();
        }
    }
}
