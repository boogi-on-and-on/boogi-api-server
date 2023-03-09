package boogi.apiserver.domain.community.joinrequest.exception;

import boogi.apiserver.global.error.exception.InvalidValueException;

public class UnmatchedJoinRequestCommunityException extends InvalidValueException {

    private final static String MESSAGE = "해당 커뮤니티에서 처리할 수 없는 요청입니다.";

    public UnmatchedJoinRequestCommunityException() {
        super(MESSAGE);
    }
}
