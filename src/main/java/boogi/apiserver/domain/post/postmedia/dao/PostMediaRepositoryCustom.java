package boogi.apiserver.domain.post.postmedia.dao;

import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.post.postmedia.vo.PostMedias;

import java.util.List;

public interface PostMediaRepositoryCustom {

    PostMedias findUnmappedPostMediasByUUIDs(List<String> postMediaIds);

    List<PostMedia> findUnmappedPostMedias2(List<String> postMediaUUIds);

    List<PostMedia> findByPostId(Long postId);
}
