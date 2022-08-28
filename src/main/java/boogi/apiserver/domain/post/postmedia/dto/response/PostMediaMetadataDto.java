package boogi.apiserver.domain.post.postmedia.dto.response;

import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostMediaMetadataDto {

    private String url;
    private String type;

    private PostMediaMetadataDto(PostMedia postMedia) {
        this.url = postMedia.getMediaURL();
        this.type = postMedia.getMediaType().toString();
    }

    public static PostMediaMetadataDto of(PostMedia postMedia) {
        return new PostMediaMetadataDto(postMedia);
    }
}
