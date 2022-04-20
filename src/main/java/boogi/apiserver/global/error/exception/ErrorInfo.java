package boogi.apiserver.global.error.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorInfo {

    // COMMON_ERROR
    RESOURCE_NOT_FOUND("해당 경로의 Http method가 존재하지 않습니다.", "COMMON_001", 400),
    INTERNAL_SERVER_ERROR("알 수없는 오류가 발생했습니다.", "COMMON_002", 500),
    INVALID_INPUT_ERROR("invalid input", "COMMON_003", 200),
    BAD_REQUEST("잘못된 요청입니다.", "COMMON_004", 400),
    NOT_FOUND("해당 리소스가 존재하지 않습니다.", "COMMON_005", 404),

    // SESSION_ERROR
    SESSION_NOT_FOUND_EXCEPTION("로그인 정보를 찾을 수 없습니다.", "COMMON_005", HttpStatus.UNAUTHORIZED.value()),

    // MEMBER_ERROR
    MEMBER_NOT_FOUND("해당 멤버가 없습니다.", "MEMBER_001", HttpStatus.NOT_FOUND.value()),
    MEMBER_NOT_JOINED_COMMUNITY("해당 커뮤니티에 가입되지 않았습니다", "MEMBER_003", HttpStatus.FORBIDDEN.value()),

    MEMBER_NOT_PERMITTED("권한이 없습니다.", "MEMBER_002", HttpStatus.FORBIDDEN.value()),

    // LIKE_ERROR
    LIKE_ALREADY_DO("이미 좋아요한 상태입니다.", "LIKE_001", HttpStatus.BAD_REQUEST.value()),

    // COMMENT_ERROR
    COMMENT_MAX_DEPTH_OVER("댓글은 대댓글까지만 작성 가능합니다.", "COMMENT_001", HttpStatus.BAD_REQUEST.value());

    private String message;
    private String code;
    private final int statusCode;

    ErrorInfo(String message, String code, int statusCode) {
        this.message = message;
        this.code = code;
        this.statusCode = statusCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
