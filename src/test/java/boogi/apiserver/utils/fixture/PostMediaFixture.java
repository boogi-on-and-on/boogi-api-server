package boogi.apiserver.utils.fixture;

import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.postmedia.domain.MediaType;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;

import java.time.LocalDateTime;

import static boogi.apiserver.utils.fixture.TimeFixture.STANDARD;

public enum PostMediaFixture {
    POSTMEDIA1("uuid1", MediaType.IMG, "url1", null),
    POSTMEDIA2("uuid2", MediaType.IMG, "url2", null),
    DELETED_POSTMEDIA("uuid", MediaType.IMG, "url", STANDARD);

    public final String uuid;
    public final MediaType mediaType;
    public final String mediaURL;
    public final LocalDateTime deletedAt;

    PostMediaFixture(String uuid, MediaType mediaType, String mediaURL, LocalDateTime deletedAt) {
        this.uuid = uuid;
        this.mediaType = mediaType;
        this.mediaURL = mediaURL;
        this.deletedAt = deletedAt;
    }

    public PostMedia toPostMedia() {
        return toPostMedia(null, null);
    }

    public PostMedia toPostMedia(Long id) {
        return toPostMedia(id, null);
    }

    public PostMedia toPostMedia(Long id, Post post) {
        return PostMedia.builder()
                .id(id)
                .post(post)
                .uuid(this.uuid)
                .mediaType(this.mediaType)
                .mediaURL(this.mediaURL)
                .deletedAt(this.deletedAt)
                .build();
    }
}
