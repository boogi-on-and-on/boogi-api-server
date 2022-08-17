package boogi.apiserver.domain.post.postmedia.dao;

import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.utils.PersistenceUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PostMediaRepositoryTest {

    @Autowired
    PostMediaRepository postMediaRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    EntityManager em;

    private PersistenceUtil persistenceUtil;

    @BeforeAll
    void init() {
        persistenceUtil = new PersistenceUtil(em);
    }


    @Test
    @DisplayName("커뮤니티에서 가장 최근 글의 Id들을 입력받아 각 글의 PostMedia 중에서 가장 최근 1개에 대한 PostMedia들을 조회한다.")
    @Disabled
        //MYSQL native 쿼리로 인한 h2 테스트 불가능
    void testGetPostMediasByLatestPostIds() {
        Post post1 = Post.builder()
                .build();
        Post post2 = Post.builder()
                .build();
        postRepository.saveAll(List.of(post1, post2));

        PostMedia postMedia1 = PostMedia.builder()
                .post(post1)
                .build();
        postMedia1.setCreatedAt(LocalDateTime.now());
        PostMedia postMedia2 = PostMedia.builder()
                .post(post1)
                .build();
        postMedia2.setCreatedAt(LocalDateTime.now());
        postMediaRepository.saveAll(List.of(postMedia1, postMedia2));

        persistenceUtil.cleanPersistenceContext();

        List<PostMedia> postMedias = postMediaRepository
                .getPostMediasByLatestPostIds(List.of(post1.getId(), post2.getId()));

        assertThat(postMedias.size()).isEqualTo(1);
        assertThat(postMedias.get(0).getId()).isEqualTo(postMedia1.getId());
        assertThat(postMedias.get(0).getPost().getId()).isEqualTo(post1.getId());
    }

    @Test
    @DisplayName("PostMedia의 UUID로 Post가 세팅되지 않은 PostMedia들을 조회한다.")
    void testFindUnmappedPostMediasByUUIDs() {
        Post post = Post.builder()
                .build();
        postRepository.save(post);

        PostMedia postMedia1 = PostMedia.builder()
                .uuid("1234")
                .build();
        PostMedia postMedia2 = PostMedia.builder()
                .uuid("2345")
                .build();
        PostMedia postMedia3 = PostMedia.builder()
                .uuid("3456")
                .post(post)
                .build();
        postMediaRepository.saveAll(List.of(postMedia1, postMedia2, postMedia3));

        persistenceUtil.cleanPersistenceContext();

        List<String> postMediaUUIDs = List.of(postMedia1.getUuid(), postMedia2.getUuid(), postMedia3.getUuid());
        List<PostMedia> unmappedPostMedias = postMediaRepository
                .findUnmappedPostMediasByUUIDs(postMediaUUIDs);

        assertThat(unmappedPostMedias.size()).isEqualTo(2);
        assertThat(unmappedPostMedias.get(0).getId()).isEqualTo(postMedia1.getId());
        assertThat(unmappedPostMedias.get(0).getPost()).isNull();
        assertThat(unmappedPostMedias.get(0).getUuid()).isEqualTo(postMedia1.getUuid());
        assertThat(unmappedPostMedias.get(1).getId()).isEqualTo(postMedia2.getId());
        assertThat(unmappedPostMedias.get(1).getPost()).isNull();
        assertThat(unmappedPostMedias.get(1).getUuid()).isEqualTo(postMedia2.getUuid());
    }
}