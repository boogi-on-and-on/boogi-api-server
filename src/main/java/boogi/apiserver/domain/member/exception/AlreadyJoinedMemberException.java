package boogi.apiserver.domain.member.exception;

import boogi.apiserver.global.error.exception.InvalidValueException;

public class AlreadyJoinedMemberException extends InvalidValueException {
    public AlreadyJoinedMemberException() {
        super("이미 가입한 커뮤니티입니다.");
    }
}
