package boogi.apiserver.domain.member.exception;

public class HasNotDeleteAuthorityException extends NotAuthorizedMemberException {

    private static final String MESSAGE = "해당 유저는 삭제 권한이 없습니다";

    public HasNotDeleteAuthorityException() {
        super(MESSAGE);
    }
}
