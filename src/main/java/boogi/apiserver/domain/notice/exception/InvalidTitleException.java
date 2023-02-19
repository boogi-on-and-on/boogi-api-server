package boogi.apiserver.domain.notice.exception;

import boogi.apiserver.domain.notice.domain.Title;
import boogi.apiserver.global.error.exception.InvalidValueException;

public class InvalidTitleException extends InvalidValueException {
    private static final String MESSAGE = "공지사항의 제목의 길이는 " + Title.MIN_LENGTH + " ~ " + Title.MAX_LENGTH + "까지 입력이 가능합니다.";

    public InvalidTitleException() {
        super(MESSAGE);
    }
}
