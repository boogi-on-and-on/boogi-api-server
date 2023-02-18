package boogi.apiserver.global.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

@Getter
public class ValidStatusResponse {

    private final Boolean isValid;

    @Builder(access = AccessLevel.PRIVATE)
    private ValidStatusResponse(final Boolean isValid) {
        this.isValid = isValid;
    }

    public static ValidStatusResponse from(Boolean b) {
        return ValidStatusResponse.builder()
                .isValid(Objects.requireNonNullElse(b, false))
                .build();
    }
}
