package boogi.apiserver.domain.post.postmedia.application;

import boogi.apiserver.domain.post.postmedia.dao.PostMediaRepository;
import boogi.apiserver.domain.post.postmedia.vo.PostMedias;
import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostMediaQueryService {

    private final PostMediaRepository postMediaRepository;

    public PostMedias getUnmappedPostMediasByUUID(List<String> postMediaIds) {
        if (postMediaIds == null || postMediaIds.isEmpty())
            return PostMedias.EMPTY;

        PostMedias findPostMedias = postMediaRepository.findUnmappedPostMediasByUUIDs(postMediaIds);

        if (!findPostMedias.isSameSize(postMediaIds)) {
            throw new InvalidValueException("잘못된 요청입니다");
        }
        return findPostMedias;
    }
}