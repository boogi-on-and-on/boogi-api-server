package boogi.apiserver.domain.alarm.alarm.exception;

import boogi.apiserver.global.error.exception.InvalidValueException;

public class CanNotDeleteAlarmException extends InvalidValueException {
    private static final String MESSAGE = "해당 알림을 삭제할 권한이 없습니다.";

    public CanNotDeleteAlarmException() {
        super(MESSAGE);
    }
}
