package boogi.apiserver.domain.member.exception;

public class NotViewableMemberException extends NotAuthorizedMemberException {

    private static final String MESSAGE = "비공개 커뮤니티에 가입되어 있지 않는 유저는 볼 수 없습니다.";

    public NotViewableMemberException() {
        super(MESSAGE);
    }
}
