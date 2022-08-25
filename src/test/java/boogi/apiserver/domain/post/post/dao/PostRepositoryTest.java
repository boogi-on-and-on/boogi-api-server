package boogi.apiserver.domain.post.post.dao;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.dao.PostHashtagRepository;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.PostQueryRequest;
import boogi.apiserver.domain.post.post.dto.SearchPostDto;
import boogi.apiserver.domain.post.post.dto.request_enum.PostListingOrder;
import boogi.apiserver.domain.post.postmedia.dao.PostMediaRepository;
import boogi.apiserver.domain.post.postmedia.domain.MediaType;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.post.postmedia.dto.PostMediaMetadataDto;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.utils.PersistenceUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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

    @BeforeAll
    void init() {
        persistenceUtil = new PersistenceUtil(em);
    }

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

        PostMedia postMedia = PostMedia.builder()
                .post(post1)
                .mediaURL("123")
                .mediaType(MediaType.IMG)
                .build();
        postMediaRepository.save(postMedia);

        PostHashtag hashtagOfPost1 = PostHashtag.builder()
                .post(post1)
                .tag("게시글1의 해시태그1")
                .build();

        PostHashtag hashtagOfPost2 = PostHashtag.builder()
                .post(post1)
                .tag("게시글1의 해시태그2")
                .build();

        postHashtagRepository.saveAll(List.of(hashtagOfPost1, hashtagOfPost2));

        persistenceUtil.cleanPersistenceContext();

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

        assertThat(first.getPostMedias().get(0).getId()).isEqualTo(postMedia.getId());

        assertThat(posts.size()).isEqualTo(2);
        assertThat(first.getCreatedAt()).isAfter(second.getCreatedAt());
        assertThat(first.getHashtags().size()).isEqualTo(2);

        assertThat(postPage.getTotalElements()).isEqualTo(3); // 전체 데이터 수
        assertThat(postPage.hasNext()).isTrue(); //다음 페이지가 있는지

        assertThat(persistenceUtil.isLoaded(first.getHashtags().get(0))).isTrue();
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
        Community community = Community.builder().build();
        communityRepository.save(community);

        Post p0 = Post.builder().community(community).build();
        p0.setCreatedAt(LocalDateTime.now().minusDays(1));

        Post p1 = Post.builder().community(community).build();
        p1.setCreatedAt(LocalDateTime.now().minusDays(2));

        Post p2 = Post.builder().community(community).build();
        p2.setCreatedAt(LocalDateTime.now().minusDays(3));

        Post p3 = Post.builder().community(community).build();
        p3.setCreatedAt(LocalDateTime.now().minusDays(4));

        Post p4 = Post.builder().community(community).build();
        p4.setCreatedAt(LocalDateTime.now().minusDays(5));
