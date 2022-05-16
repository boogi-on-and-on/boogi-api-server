package boogi.apiserver.domain.post.postmedia.dao;

import boogi.apiserver.domain.post.postmedia.domain.PostMedia;

import java.util.List;

public interface PostMediaRepositoryCustom {

    List<PostMedia> findUnmappedPostMediasByIds(List<String> postMediaIds);

    List<PostMedia> findByPostId(Long postId);
}
