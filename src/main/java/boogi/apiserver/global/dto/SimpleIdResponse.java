package boogi.apiserver.global.dto;

import lombok.Getter;

import java.util.Objects;

@Getter
public class SimpleIdResponse {

    private Long id;

    public SimpleIdResponse(Long id) {
        if (Objects.isNull(id)) {
            throw new IllegalArgumentException("id는 null일 수 없습니다.");
        }
        this.id = id;
    }
}
