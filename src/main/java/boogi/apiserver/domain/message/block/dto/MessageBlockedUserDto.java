package boogi.apiserver.domain.message.block.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
@Builder
public class MessageBlockedUserDto implements Serializable {
    private Long userId;
    private String nameTag;

    @QueryProjection
    public MessageBlockedUserDto(Long userId, String name, String tag) {
        this.userId = userId;
        this.nameTag = name + tag;
    }
}
