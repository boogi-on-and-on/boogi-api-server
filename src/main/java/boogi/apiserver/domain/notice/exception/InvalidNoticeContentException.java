package boogi.apiserver.domain.notice.exception;

import boogi.apiserver.domain.notice.domain.Content;
import boogi.apiserver.global.error.exception.InvalidValueException;

public class InvalidNoticeContentException extends InvalidValueException {
    private static final String MESSAGE = "공지사항 글 내용의 길이는" + Content.MIN_LENGTH + " ~ " + Content.MAX_LENGTH + "까지 입력이 가능합니다.";

    public InvalidNoticeContentException() {
        super(MESSAGE);
    }
}
