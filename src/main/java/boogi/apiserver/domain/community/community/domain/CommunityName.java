package boogi.apiserver.domain.community.community.domain;

import boogi.apiserver.domain.community.community.exception.InvalidCommunityNameException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.regex.Pattern;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityName {

    public static final int MIN_LENGTH = 1;
    public static final int MAX_LENGTH = 30;

    private static final Pattern PATTERN = Pattern.compile("^[ㄱ-ㅎ|가-힣|a-z|A-Z| ]+$");

    @Column(name = "community_name")
    private String value;

    public CommunityName(final String value) {
        String trimedValue = StringUtils.trimWhitespace(value);
        validate(trimedValue);
        this.value = trimedValue;
    }

    private void validate(String value) {
        if (!StringUtils.hasText(value) ||
                value.length() < MIN_LENGTH || value.length() > MAX_LENGTH ||
                !PATTERN.matcher(value).matches()) {
            throw new InvalidCommunityNameException();
        }
    }
}
