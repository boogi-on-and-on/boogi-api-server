package boogi.apiserver.domain.community.community.exception;

import boogi.apiserver.domain.community.community.domain.CommunityName;
import boogi.apiserver.global.error.exception.InvalidValueException;

public class InvalidCommunityNameException extends InvalidValueException {
    private static final String MESSAGE = "커뮤니티 이름은 " + CommunityName.MIN_LENGTH + " ~ " + CommunityName.MAX_LENGTH +
            "길이의 영어나 한글이어야 합니다.";

    public InvalidCommunityNameException() {
        super(MESSAGE);
    }
}
