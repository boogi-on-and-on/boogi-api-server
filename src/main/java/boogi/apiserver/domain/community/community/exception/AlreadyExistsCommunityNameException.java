package boogi.apiserver.domain.community.community.exception;

import boogi.apiserver.global.error.exception.InvalidValueException;

public class AlreadyExistsCommunityNameException extends InvalidValueException {
    public AlreadyExistsCommunityNameException() {
        super("이미 해당 커뮤니티 이름이 존재합니다.");
    }
}
