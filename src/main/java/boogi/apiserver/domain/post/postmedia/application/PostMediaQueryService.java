package boogi.apiserver.domain.post.postmedia.application;

import boogi.apiserver.domain.post.postmedia.dao.PostMediaRepository;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostMediaQueryService {

    private final PostMediaRepository postMediaRepository;

    public List<PostMedia> getUnmappedPostMediasByUUID(List<String> postMediaIds) {
        if (postMediaIds == null || postMediaIds.isEmpty())
            return new ArrayList<>();

        List<PostMedia> findPostMedias = postMediaRepository.findUnmappedPostMediasByIds(postMediaIds);

        if (postMediaIds.size() != findPostMedias.size()) {
            throw new InvalidValueException("잘못된 요청입니다");
        }
        return findPostMedias;
    }
}