package boogi.apiserver.domain.post.postmedia.repository;

import boogi.apiserver.domain.post.postmedia.domain.PostMedia;

import java.util.List;

public interface PostMediaRepositoryCustom {

    List<PostMedia> findUnmappedPostMedias(List<String> postMediaUUIDs);
}
