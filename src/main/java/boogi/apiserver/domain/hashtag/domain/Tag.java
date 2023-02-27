package boogi.apiserver.domain.hashtag.domain;

import boogi.apiserver.domain.hashtag.exception.InvalidTagException;
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
public class Tag {

    public static final int MIN_LENGTH = 1;
    public static final int MAX_LENGTH = 10;

    private static final Pattern PATTERN = Pattern.compile("^[ㄱ-ㅎ|가-힣|a-z|A-Z]+$");

    @Column(name = "tag")
    private String value;

    public Tag(String value) {
        this.value = value;
    }

    private void validate(String value) {
        if (!StringUtils.hasText(value) ||
                value.length() < MIN_LENGTH || value.length() > MAX_LENGTH ||
                !PATTERN.matcher(value).matches()) {
            throw new InvalidTagException();
        }
    }
}
