package boogi.apiserver.domain.member.exception;

import boogi.apiserver.global.error.exception.EntityNotFoundException;
import boogi.apiserver.global.error.exception.ErrorInfo;

public class NotJoinedMemberException extends EntityNotFoundException {
    public NotJoinedMemberException() {
        super(ErrorInfo.MEMBER_NOT_JOINED_COMMUNITY);
    }
}
