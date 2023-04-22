package boogi.apiserver.domain.auth.dto;

import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.error.exception.ErrorInfo;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import java.util.Map;

@Getter
public class OAuthAttributes {
    private static final String ALLOWED_EMAIL_SERVER = "hansung.ac.kr";

    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String department;
    private String email;

    @Builder(access = AccessLevel.PRIVATE)
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name, String department, String email) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.department = department;
        this.email = email;
    }

    public static OAuthAttributes of(String userNameAttributeName, Map<String, Object> attributes) {
        validateEmailServer((String) attributes.get("hd"));

        return OAuthAttributes.builder()
                .name((String) attributes.get("given_name"))
                .department(getDepartment((String) attributes.get("family_name")))
                .email((String) attributes.get("email"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    public User toEntity(int sameNameUserNum) {
        return User.of(email, name, department, sameNameUserNum);
    }

    private static void validateEmailServer(String hd) {
        ErrorInfo errorInfo = ErrorInfo.LOGIN_NOT_ALLOWED_EMAIL_SERVER;
        if (!ALLOWED_EMAIL_SERVER.equals(hd)) {
            throw new OAuth2AuthenticationException(new OAuth2Error(errorInfo.getCode(), errorInfo.getMessage(), null), errorInfo.getMessage());
        }
    }

    private static String getDepartment(String rawDepartment) {
        return rawDepartment.substring(1, rawDepartment.length() - 1);
    }
}
