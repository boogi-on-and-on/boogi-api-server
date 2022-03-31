package boogi.apiserver.domain.hashtag.post.dao;

import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;

import java.util.List;

public interface PostHashtagRepositoryCustom {

    List<PostHashtag> findPostHashtagByPostId(Long postId);

    void deleteAllByPostId(Long postId);
}
