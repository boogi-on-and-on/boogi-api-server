package boogi.apiserver.domain.community.joinrequest.exception;

import boogi.apiserver.global.error.exception.EntityNotFoundException;

public class JoinRequestNotFoundException extends EntityNotFoundException {
    private final static String MESSAGE = "해당 가입요청이 존재하지 않습니다.";

    public JoinRequestNotFoundException() {
        super(MESSAGE);
    }
}
