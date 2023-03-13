package boogi.apiserver.builder;

import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;

public class TestPostHashtag {

    public static PostHashtag.PostHashtagBuilder builder() {
        return PostHashtag.builder()
                .tag("TEST");
    }
}
