package boogi.apiserver.global.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ValidStatusResponse {

    private final boolean isValid;

    @Builder(access = AccessLevel.PRIVATE)
    private ValidStatusResponse(final boolean isValid) {
        this.isValid = isValid;
    }

    public static ValidStatusResponse from(boolean isValid) {
        return ValidStatusResponse.builder()
                .isValid(isValid)
                .build();
    }

    @JsonProperty("isValid")
    public boolean isValid() {
        return isValid;
    }
}
