package boogi.apiserver.domain.model;

import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@Getter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Content {

    @Column(name = "content")
    protected String value;

    public Content(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String value) {
        if (!StringUtils.hasText(value) ||
                value.length() < min_length() || value.length() > max_length()) {
            throw exception();
        }
    }

    protected abstract int min_length();

    protected abstract int max_length();

    protected abstract InvalidValueException exception();
}
