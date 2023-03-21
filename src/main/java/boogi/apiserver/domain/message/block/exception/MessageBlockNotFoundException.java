package boogi.apiserver.domain.message.block.exception;

import boogi.apiserver.global.error.exception.EntityNotFoundException;

public class MessageBlockNotFoundException extends EntityNotFoundException {
    public static final String MESSAGE = "해당 유저의 메시지 차단 정보를 찾을 수 없습니다.";

    public MessageBlockNotFoundException() {
        super(MESSAGE);
    }
}
