package boogi.apiserver.domain.post.post.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;


@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class CreatePost {

    @NotNull
    private Long communityId;

    @NotEmpty
    @Size(min = 1, max = 1000, message = "1000자 이내로 입력해주세요")
    private String content;

    private List<String> hashtags;

    // TODO: 이미지 업로드 추가
}
