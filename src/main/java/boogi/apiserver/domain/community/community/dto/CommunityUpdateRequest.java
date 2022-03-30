package boogi.apiserver.domain.community.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommunityUpdateRequest {

    @NotEmpty(message = "커뮤니티 소개란을 입력해주세요.")
    @Size(min = 10, message = "10글자 이상 소개란을 입력해주세요.")
    private String description;

    @Size(max = 5, message = "해시테그는 5개까지만 입력가능합니다.")
    private List<String> hashtags;
}
