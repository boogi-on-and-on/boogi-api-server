package boogi.apiserver.domain.post.post.dao;

import boogi.apiserver.annotations.CustomDataJpaTest;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.dao.PostHashtagRepository;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.enums.PostListingOrder;
import boogi.apiserver.domain.post.post.dto.request.PostQueryRequest;
import boogi.apiserver.domain.post.post.dto.dto.SearchPostDto;
import boogi.apiserver.domain.post.post.exception.PostNotFoundException;
import boogi.apiserver.domain.post.postmedia.dao.PostMediaRepository;
import boogi.apiserver.domain.post.postmedia.domain.MediaType;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.post.postmedia.dto.dto.PostMediaMetadataDto;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.utils.PersistenceUtil;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;

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
        final Community community1 = TestEmptyEntityGenerator.Community();

        final Community community2 = TestEmptyEntityGenerator.Community();
        ReflectionTestUtils.setField(community2, "isPrivate", true);

        communityRepository.saveAll(List.of(community1, community2));

        final Post post1 = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(post1, "community", community1);
        ReflectionTestUtils.setField(post1, "content", "게시글1");
        ReflectionTestUtils.setField(post1, "likeCount", 10);
        ReflectionTestUtils.setField(post1, "commentCount", 1);
        ReflectionTestUtils.setField(post1, "createdAt", LocalDateTime.now().minusDays(10));

        final Post post2 = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(post2, "community", community2);
        ReflectionTestUtils.setField(post2, "content", "게시글2_리스팅안됨");
        ReflectionTestUtils.setField(post2, "likeCount", 100);
        ReflectionTestUtils.setField(post2, "commentCount", 1);
        ReflectionTestUtils.setField(post2, "createdAt", LocalDateTime.now());

        final Post post3 = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(post3, "community", community1);
        ReflectionTestUtils.setField(post3, "content", "게시글3");
        ReflectionTestUtils.setField(post3, "likeCount", 2);
        ReflectionTestUtils.setField(post3, "commentCount", 1);
        ReflectionTestUtils.setField(post3, "createdAt", LocalDateTime.now());

        final Post post4 = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(post4, "community", community1);
        ReflectionTestUtils.setField(post4, "content", "게시글4");
        ReflectionTestUtils.setField(post4, "likeCount", 2);
        ReflectionTestUtils.setField(post4, "commentCount", 10);
        ReflectionTestUtils.setField(post4, "createdAt", LocalDateTime.now());

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
        final Community community = TestEmptyEntityGenerator.Community();
        communityRepository.save(community);

        final Post p0 = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(p0, "community", community);
        ReflectionTestUtils.setField(p0, "createdAt", LocalDateTime.now().minusDays(1));

        final Post p1 = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(p1, "community", community);
        ReflectionTestUtils.setField(p1, "createdAt", LocalDateTime.now().minusDays(2));

        final Post p2 = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(p2, "community", community);
        ReflectionTestUtils.setField(p2, "createdAt", LocalDateTime.now().minusDays(3));

        final Post p3 = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(p3, "community", community);
        ReflectionTestUtils.setField(p3, "createdAt", LocalDateTime.now().minusDays(4));

        final Post p4 = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(p4, "community", community);
        ReflectionTestUtils.setField(p4, "createdAt", LocalDateTime.now().minusDays(5));


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
        final Community community = TestEmptyEntityGenerator.Community();
        communityRepository.save(community);

        final User user1 = TestEmptyEntityGenerator.User();
        final User user2 = TestEmptyEntityGenerator.User();

        userRepository.saveAll(List.of(user1, user2));

        final Member member1 = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(member1, "user", user1);
        ReflectionTestUtils.setField(member1, "community", community);

        final Member member2 = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(member2, "user", user2);
        ReflectionTestUtils.setField(member2, "community", community);

        memberRepository.saveAll(List.of(member1, member2));

        final Post post1 = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(post1, "community", community);
        ReflectionTestUtils.setField(post1, "member", member1);
        ReflectionTestUtils.setField(post1, "createdAt", LocalDateTime.now());

        final Post post2 = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(post2, "community", community);
        ReflectionTestUtils.setField(post2, "member", member2);
        ReflectionTestUtils.setField(post2, "createdAt", LocalDateTime.now().minusDays(1));

        final Post post3 = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(post3, "community", community);
        ReflectionTestUtils.setField(post3, "member", member2);
        ReflectionTestUtils.setField(post3, "createdAt", LocalDateTime.now().minusDays(2));

        postRepository.saveAll(List.of(post1, post2, post3));

        final PostMedia postMedia1 = TestEmptyEntityGenerator.PostMedia();
        ReflectionTestUtils.setField(postMedia1, "post", post1);
        ReflectionTestUtils.setField(postMedia1, "mediaURL", "123");
        ReflectionTestUtils.setField(postMedia1, "mediaType", MediaType.IMG);

        postMediaRepository.save(postMedia1);

        final PostHashtag p1_t1 = TestEmptyEntityGenerator.PostHashtag();
        ReflectionTestUtils.setField(p1_t1, "post", post2);
        ReflectionTestUtils.setField(p1_t1, "tag", "post2태그1");

        final PostHashtag p1_t2 = TestEmptyEntityGenerator.PostHashtag();
        ReflectionTestUtils.setField(p1_t2, "post", post2);
        ReflectionTestUtils.setField(p1_t2, "tag", "post2태그2");

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
        final Community community1 = TestEmptyEntityGenerator.Community();
        ReflectionTestUtils.setField(community1, "communityName", "안녕");

        final Community community2 = TestEmptyEntityGenerator.Community();
        ReflectionTestUtils.setField(community2, "isPrivate", true);
        ReflectionTestUtils.setField(community2, "communityName", "비밀커뮤니티");

        communityRepository.saveAll(List.of(community1, community2));

        final User user = TestEmptyEntityGenerator.User();
        ReflectionTestUtils.setField(user, "username", "김");
        ReflectionTestUtils.setField(user, "tagNumber", "#0001");
        ReflectionTestUtils.setField(user, "department", "컴공");

        userRepository.save(user);

        final Member member1 = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(member1, "user", user);
        ReflectionTestUtils.setField(member1, "community", community1);

        final Member member2 = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(member2, "user", user);
        ReflectionTestUtils.setField(member2, "community", community2);
        memberRepository.saveAll(List.of(member1, member2));

        final Post p1 = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(p1, "community", community1);
        ReflectionTestUtils.setField(p1, "member", member1);
        ReflectionTestUtils.setField(p1, "commentCount", 1);
        ReflectionTestUtils.setField(p1, "likeCount", 3);
        ReflectionTestUtils.setField(p1, "createdAt", LocalDateTime.now());

        final Post p2 = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(p2, "community", community1);
        ReflectionTestUtils.setField(p2, "member", member1);
        ReflectionTestUtils.setField(p2, "createdAt", LocalDateTime.now().minusDays(1));

        final Post p3 = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(p3, "community", community2);
        ReflectionTestUtils.setField(p3, "member", member2);
        ReflectionTestUtils.setField(p3, "commentCount", 1);
        ReflectionTestUtils.setField(p3, "likeCount", 2);
        ReflectionTestUtils.setField(p3, "createdAt", LocalDateTime.now().minusDays(2));


        postRepository.saveAll(List.of(p1, p2, p3));

        final PostMedia postMedia1 = TestEmptyEntityGenerator.PostMedia();
        ReflectionTestUtils.setField(postMedia1, "post", p3);
        ReflectionTestUtils.setField(postMedia1, "mediaURL", "123");
        ReflectionTestUtils.setField(postMedia1, "mediaType", MediaType.IMG);

        final PostMedia postMedia2 = TestEmptyEntityGenerator.PostMedia();
        ReflectionTestUtils.setField(postMedia2, "post", p3);
        ReflectionTestUtils.setField(postMedia2, "mediaURL", "456");
        ReflectionTestUtils.setField(postMedia2, "mediaType", MediaType.IMG);

        postMediaRepository.saveAll(List.of(postMedia1, postMedia2));

        PostHashtag p1_ht1 = PostHashtag.of("헤헤", p1);
        PostHashtag p1_ht2 = PostHashtag.of("ㅋㅋ", p1);
        PostHashtag p2_ht1 = PostHashtag.of("호호", p2);
        PostHashtag p3_ht1 = PostHashtag.of("헤헤", p3);

        postHashtagRepository.saveAll(List.of(p1_ht1, p1_ht2, p2_ht1, p3_ht1));

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
        final User user = TestEmptyEntityGenerator.User();
        userRepository.save(user);


        final Community community = TestEmptyEntityGenerator.Community();
        communityRepository.save(community);

        final Member member = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(member, "user", user);
        ReflectionTestUtils.setField(member, "community", community);

        memberRepository.save(member);

        final Post post = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(post, "community", community);
        ReflectionTestUtils.setField(post, "member", member);

        postRepository.save(post);

        persistenceUtil.cleanPersistenceContext();

        Post findPost = postRepository
                .getPostWithUserAndMemberAndCommunityByPostId(post.getId())
                .orElse(null);
        if (findPost == null) {
            fail();
        }

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
        final User user = TestEmptyEntityGenerator.User();
        userRepository.save(user);

        final Community community = TestEmptyEntityGenerator.Community();
        communityRepository.save(community);

        final Member member = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(member, "user", user);
        ReflectionTestUtils.setField(member, "community", community);

        memberRepository.save(member);

        final Post post = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(post, "community", community);
        ReflectionTestUtils.setField(post, "member", member);

        postRepository.save(post);

        persistenceUtil.cleanPersistenceContext();

        Post findPost = postRepository
                .getPostWithCommunityAndMemberByPostId(post.getId())
                .orElse(null);
        if (findPost == null) {
            fail();
        }

        assertThat(findPost.getId()).isEqualTo(post.getId());
        assertThat(persistenceUtil.isLoaded(findPost.getMember())).isTrue();
        assertThat(findPost.getMember().getId()).isEqualTo(member.getId());
        assertThat(persistenceUtil.isLoaded(findPost.getMember().getUser())).isFalse();
        assertThat(findPost.getMember().getUser().getId()).isEqualTo(user.getId());
        assertThat(persistenceUtil.isLoaded(findPost.getCommunity())).isTrue();
        assertThat(findPost.getCommunity().getId()).isEqualTo(community.getId());
    }

    @Test
    @DisplayName("memberId들로 해당 멤버들이 작성한 글을 최근 작성일순으로 페이지네이션해서 조회한다.")
    void testGetUserPostPageByMemberIds() {
        final Member member1 = TestEmptyEntityGenerator.Member();
        final Member member2 = TestEmptyEntityGenerator.Member();

        memberRepository.saveAll(List.of(member1, member2));

        final Post post1 = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(post1, "member", member1);
        ReflectionTestUtils.setField(post1, "createdAt", LocalDateTime.now());

        final Post post2 = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(post2, "member", member2);
        ReflectionTestUtils.setField(post2, "createdAt", LocalDateTime.now());

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
        final Community community1 = TestEmptyEntityGenerator.Community();
        final Community community2 = TestEmptyEntityGenerator.Community();
        final Community community3 = TestEmptyEntityGenerator.Community();
        communityRepository.saveAll(List.of(community1, community2, community3));

        final Post post1 = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(post1, "community", community1);
        ReflectionTestUtils.setField(post1, "createdAt", LocalDateTime.now());

        final Post post2 = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(post2, "community", community1);
        ReflectionTestUtils.setField(post2, "createdAt", LocalDateTime.now());

        final Post post3 = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(post3, "community", community2);
        ReflectionTestUtils.setField(post3, "createdAt", LocalDateTime.now());

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
            final Post post = TestEmptyEntityGenerator.Post();
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
}