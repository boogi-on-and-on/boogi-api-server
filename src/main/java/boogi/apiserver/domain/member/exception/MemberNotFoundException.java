package boogi.apiserver.domain.member.exception;

import boogi.apiserver.global.error.exception.EntityNotFoundException;

public class MemberNotFoundException extends EntityNotFoundException {
    private static final String MESSAGE = "해당 멤버를 찾을 수 없습니다.";

    public MemberNotFoundException() {
        super(MESSAGE);
    }
}
