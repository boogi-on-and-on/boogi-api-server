package boogi.apiserver.domain.post.post.dao;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.dao.PostHashtagRepository;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private PostHashtagRepository postHashtagRepository;

    @Autowired
    EntityManager em;

    @Autowired
    EntityManagerFactory emf;

    @Test
    void getUserPostPage() {
        User user = User.builder()
                .build();
        userRepository.save(user);

        Community community = Community.builder()
                .communityName("커뮤니티1")
                .build();
        communityRepository.save(community);

        Member member1 = Member.builder()
                .user(user)
                .community(community)
                .build();

        Member member2 = Member.builder()
                .user(user)
                .community(community)
                .build();

        memberRepository.saveAll(List.of(member1, member2));

        Post post1 = Post.builder()
                .community(community)
                .content("게시글1")
                .member(member1)
                .build();
        post1.setCreatedAt(LocalDateTime.now().minusDays(1));

        Post post2 = Post.builder()
                .community(community)
                .content("게시글2")
                .member(member2)
                .build();
        post2.setCreatedAt(LocalDateTime.now().minusDays(2));

        Post post3 = Post.builder()
                .community(community)
                .content("게시글3")
                .member(member2)
                .build();
        post3.setCreatedAt(LocalDateTime.now().minusDays(3));

        postRepository.saveAll(List.of(post1, post2, post3));

        PostHashtag hashtagOfPost1 = PostHashtag.builder()
                .post(post1)
                .tag("게시글1의 해시태그1")
                .build();

        PostHashtag hashtagOfPost2 = PostHashtag.builder()
                .post(post1)
                .tag("게시글1의 해시태그2")
                .build();

        postHashtagRepository.saveAll(List.of(hashtagOfPost1, hashtagOfPost2));

        em.flush();
        em.clear();

        Pageable pageable = PageRequest.of(0, 2);

        //when
        Page<Post> postPage = postRepository.getUserPostPage(pageable, user.getId());
        Page<Post> postPage1 = postRepository.getUserPostPage(PageRequest.of(1, 2), user.getId());

        //then
        //first page
        List<Post> posts = postPage.getContent();
        Post first = posts.get(0);
        Post second = posts.get(1);

        assertThat(first.getId()).isEqualTo(post1.getId());
        assertThat(second.getId()).isEqualTo(post2.getId());

        assertThat(posts.size()).isEqualTo(2);
        assertThat(first.getCreatedAt()).isAfter(second.getCreatedAt());
        assertThat(first.getHashtags().size()).isEqualTo(2);

        assertThat(postPage.getTotalElements()).isEqualTo(3); // 전체 데이터 수
        assertThat(postPage.hasNext()).isTrue(); //다음 페이지가 있는지

        assertThat(emf.getPersistenceUnitUtil().isLoaded(first.getHashtags().get(0))).isTrue();
    }

    @Test
    void getHotPosts() {
        //given
        Community community1 = Community.builder().build();
        Community community2 = Community.builder()
                .isPrivate(true)
                .build();
        communityRepository.saveAll(List.of(community1, community2));

        Post post1 = Post.builder()
                .community(community1)
                .content("게시글1")
                .likeCount(10)
                .commentCount(1)
                .build();
        post1.setCreatedAt(LocalDateTime.now().minusDays(10));

        Post post2 = Post.builder()
                .community(community2)
                .content("게시글2_리스팅안됨")
                .likeCount(100)
                .commentCount(1)
                .build();
        post2.setCreatedAt(LocalDateTime.now());


        Post post3 = Post.builder()
                .community(community1)
                .content("게시글3")
                .likeCount(2)
                .commentCount(1)
                .build();
        post3.setCreatedAt(LocalDateTime.now());

        Post post4 = Post.builder()
                .community(community1)
                .content("게시글4")
                .likeCount(2)
                .commentCount(10)
                .build();
        post4.setCreatedAt(LocalDateTime.now());

        postRepository.saveAll(List.of(post1, post2, post3, post4));

        em.flush();
        em.clear();

        //when
        List<Post> posts = postRepository.getHotPosts();

        //then
        assertThat(posts.size()).isEqualTo(2);
        assertThat(posts.stream().anyMatch(p -> p.getCreatedAt().isBefore(LocalDateTime.now().minusDays(4)))).isFalse();

        Post first = posts.get(0);
        Post second = posts.get(1);

        assertThat(first.getId()).isEqualTo(post4.getId());
        assertThat(second.getId()).isEqualTo(post3.getId());
    }

    @Test
    void 가입한_커뮤니티의_최근_글() {
        User user = User.builder().build();
        userRepository.save(user);

        Community community1 = Community.builder().build();
        Community community2 = Community.builder().build();
        communityRepository.saveAll(List.of(community1, community2));

        Member secondCreatedMember = Member.builder()
                .user(user)
                .community(community1)
                .build();
        secondCreatedMember.setCreatedAt(LocalDateTime.now());

        Member firstCreatedMember = Member.builder()
                .user(user)
                .community(community2)
                .build();
        firstCreatedMember.setCreatedAt(LocalDateTime.now().minusDays(1));
        memberRepository.saveAll(List.of(secondCreatedMember, firstCreatedMember));

        Post post1OfCommunity1 = Post.builder()
                .content("p1-c1")
                .member(secondCreatedMember)
                .community(community1)
                .build();
        // id가 auto_increment를 보장하기 위해서 saveAll이 아닌, 개별적 save이용
        // 엔티티의 save 순서에 유의해야함.
        postRepository.save(post1OfCommunity1);

        Post post2OfCommunity1 = Post.builder()
                .content("p2-c1")
                .member(firstCreatedMember)
                .community(community1)
                .build();
        postRepository.save(post2OfCommunity1);

        Post post1OfCommunity2 = Post.builder()
                .content("p1-c2")
                .member(secondCreatedMember)
                .community(community2)
                .build();
        postRepository.save(post1OfCommunity2);

        em.flush();
        em.clear();

        //when
        List<Post> posts = postRepository.getLatestPostOfCommunity(user.getId());

        //then
        assertThat(posts.size()).isEqualTo(2);

        Post first = posts.get(0);
        Post second = posts.get(1);
        assertThat(first.getMember().getId()).isEqualTo(firstCreatedMember.getId());
        assertThat(second.getMember().getId()).isEqualTo(secondCreatedMember.getId());

        assertThat(first.getContent()).isEqualTo("p2-c1");
        assertThat(second.getContent()).isEqualTo("p1-c2");
    }
}