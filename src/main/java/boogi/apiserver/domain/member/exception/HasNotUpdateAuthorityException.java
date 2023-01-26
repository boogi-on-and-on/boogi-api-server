package boogi.apiserver.domain.member.exception;

public class HasNotUpdateAuthorityException extends NotAuthorizedMemberException {

    private static final String MESSAGE = "해당 유저는 수정 권한이 없습니다";

    public HasNotUpdateAuthorityException() {
        super(MESSAGE);
    }
}
