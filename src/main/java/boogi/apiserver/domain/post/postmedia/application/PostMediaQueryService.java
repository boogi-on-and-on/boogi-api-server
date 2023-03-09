package boogi.apiserver.domain.post.postmedia.application;

import boogi.apiserver.domain.post.postmedia.dao.PostMediaRepository;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.post.postmedia.exception.UnmappedPostMediaExcecption;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostMediaQueryService {

    private final PostMediaRepository postMediaRepository;

    public List<PostMedia> getUnmappedPostMediasByUUID(List<String> postMediaUUIDs) {
        final List<PostMedia> findPostMedias = postMediaRepository.findUnmappedPostMedias2(postMediaUUIDs);
        if (findPostMedias.size() != postMediaUUIDs.size()) {
            throw new UnmappedPostMediaExcecption();
        }
        return findPostMedias;
    }
}