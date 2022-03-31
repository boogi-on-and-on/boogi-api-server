package boogi.apiserver.domain.hashtag.post.application;


import boogi.apiserver.domain.hashtag.post.dao.PostHashtagRepository;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostHashtagCoreService {

    private final PostQueryService postQueryService;
    private final PostHashtagRepository postHashtagRepository;

    @Transactional
    public List<PostHashtag> addTags(Long postId, List<String> tags) {
        if (tags == null || tags.size() == 0) {
            return null;
        }

        Post post = postQueryService.getPost(postId);

        List<PostHashtag> postHashtags = tags.stream()
                .map(ht -> PostHashtag.of(ht, post))
                .collect(Collectors.toList());

        return postHashtagRepository.saveAll(postHashtags);
    }

    @Transactional
    public void removeTagsByPostId(Long postId) {
        postHashtagRepository.deleteAllByPostId(postId);
    }
}
