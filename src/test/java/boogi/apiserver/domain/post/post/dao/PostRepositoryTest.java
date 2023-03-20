package boogi.apiserver.domain.post.post.dao;

import boogi.apiserver.annotations.CustomDataJpaTest;
import boogi.apiserver.builder.*;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.dao.PostHashtagRepository;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtags;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.dto.SearchPostDto;
import boogi.apiserver.domain.post.post.dto.enums.PostListingOrder;
import boogi.apiserver.domain.post.post.dto.request.PostQueryRequest;
import boogi.apiserver.domain.post.post.exception.PostNotFoundException;
import boogi.apiserver.domain.post.postmedia.dao.PostMediaRepository;
import boogi.apiserver.domain.post.postmedia.domain.MediaType;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.post.postmedia.dto.dto.PostMediaMetadataDto;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.utils.PersistenceUtil;
import boogi.apiserver.utils.TestTimeReflection;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CustomDataJpaTest
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
    private PostMediaRepository postMediaRepository;

    @Autowired
    EntityManager em;

    private PersistenceUtil persistenceUtil;

    @BeforeEach
    void init() {
        persistenceUtil = new PersistenceUtil(em);
    }

    @Test
    void getHotPosts() {
        //given
        final Community community1 = TestCommunity.builder().build();
        final Community community2 = TestCommunity.builder().isPrivate(true).build();
        communityRepository.saveAll(List.of(community1, community2));

        final Post post1 = TestPost.builder()
                .community(community1)
                .content("게시글1의 내용입니다.")
                .likeCount(10)
                .commentCount(1)
                .build();
        TestTimeReflection.setCreatedAt(post1, LocalDateTime.now().minusDays(10));

        final Post post2 = TestPost.builder()
                .community(community2)
                .content("게시글2의 내용입니다. 리스팅X")
                .likeCount(100)
                .commentCount(1)
                .build();
        TestTimeReflection.setCreatedAt(post2, LocalDateTime.now());

        final Post post3 = TestPost.builder()
                .community(community1)
                .content("게시글3의 내용입니다.")
                .likeCount(2)
                .commentCount(1)
                .build();
        TestTimeReflection.setCreatedAt(post3, LocalDateTime.now());

        final Post post4 = TestPost.builder()
                .community(community1)
                .content("게시글4의 내용입니다.")
                .likeCount(2)
                .commentCount(10)
                .build();
        TestTimeReflection.setCreatedAt(post4, LocalDateTime.now());

        postRepository.saveAll(List.of(post1, post2, post3, post4));

        persistenceUtil.cleanPersistenceContext();

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

//    @Test
//    void 가입한_커뮤니티의_최근_글() {
//        User user = User.builder().build();
//        userRepository.save(user);
//
//        Community community1 = Community.builder().build();
//        Community community2 = Community.builder().build();
//        communityRepository.saveAll(List.of(community1, community2));
//
//        Member secondCreatedMember = Member.builder()
//                .user(user)
//                .community(community1)
//                .build();
//        secondCreatedMember.setCreatedAt(LocalDateTime.now());
//
//        Member firstCreatedMember = Member.builder()
//                .user(user)
//                .community(community2)
//                .build();
//        firstCreatedMember.setCreatedAt(LocalDateTime.now().minusDays(1));
//        memberRepository.saveAll(List.of(secondCreatedMember, firstCreatedMember));
//
//        Post post1OfCommunity1 = Post.builder()
//                .content("p1-c1")
//                .member(secondCreatedMember)
//                .community(community1)
//                .build();
//        // id가 auto_increment를 보장하기 위해서 saveAll이 아닌, 개별적 save이용
//        // 엔티티의 save 순서에 유의해야함.
//        postRepository.save(post1OfCommunity1);
//
//        Post post2OfCommunity1 = Post.builder()
//                .content("p2-c1")
//                .member(firstCreatedMember)
//                .community(community1)
//                .build();
//        postRepository.save(post2OfCommunity1);
//
//        Post post1OfCommunity2 = Post.builder()
//                .content("p1-c2")
//                .member(secondCreatedMember)
//                .community(community2)
//                .build();
//        postRepository.save(post1OfCommunity2);
//
//        em.flush();
//        em.clear();
//
//        //when
//        List<Post> posts = postRepository.getLatestPostOfUserJoinedCommunities(user.getId());
//
//        //then
//        assertThat(posts.size()).isEqualTo(2);
//
//        Post first = posts.get(0);
//        Post second = posts.get(1);
//        assertThat(first.getMember().getId()).isEqualTo(firstCreatedMember.getId());
//        assertThat(second.getMember().getId()).isEqualTo(secondCreatedMember.getId());
//
//        assertThat(first.getContent()).isEqualTo("p2-c1");
//        assertThat(second.getContent()).isEqualTo("p1-c2");
//    }

    @Test
    @Disabled
    void getLatestPostOfCommunity() {
        //given
        final Community community = TestCommunity.builder().build();
        communityRepository.save(community);

        final Post p0 = TestPost.builder()
                .community(community)
                .build();
        TestTimeReflection.setCreatedAt(p0, LocalDateTime.now().minusDays(1));

        final Post p1 = TestPost.builder()
                .community(community)
                .build();
        TestTimeReflection.setCreatedAt(p1, LocalDateTime.now().minusDays(2));

        final Post p2 = TestPost.builder()
                .community(community)
                .build();
        TestTimeReflection.setCreatedAt(p2, LocalDateTime.now().minusDays(3));

        final Post p3 = TestPost.builder()
                .community(community)
                .build();
        TestTimeReflection.setCreatedAt(p3, LocalDateTime.now().minusDays(4));

        final Post p4 = TestPost.builder()
                .community(community)
                .build();
        TestTimeReflection.setCreatedAt(p4, LocalDateTime.now().minusDays(5));

        postRepository.saveAll(List.of(p0, p1, p2, p3, p4));

        //when
        List<Post> posts = postRepository.getLatestPostOfCommunity(community.getId());

        //then
        assertThat(posts.size()).isEqualTo(4);
        assertThat(posts.get(0)).isEqualTo(p0);
        assertThat(posts.get(1)).isEqualTo(p1);
        assertThat(posts.get(2)).isEqualTo(p2);
        assertThat(posts.get(3)).isEqualTo(p3);
    }

    @Test
    void getPostsOfCommunity() {
        final Community community = TestCommunity.builder().build();
        communityRepository.save(community);

        final User user1 = TestUser.builder().build();
        final User user2 = TestUser.builder().build();
        userRepository.saveAll(List.of(user1, user2));

        final Member member1 = TestMember.builder()
                .user(user1)
                .community(community)
                .build();
        final Member member2 = TestMember.builder()
                .user(user2)
                .community(community)
                .build();
        memberRepository.saveAll(List.of(member1, member2));

        final Post post1 = TestPost.builder()
                .community(community)
                .member(member1)
                .build();
        TestTimeReflection.setCreatedAt(post1, LocalDateTime.now());

        final Post post2 = TestPost.builder()
                .community(community)
                .member(member2)
                .build();
        TestTimeReflection.setCreatedAt(post2, LocalDateTime.now().minusDays(1));

        final Post post3 = TestPost.builder()
                .community(community)
                .member(member2)
                .build();
        TestTimeReflection.setCreatedAt(post3, LocalDateTime.now().minusDays(2));

        postRepository.saveAll(List.of(post1, post2, post3));

        final PostMedia postMedia1 = TestPostMedia.builder()
                .post(post1)
                .mediaURL("123")
                .mediaType(MediaType.IMG)
                .build();
        postMediaRepository.save(postMedia1);

        final PostHashtag p1_t1 = TestPostHashtag.builder()
                .post(post2)
                .tag("post2태그1")
                .build();
        final PostHashtag p1_t2 = TestPostHashtag.builder()
                .post(post2)
                .tag("post2태그2")
                .build();
        postHashtagRepository.saveAll(List.of(p1_t1, p1_t2));

        persistenceUtil.cleanPersistenceContext();

        PageRequest page = PageRequest.of(0, 10);
        Slice<Post> postSlice = postRepository.getPostsOfCommunity(page, community.getId());

        List<Post> posts = postSlice.getContent();
        Post first = posts.get(0);
        Post second = posts.get(1);
        Post third = posts.get(2);

        assertThat(posts.size()).isEqualTo(3);
        assertThat(first.getId()).isEqualTo(post1.getId());
        assertThat(second.getId()).isEqualTo(post2.getId());
        assertThat(third.getId()).isEqualTo(post3.getId());

        assertThat(first.getPostMedias().getValues().get(0).getId()).isEqualTo(postMedia1.getId());

        assertThat(second.getHashtags().getValues().size()).isEqualTo(2);
        assertThat(persistenceUtil.isLoaded(second.getHashtags().getValues().get(0))).isTrue();
    }

    @Test
    void getSearchedPosts() {
        //given
        final Community community1 = TestCommunity.builder().communityName("안녕").build();
        final Community community2 = TestCommunity.builder()
                .isPrivate(true)
                .communityName("비밀커뮤니티")
                .build();
        communityRepository.saveAll(List.of(community1, community2));

        final User user = TestUser.builder()
                .username("홍길동")
                .tagNumber("#0001")
                .department("컴퓨터공학과")
                .build();
        userRepository.save(user);

        final Member member1 = TestMember.builder()
                .user(user)
                .community(community1)
                .build();
        final Member member2 = TestMember.builder()
                .user(user)
                .community(community2)
                .build();
        memberRepository.saveAll(List.of(member1, member2));

        final Post p1 = TestPost.builder()
                .community(community1)
                .member(member1)
                .commentCount(1)
                .likeCount(3)
                .build();
        TestTimeReflection.setCreatedAt(p1, LocalDateTime.now());

        final Post p2 = TestPost.builder()
                .community(community1)
                .member(member1)
                .build();
        TestTimeReflection.setCreatedAt(p2, LocalDateTime.now().minusDays(1));

        final Post p3 = TestPost.builder()
                .community(community2)
                .member(member2)
                .commentCount(1)
                .likeCount(2)
                .build();
        TestTimeReflection.setCreatedAt(p3, LocalDateTime.now().minusDays(2));

        postRepository.saveAll(List.of(p1, p2, p3));

        final PostMedia postMedia1 = TestPostMedia.builder()
                .post(p3)
                .mediaURL("123")
                .mediaType(MediaType.IMG)
                .build();
        final PostMedia postMedia2 = TestPostMedia.builder()
                .post(p3)
                .mediaURL("456")
                .mediaType(MediaType.IMG)
                .build();
        postMediaRepository.saveAll(List.of(postMedia1, postMedia2));

        p1.addTags(List.of("헤헤", "ㅋㅋ"));
        p2.addTags(List.of("호호"));
        p3.addTags(List.of("헤헤"));

        PostQueryRequest request = new PostQueryRequest("헤헤", PostListingOrder.NEWER);

        persistenceUtil.cleanPersistenceContext();

        //when
        Slice<SearchPostDto> postPage = postRepository.getSearchedPosts(PageRequest.of(0, 3), request, user.getId());

        //then
        List<SearchPostDto> posts = postPage.getContent();
        long count = postPage.getContent().size();

        SearchPostDto first = posts.get(0);
        SearchPostDto second = posts.get(1);
        assertThat(first.getId()).isEqualTo(p1.getId());
        assertThat(second.getId()).isEqualTo(p3.getId());

        assertThat(first.getHashtags())
                .containsExactlyInAnyOrderElementsOf(List.of("ㅋㅋ", "헤헤"));

        assertThat(second.getHashtags())
                .containsExactlyInAnyOrderElementsOf(List.of("헤헤"));

        assertThat(count).isEqualTo(2);

        List<String> urls = second.getPostMedias().stream()
                .map(PostMediaMetadataDto::getUrl)
                .collect(Collectors.toList());

        assertThat(urls).containsExactlyInAnyOrderElementsOf(List.of("123", "456"));
    }

    @Test
    @DisplayName("postId로 Post를 Member, User, Community와 함께 fetch join해서 조회한다.")
    void testGetPostWithUserAndMemberAndCommunityByPostId() {
        final User user = TestUser.builder().build();
        userRepository.save(user);

        final Community community = TestCommunity.builder().build();
        communityRepository.save(community);

        final Member member = TestMember.builder()
                .user(user)
                .community(community)
                .build();
        memberRepository.save(member);

        final Post post = TestPost.builder()
                .community(community)
                .member(member)
                .build();
        postRepository.save(post);

        persistenceUtil.cleanPersistenceContext();

        Post findPost = postRepository
                .getPostWithAll(post.getId())
                .orElseGet(Assertions::fail);

        assertThat(findPost.getId()).isEqualTo(post.getId());
        assertThat(persistenceUtil.isLoaded(findPost.getMember())).isTrue();
        assertThat(findPost.getMember().getId()).isEqualTo(member.getId());
        assertThat(persistenceUtil.isLoaded(findPost.getMember().getUser())).isTrue();
        assertThat(findPost.getMember().getUser().getId()).isEqualTo(user.getId());
        assertThat(persistenceUtil.isLoaded(findPost.getCommunity())).isTrue();
        assertThat(findPost.getCommunity().getId()).isEqualTo(community.getId());
    }

    @Test
    @DisplayName("postId로 Post를 Member, Community와 함께 fetch join해서 조회한다.")
    void testGetPostWithCommunityAndMemberByPostId() {
        final User user = TestUser.builder().build();
        userRepository.save(user);

        final Community community = TestCommunity.builder().build();
        communityRepository.save(community);

        final Member member = TestMember.builder()
                .user(user)
                .community(community)
                .build();
        memberRepository.save(member);

        final Post post = TestPost.builder()
                .community(community)
                .member(member)
                .build();
        postRepository.save(post);

        persistenceUtil.cleanPersistenceContext();

//        Post findPost = postRepository
//                .getPostWithCommunityAndMemberByPostId(post.getId())
//                .orElseGet(Assertions::fail);
//
//        assertThat(findPost.getId()).isEqualTo(post.getId());
//        assertThat(persistenceUtil.isLoaded(findPost.getMember())).isTrue();
//        assertThat(findPost.getMember().getId()).isEqualTo(member.getId());
//        assertThat(persistenceUtil.isLoaded(findPost.getMember().getUser())).isFalse();
//        assertThat(findPost.getMember().getUser().getId()).isEqualTo(user.getId());
//        assertThat(persistenceUtil.isLoaded(findPost.getCommunity())).isTrue();
//        assertThat(findPost.getCommunity().getId()).isEqualTo(community.getId());
    }

    @Test
    @DisplayName("memberId들로 해당 멤버들이 작성한 글을 최근 작성일순으로 페이지네이션해서 조회한다.")
    void testGetUserPostPageByMemberIds() {
        final Member member1 = TestMember.builder().build();
        final Member member2 = TestMember.builder().build();
        memberRepository.saveAll(List.of(member1, member2));

        final Post post1 = TestPost.builder()
                .member(member1)
                .build();
        TestTimeReflection.setCreatedAt(post1, LocalDateTime.now());

        final Post post2 = TestPost.builder()
                .member(member2)
                .build();
        TestTimeReflection.setCreatedAt(post2, LocalDateTime.now());

        postRepository.saveAll(List.of(post1, post2));

        persistenceUtil.cleanPersistenceContext();

        Pageable pageable = PageRequest.of(0, 2);
        List<Long> memberIds = List.of(member1.getId(), member2.getId());

        Slice<Post> userPostPage = postRepository.getUserPostPageByMemberIds(memberIds, pageable);

        List<Post> userPosts = userPostPage.getContent();
        assertThat(userPosts.size()).isEqualTo(2);
        assertThat(userPosts.get(0).getId()).isEqualTo(post2.getId());
        assertThat(userPosts.get(0).getMember().getId()).isEqualTo(member2.getId());
        assertThat(userPosts.get(1).getId()).isEqualTo(post1.getId());
        assertThat(userPosts.get(1).getMember().getId()).isEqualTo(member1.getId());
    }

    @Test
    @DisplayName("커뮤니티별 가장 최근 글 1개씩 조회한다.")
    void testGetLatestPostByCommunityIds() {
        final Community community1 = TestCommunity.builder().build();
        final Community community2 = TestCommunity.builder().build();
        final Community community3 = TestCommunity.builder().build();
        communityRepository.saveAll(List.of(community1, community2, community3));

        final Post post1 = TestPost.builder()
                .community(community1)
                .build();
        TestTimeReflection.setCreatedAt(post1, LocalDateTime.now());

        final Post post2 = TestPost.builder()
                .community(community1)
                .build();
        TestTimeReflection.setCreatedAt(post2, LocalDateTime.now());

        final Post post3 = TestPost.builder()
                .community(community2)
                .build();
        TestTimeReflection.setCreatedAt(post3, LocalDateTime.now());

        postRepository.saveAll(List.of(post1, post2, post3));

        persistenceUtil.cleanPersistenceContext();

        Set<Long> communityIds = Set.of(community1.getId(), community2.getId(), community3.getId());
        List<Post> latestPosts = postRepository
                .getLatestPostByCommunityIds(communityIds);

        assertThat(latestPosts.size()).isEqualTo(2);
        assertThat(latestPosts.get(0).getId()).isEqualTo(post3.getId());
        assertThat(latestPosts.get(0).getCommunity().getId()).isEqualTo(community2.getId());
        assertThat(latestPosts.get(1).getId()).isEqualTo(post2.getId());
        assertThat(latestPosts.get(1).getCommunity().getId()).isEqualTo(community1.getId());
    }

    @Nested
    @DisplayName("findByPostId 디폴트 메서드 테스트")
    class findByPostId {

        @DisplayName("성공")
        @Test
        void success() {
            final Post post = TestPost.builder().build();
            postRepository.save(post);

            persistenceUtil.cleanPersistenceContext();

            final Post findPost = postRepository.findByPostId(post.getId());
            assertThat(findPost.getId()).isEqualTo(post.getId());
        }

        @DisplayName("throw PostNotFoundException")
        @Test
        void throwException() {
            assertThatThrownBy(() -> {
                postRepository.findByPostId(1L);
            }).isInstanceOf(PostNotFoundException.class);
        }
    }

    @Test
    void test1() {
        final Post post = TestPost.builder().build();


//        final PostHashtag p1 = TestPostHashtag.builder().build();
//        final PostHashtag p2 = TestPostHashtag.builder().build();
//        postHashtagRepository.saveAll(List.of(p1, p2));

        post.addTags(List.of("tag1", "tag2"));

        postRepository.save(post);

        persistenceUtil.cleanPersistenceContext();

        final Post findPost = postRepository.findByPostId(post.getId());
        final PostHashtags tags = findPost.getHashtags();
        tags.getValues().forEach(i-> System.out.println("i = " + i));


//        assertThat(findPost.getHashtags())
    }

    @Test
    void test2() {
        final Post post = TestPost.builder().build();
        post.addTags(List.of("tag1", "tag2"));

        postRepository.save(post);

        persistenceUtil.cleanPersistenceContext();

        postRepository.delete(post);

        persistenceUtil.cleanPersistenceContext();

        final List<PostHashtag> all = postHashtagRepository.findAll();
        System.out.println("all.size() = " + all.size());


//        assertThat(findPost.getHashtags())
    }

}