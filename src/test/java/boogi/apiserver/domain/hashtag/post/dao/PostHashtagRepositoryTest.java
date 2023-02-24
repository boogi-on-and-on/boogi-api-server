package boogi.apiserver.domain.hashtag.post.dao;

import boogi.apiserver.annotations.CustomDataJpaTest;
import boogi.apiserver.builder.TestPost;
import boogi.apiserver.builder.TestPostHashtag;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.utils.PersistenceUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CustomDataJpaTest
class PostHashtagRepositoryTest {

    @Autowired
    private PostHashtagRepository postHashtagRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    EntityManager em;

    private PersistenceUtil persistenceUtil;

    @BeforeEach
    void init() {
        persistenceUtil = new PersistenceUtil(em);
    }


    @Test
    @DisplayName("postId로 해당 글에 달린 PostHashtag들을 조회한다.")
    void testFindPostHashtagByPostId() {
        final Post post = TestPost.builder().build();
        postRepository.save(post);

        final PostHashtag postHashtag1 = TestPostHashtag.builder()
                .post(post)
                .build();

        final PostHashtag postHashtag2 = TestPostHashtag.builder()
                .post(post)
                .build();

        final PostHashtag postHashtag3 = TestPostHashtag.builder().build();

        postHashtagRepository.saveAll(List.of(postHashtag1, postHashtag2, postHashtag3));

        persistenceUtil.cleanPersistenceContext();

        List<PostHashtag> postHashtags = postHashtagRepository
                .findPostHashtagByPostId(post.getId());

        assertThat(postHashtags.size()).isEqualTo(2);
        assertThat(postHashtags.get(0).getId()).isEqualTo(postHashtag1.getId());
        assertThat(postHashtags.get(0).getPost().getId()).isEqualTo(post.getId());
        assertThat(postHashtags.get(1).getId()).isEqualTo(postHashtag2.getId());
        assertThat(postHashtags.get(1).getPost().getId()).isEqualTo(post.getId());
    }

    @Test
    @DisplayName("postId로 해당 글에 달린 PostHashtag들을 전부 삭제한다(Hard Delete).")
    void testDeleteAllByPostId() {
        final Post post = TestPost.builder().build();
        postRepository.save(post);

        final PostHashtag postHashtag1 = TestPostHashtag.builder()
                .post(post)
                .build();

        final PostHashtag postHashtag2 = TestPostHashtag.builder()
                .post(post)
                .build();

        final PostHashtag postHashtag3 = TestPostHashtag.builder().build();

        postHashtagRepository.saveAll(List.of(postHashtag1, postHashtag2, postHashtag3));

        persistenceUtil.cleanPersistenceContext();

        postHashtagRepository.deleteAllByPostId(post.getId());

        List<PostHashtag> postHashtagAll = postHashtagRepository.findAll();

        assertThat(postHashtagAll.size()).isEqualTo(1);
        assertThat(postHashtagAll.get(0).getId()).isEqualTo(postHashtag3.getId());
        assertThat(postHashtagAll.get(0).getPost()).isNotEqualTo(post.getId());
    }
}