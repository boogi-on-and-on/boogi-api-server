package boogi.apiserver.global.error.exception;

import lombok.Getter;

@Getter
public enum ErrorInfo {

    // COMMON_ERROR
    RESOURCE_NOT_FOUND("해당 경로의 Http method가 존재하지 않습니다.", "COMMON_001", 400),
    INTERNAL_SERVER_ERROR("알 수없는 오류가 발생했습니다.", "COMMON_002", 500),
    INVALID_INPUT_ERROR("invalid input", "COMMON_003", 200),
    BAD_REQUEST("잘못된 요청입니다.", "COMMON_004", 400),

    // MEMBER_ERROR
    MEMBER_NOT_FOUND("해당 멤버가 없습니다.", "MEMBER_001", 400);

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
