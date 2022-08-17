package boogi.apiserver.domain.hashtag.post.dao;

import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.utils.PersistenceUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PostHashtagRepositoryTest {

    @Autowired
    private PostHashtagRepository postHashtagRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    EntityManager em;

    private PersistenceUtil persistenceUtil;

    @BeforeAll
    void init() {
        persistenceUtil = new PersistenceUtil(em);
    }


    @Test
    @DisplayName("postId로 해당 글에 달린 PostHashtag들을 조회한다.")
    void testFindPostHashtagByPostId() {
        Post post = Post.builder()
                .build();
        postRepository.save(post);

        PostHashtag postHashtag1 = PostHashtag.builder()
                .post(post)
                .build();
        PostHashtag postHashtag2 = PostHashtag.builder()
                .post(post)
                .build();
        PostHashtag postHashtag3 = PostHashtag.builder()
                .build();
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
        Post post = Post.builder()
                .build();
        postRepository.save(post);

        PostHashtag postHashtag1 = PostHashtag.builder()
                .post(post)
                .build();
        PostHashtag postHashtag2 = PostHashtag.builder()
                .post(post)
                .build();
        PostHashtag postHashtag3 = PostHashtag.builder()
                .build();
        postHashtagRepository.saveAll(List.of(postHashtag1, postHashtag2, postHashtag3));

        persistenceUtil.cleanPersistenceContext();

        postHashtagRepository.deleteAllByPostId(post.getId());

        List<PostHashtag> postHashtagAll = postHashtagRepository.findAll();

        assertThat(postHashtagAll.size()).isEqualTo(1);
        assertThat(postHashtagAll.get(0).getId()).isEqualTo(postHashtag3.getId());
        assertThat(postHashtagAll.get(0).getPost()).isNotEqualTo(post.getId());
    }
}