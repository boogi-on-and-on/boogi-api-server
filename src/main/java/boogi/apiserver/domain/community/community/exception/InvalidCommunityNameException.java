package boogi.apiserver.domain.community.community.exception;

import boogi.apiserver.domain.community.community.domain.CommunityName;
import boogi.apiserver.global.error.exception.InvalidValueException;

public class InvalidCommunityNameException extends InvalidValueException {
    private static final String MESSAGE = CommunityName.MIN_LENGTH + " ~ " + CommunityName.MAX_LENGTH + "까지 입력이 가능합니다.";

    public InvalidCommunityNameException() {
        super(MESSAGE);
    }
}
