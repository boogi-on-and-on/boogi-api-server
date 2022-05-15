package boogi.apiserver.domain.post.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class UpdatePost {

    @NotEmpty
    @Size(min = 1, max = 1000, message = "1000자 이내로 입력해주세요")
    private String content;

    private List<String> hashtags;

    private List<String> postMediaIds;
}