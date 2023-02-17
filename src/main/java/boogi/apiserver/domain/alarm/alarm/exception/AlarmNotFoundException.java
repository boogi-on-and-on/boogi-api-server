package boogi.apiserver.domain.alarm.alarm.exception;

import boogi.apiserver.global.error.exception.EntityNotFoundException;

public class AlarmNotFoundException extends EntityNotFoundException {
    private static final String MESSAGE = "해당 알람이 존재하지 않습니다.";

    public AlarmNotFoundException() {
        super(MESSAGE);
    }
}
