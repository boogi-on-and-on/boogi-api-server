package boogi.apiserver.domain.post.postmedia.dto.dto;

import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PostMediaMetadataDto {

    private String url;
    private String type;

    public PostMediaMetadataDto(String url, String type) {
        this.url = url;
        this.type = type;
    }

    public static PostMediaMetadataDto from(PostMedia postMedia) {
        return new PostMediaMetadataDto(postMedia.getMediaURL(), postMedia.getMediaType().toString());
    }

    public static List<PostMediaMetadataDto> listFrom(List<PostMedia> postMedias) {
        return postMedias.stream()
                .map(PostMediaMetadataDto::from)
                .collect(Collectors.toList());
    }
}
