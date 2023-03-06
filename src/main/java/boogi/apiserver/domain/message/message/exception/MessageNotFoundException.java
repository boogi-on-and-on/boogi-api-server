package boogi.apiserver.domain.message.message.exception;

import boogi.apiserver.global.error.exception.EntityNotFoundException;

public class MessageNotFoundException extends EntityNotFoundException {
    private static final String MESSAGE = "해당 쪽지가 존재하지 않습니다.";

    public MessageNotFoundException() {
        super(MESSAGE);
    }
}
