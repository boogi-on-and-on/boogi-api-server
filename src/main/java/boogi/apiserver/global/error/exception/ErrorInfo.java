package boogi.apiserver.global.error.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
public enum ErrorInfo {

    // COMMON_ERROR
    COMMON_RESOURCE_NOT_FOUND("해당 경로의 Http method가 존재하지 않습니다.", "COMMON_001", BAD_REQUEST),
    COMMON_INTERNAL_SERVER_ERROR("알 수 없는 오류가 발생했습니다.", "COMMON_002", INTERNAL_SERVER_ERROR),
    COMMON_INVALID_INPUT_ERROR("invalid input", "COMMON_003", BAD_REQUEST),
    COMMON_BAD_REQUEST("잘못된 요청입니다.", "COMMON_004", NOT_FOUND),
    COMMON_NOT_FOUND("해당 리소스가 존재하지 않습니다.", "COMMON_005", NOT_FOUND),

    // SESSION_ERROR
    SESSION_NOT_FOUND_EXCEPTION("로그인 정보를 찾을 수 없습니다.", "COMMON_005", UNAUTHORIZED),

    // MEMBER_ERROR
    MEMBER_NOT_FOUND("해당 멤버가 없습니다.", "MEMBER_001", NOT_FOUND),
    MEMBER_NOT_JOINED_COMMUNITY("해당 커뮤니티에 가입되지 않았습니다", "MEMBER_003", FORBIDDEN),

    MEMBER_NOT_PERMITTED("권한이 없습니다.", "MEMBER_002", FORBIDDEN),

    // LIKE_ERROR
    LIKE_ALREADY_DO("이미 좋아요한 상태입니다.", "LIKE_001", BAD_REQUEST),

    // COMMENT_ERROR
    COMMENT_MAX_DEPTH_OVER("댓글은 대댓글까지만 작성 가능합니다.", "COMMENT_001", BAD_REQUEST);

    private String message;
    private String code;
    private final HttpStatus statusCode;

    ErrorInfo(String message, String code, HttpStatus statusCode) {
        this.message = message;
        this.code = code;
        this.statusCode = statusCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
