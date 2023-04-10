package boogi.apiserver.domain.post.postmedia.dao;

import boogi.apiserver.annotations.CustomDataJpaTest;
import boogi.apiserver.builder.TestPost;
import boogi.apiserver.builder.TestPostMedia;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.utils.PersistenceUtil;
import boogi.apiserver.utils.TestTimeReflection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@CustomDataJpaTest
class PostMediaRepositoryTest {

    @Autowired
    PostMediaRepository postMediaRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    EntityManager em;

    private PersistenceUtil persistenceUtil;

    @BeforeEach
    void init() {
        persistenceUtil = new PersistenceUtil(em);
    }


    @Test
    @DisplayName("커뮤니티에서 가장 최근 글의 Id들을 입력받아 각 글의 가장 먼저 추가된 PostMedia 1개씩 조회한다.")
    void getPostMediasByLatestPostIds() {
        Post post1 = TestPost.builder().build();
        Post post2 = TestPost.builder().build();
        postRepository.saveAll(List.of(post1, post2));

        final PostMedia postMedia1 = TestPostMedia.builder().post(post1).build();
        TestTimeReflection.setCreatedAt(postMedia1, LocalDateTime.now().minusHours(1));

        final PostMedia postMedia2 = TestPostMedia.builder().post(post1).build();
        TestTimeReflection.setCreatedAt(postMedia2, LocalDateTime.now());

        postMediaRepository.saveAll(List.of(postMedia1, postMedia2));

        persistenceUtil.cleanPersistenceContext();

        List<Long> postIds = List.of(post1.getId(), post2.getId());
        List<PostMedia> postMedias = postMediaRepository.getPostMediasByLatestPostIds(postIds);

        assertThat(postMedias).hasSize(1);
        assertThat(postMedias).extracting("id").containsExactly(postMedia1.getId());
        assertThat(postMedias).extracting("post").extracting("id")
                .containsExactly(post1.getId());
    }

    @Test
    @DisplayName("PostMedia의 UUID들로 Post가 세팅되지 않은 PostMedia들을 조회한다.")
    void findUnmappedPostMedias() {
        Post post = TestPost.builder().build();
        postRepository.save(post);

        final PostMedia postMedia1 = TestPostMedia.builder().uuid("1234").build();
        final PostMedia postMedia2 = TestPostMedia.builder().uuid("2345").build();
        final PostMedia postMedia3 = TestPostMedia.builder().uuid("3456").post(post).build();
        postMediaRepository.saveAll(List.of(postMedia1, postMedia2, postMedia3));

        persistenceUtil.cleanPersistenceContext();

        List<String> postMediaUUIDs = List.of(postMedia1.getUuid(), postMedia2.getUuid(), postMedia3.getUuid());
        List<PostMedia> unmappedPostMedias = postMediaRepository.findUnmappedPostMedias(postMediaUUIDs);

        assertThat(unmappedPostMedias).hasSize(2);
        assertThat(unmappedPostMedias).extracting("id")
                .containsExactly(postMedia1.getId(), postMedia2.getId());
        assertThat(unmappedPostMedias).extracting("post").containsOnlyNulls();
        assertThat(unmappedPostMedias).extracting("uuid").containsExactly("1234", "2345");
    }
}