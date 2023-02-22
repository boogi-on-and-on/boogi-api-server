package boogi.apiserver.builder;

import boogi.apiserver.domain.post.post.domain.Post;

import java.util.ArrayList;

public class TestPost {

    public static Post.PostBuilder builder() {
        return Post.builder()
                .content("THIS IS POST CONTENT")
                .hashtags(new ArrayList<>())
                .postMedias(new ArrayList<>())
                .likes(new ArrayList<>());
    }
}
