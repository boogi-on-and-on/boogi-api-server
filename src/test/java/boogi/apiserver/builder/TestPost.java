package boogi.apiserver.builder;

import boogi.apiserver.domain.post.post.domain.Post;

import java.util.ArrayList;

public class TestPost {

    public static Post.PostBuilder builder() {
        return Post.builder()
                .content("테스트 글의 컨텐츠")
                .hashtags(new ArrayList<>())
                .postMedias(new ArrayList<>())
                .likes(new ArrayList<>());
    }
}