//        p4.setCanceledAt(LocalDateTime.now());

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
        Community community = Community.builder().build();
        communityRepository.save(community);

        User user1 = User.builder().build();
        User user2 = User.builder().build();

        userRepository.saveAll(List.of(user1, user2));

        Member member1 = Member.builder()
                .community(community)
                .user(user1)
                .build();
        Member member2 = Member.builder()
                .community(community)
                .user(user2)
                .build();
        memberRepository.saveAll(List.of(member1, member2));

        Post post1 = Post.builder()
                .community(community)
                .member(member1)
                .build();
        post1.setCreatedAt(LocalDateTime.now());
        Post post2 = Post.builder()
                .community(community)
                .member(member2)
                .build();
        post2.setCreatedAt(LocalDateTime.now().minusDays(1));

        Post post3 = Post.builder()
                .community(community)
                .member(member2)
                .build();
        post3.setCreatedAt(LocalDateTime.now().minusDays(2));

        postRepository.saveAll(List.of(post1, post2, post3));

        PostMedia postMedia1 = PostMedia.builder()
                .post(post1)
                .mediaURL("123")
                .mediaType(MediaType.IMG)
                .build();
        postMediaRepository.save(postMedia1);

        PostHashtag p1_t1 = PostHashtag.builder()
                .post(post2)
                .tag("post2태그1")
                .build();
        PostHashtag p1_t2 = PostHashtag.builder()
                .post(post2)
                .tag("post2태그2")
                .build();
        postHashtagRepository.saveAll(List.of(p1_t1, p1_t2));

        persistenceUtil.cleanPersistenceContext();

        PageRequest page = PageRequest.of(0, 10);
        Page<Post> postPage = postRepository.getPostsOfCommunity(page, community.getId());

        List<Post> posts = postPage.getContent();
        Post first = posts.get(0);
        Post second = posts.get(1);
        Post third = posts.get(2);

        assertThat(posts.size()).isEqualTo(3);
        assertThat(first.getId()).isEqualTo(post1.getId());
        assertThat(second.getId()).isEqualTo(post2.getId());
        assertThat(third.getId()).isEqualTo(post3.getId());

        assertThat(first.getPostMedias().get(0).getId()).isEqualTo(postMedia1.getId());

        assertThat(second.getHashtags().size()).isEqualTo(2);
        assertThat(persistenceUtil.isLoaded(second.getHashtags().get(0))).isTrue();
    }

    @Test
    void getSearchedPosts() {
        //given
        Community community1 = Community.builder()
                .communityName("안녕")
                .build();

        Community community2 = Community.builder()
                .isPrivate(true)
                .communityName("비밀커뮤니티")
                .build();
        communityRepository.saveAll(List.of(community1, community2));

        User user = User.builder()
                .username("김")
                .tagNumber("#0001")
                .department("컴공")
                .build();
        userRepository.save(user);

        Member member1 = Member.builder()
                .user(user)
                .community(community1)
                .build();

        Member member2 = Member.builder()
                .user(user)
                .community(community2)
                .build();
        memberRepository.saveAll(List.of(member1, member2));

        Post p1 = Post.builder()
                .community(community1)
                .member(member1)
                .commentCount(1)
                .likeCount(3)
                .hashtags(new ArrayList<>())
                .build();
        p1.setCreatedAt(LocalDateTime.now());

        Post p2 = Post.builder()
                .community(community1)
                .member(member1)
                .hashtags(new ArrayList<>())
                .build();
        p2.setCreatedAt(LocalDateTime.now().minusDays(1));

        Post p3 = Post.builder()
                .community(community2)
                .member(member2)
                .commentCount(1)
                .likeCount(2)
                .hashtags(new ArrayList<>())
                .build();
        p3.setCreatedAt(LocalDateTime.now().minusDays(2));

        postRepository.saveAll(List.of(p1, p2, p3));

        PostMedia postMedia1 = PostMedia.builder()
                .post(p3)
                .mediaType(MediaType.IMG)
                .mediaURL("123")
                .build();
        PostMedia postMedia2 = PostMedia.builder()
                .post(p3)
                .mediaType(MediaType.IMG)
                .mediaURL("456")
                .build();
        postMediaRepository.saveAll(List.of(postMedia1, postMedia2));

        PostHashtag p1_ht1 = PostHashtag.of("헤헤", p1);
        PostHashtag p1_ht2 = PostHashtag.of("ㅋㅋ", p1);
        PostHashtag p2_ht1 = PostHashtag.of("호호", p2);
        PostHashtag p3_ht1 = PostHashtag.of("헤헤", p3);

        postHashtagRepository.saveAll(List.of(p1_ht1, p1_ht2, p2_ht1, p3_ht1));

        PostQueryRequest request = PostQueryRequest.builder()
                .keyword("헤헤")
                .order(PostListingOrder.NEWER)
                .build();

        persistenceUtil.cleanPersistenceContext();

        //when
        Page<SearchPostDto> postPage = postRepository.getSearchedPosts(PageRequest.of(0, 3), request, user.getId());

        //then
        List<SearchPostDto> posts = postPage.getContent();
        long count = postPage.getTotalElements();

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
        User user = User.builder()
                .build();
        userRepository.save(user);

        Community community = Community.builder()
                .build();
        communityRepository.save(community);

        Member member = Member.builder()
                .user(user)
                .community(community)
                .build();
        memberRepository.save(member);

        Post post = Post.builder()
                .member(member)
                .community(community)
                .build();
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
        User user = User.builder()
                .build();
        userRepository.save(user);

        Community community = Community.builder()
                .build();
        communityRepository.save(community);

        Member member = Member.builder()
                .user(user)
                .community(community)
                .build();
        memberRepository.save(member);

        Post post = Post.builder()
                .member(member)
                .community(community)
                .build();
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
        Member member1 = Member.builder()
                .build();
        Member member2 = Member.builder()
                .build();
        memberRepository.saveAll(List.of(member1, member2));

        Post post1 = Post.builder()
                .member(member1)
                .build();
        post1.setCreatedAt(LocalDateTime.now());
        Post post2 = Post.builder()
                .member(member2)
                .build();
        post2.setCreatedAt(LocalDateTime.now());
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
    @Disabled
    //MYSQL native 쿼리로 인한 h2 테스트 불가능
    void testGetLatestPostByCommunityIds() {
        Community community1 = Community.builder()
                .build();
        Community community2 = Community.builder()
                .build();
        Community community3 = Community.builder()
                .build();
        communityRepository.saveAll(List.of(community1, community2, community3));

        Post post1 = Post.builder()
                .community(community1)
                .build();
        post1.setCreatedAt(LocalDateTime.now());
        Post post2 = Post.builder()
                .community(community1)
                .build();
        post2.setCreatedAt(LocalDateTime.now());
        Post post3 = Post.builder()
                .community(community2)
                .build();
        post3.setCreatedAt(LocalDateTime.now());
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
}