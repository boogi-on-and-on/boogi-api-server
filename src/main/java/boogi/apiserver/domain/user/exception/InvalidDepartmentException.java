package boogi.apiserver.domain.user.exception;

import boogi.apiserver.domain.user.domain.Department;
import boogi.apiserver.global.error.exception.InvalidValueException;

public class InvalidDepartmentException extends InvalidValueException {
    private static final String MESSAGE = "학과 명은 " + Department.MIN_LENGTH + " ~ " + Department.MAX_LENGTH +
            " 길이의 한글만 가능합니다.";

    public InvalidDepartmentException() {
        super(MESSAGE);
    }
}
