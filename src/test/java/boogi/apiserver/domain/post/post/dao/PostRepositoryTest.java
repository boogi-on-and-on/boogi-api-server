package boogi.apiserver.domain.post.post.dao;

import boogi.apiserver.builder.*;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.dao.PostHashtagRepository;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
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
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.utils.RepositoryTest;
import boogi.apiserver.utils.TestTimeReflection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostRepositoryTest extends RepositoryTest {

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

    @Test
    @DisplayName("커뮤니티별 가장 최근 글 1개씩 조회한다.")
    void getLatestPostByCommunityIds() {
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

        cleanPersistenceContext();

        Set<Long> communityIds = Set.of(community1.getId(), community2.getId(), community3.getId());
        List<Post> latestPosts = postRepository.getLatestPostByCommunityIds(communityIds);

        assertThat(latestPosts).hasSize(2);
        assertThat(latestPosts).extracting("id").containsExactly(post3.getId(), post2.getId());
        assertThat(latestPosts).extracting("community").extracting("id")
                .containsExactly(community2.getId(), community1.getId());
    }

    @Nested
    @DisplayName("ID로 게시글 조회시")
    class findPostById {

        @DisplayName("성공")
        @Test
        void success() {
            final Post post = TestPost.builder().build();
            postRepository.save(post);

            cleanPersistenceContext();

            final Post findPost = postRepository.findPostById(post.getId());
            assertThat(findPost.getId()).isEqualTo(post.getId());
        }

        @DisplayName("throw PostNotFoundException")
        @Test
        void throwException() {
            assertThatThrownBy(() -> {
                postRepository.findPostById(1L);
            }).isInstanceOf(PostNotFoundException.class);
        }
    }

    @Test
    @DisplayName("핫한 게시글 3개를 좋아요 많은 순, 댓글수 많은 순으로 조회한다.")
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

        cleanPersistenceContext();

        //when
        List<Post> posts = postRepository.getHotPosts();

        //then
        assertThat(posts).hasSize(2);
        assertThat(posts).extracting("id").containsExactly(post4.getId(), post3.getId());

        boolean hasCreatedBeforeFourDaysAgo = posts.stream()
                .map(Post::getCreatedAt)
                .anyMatch(pc -> pc.isBefore(LocalDateTime.now().minusDays(4)));
        assertThat(hasCreatedBeforeFourDaysAgo).isFalse();

        assertThat(posts).extracting("community").extracting("isPrivate")
                .containsOnly(false);

        assertThat(posts).extracting("likeCount").containsExactly(2, 2);
        assertThat(posts).extracting("commentCount").containsExactly(10, 1);
    }

    @Test
    @DisplayName("해당 커뮤니티의 최신 게시글 5개를 최신순으로 조회한다.")
    void getLatestPostOfCommunity() {
        //given
        final Community community = TestCommunity.builder().build();
        communityRepository.save(community);

        List<Post> posts = IntStream.range(0, 6)
                .mapToObj(i -> {
                    Post post = TestPost.builder()
                            .community(community)
                            .build();
                    TestTimeReflection.setCreatedAt(post, LocalDateTime.now().minusHours(i));
                    return post;
                }).collect(Collectors.toList());
        postRepository.saveAll(posts);

        cleanPersistenceContext();

        //when
        List<Post> latestPosts = postRepository.getLatestPostOfCommunity(community.getId());

        List<Long> expectedPostIds = posts.subList(0, 5).stream().map(Post::getId).collect(Collectors.toList());

        //then
        assertThat(latestPosts).hasSize(5);
        assertThat(latestPosts).extracting("id").containsExactlyElementsOf(expectedPostIds);
        assertThat(latestPosts).extracting("community").extracting("id")
                .containsOnly(community.getId());
    }

    @Test
    @DisplayName("postId로 Post를 Member, User, Community와 함께 fetch join해서 조회한다.")
    void getPostWithAll() {
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

        cleanPersistenceContext();

        Post findPost = postRepository.getPostWithAll(post.getId())
                .orElseGet(Assertions::fail);

        assertThat(findPost.getId()).isEqualTo(post.getId());

        assertThat(isLoaded(findPost.getMember())).isTrue();
        assertThat(findPost.getMember().getId()).isEqualTo(member.getId());

        assertThat(isLoaded(findPost.getMember().getUser())).isTrue();
        assertThat(findPost.getMember().getUser().getId()).isEqualTo(user.getId());

        assertThat(isLoaded(findPost.getCommunity())).isTrue();
        assertThat(findPost.getCommunity().getId()).isEqualTo(community.getId());
    }

    @Test
    @DisplayName("해당 커뮤니티의 게시글들을 페이지네이션해서 최신순으로 조회한다.")
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

        cleanPersistenceContext();

        Pageable pageable = PageRequest.of(0, 10);
        Slice<Post> postPage = postRepository.getPostsOfCommunity(pageable, community.getId());

        List<Post> posts = postPage.getContent();

        assertThat(posts).hasSize(3);
        assertThat(posts).extracting("id").containsExactly(post1.getId(), post2.getId(), post3.getId());

        Post first = posts.get(0);
        Post second = posts.get(1);
        assertThat(isLoaded(first.getMember())).isTrue();
        assertThat(isLoaded(first.getMember().getUser())).isTrue();

        assertThat(first.getPostMedias()).extracting("id").containsExactly(postMedia1.getId());
        assertThat(second.getHashtags()).hasSize(2);
        assertThat(isLoaded(second.getHashtags().get(0))).isTrue();
    }

    @Test
    @DisplayName("memberId들로 해당 멤버들이 작성한 글을 최근 작성일순으로 페이지네이션해서 조회한다.")
    void getUserPostPageByMemberIds() {
        final Member member1 = TestMember.builder().build();
        final Member member2 = TestMember.builder().build();
        memberRepository.saveAll(List.of(member1, member2));

        final Post post1 = TestPost.builder()
                .member(member1)
                .build();
        TestTimeReflection.setCreatedAt(post1, LocalDateTime.now().minusHours(1));

        final Post post2 = TestPost.builder()
                .member(member2)
                .build();
        TestTimeReflection.setCreatedAt(post2, LocalDateTime.now());

        postRepository.saveAll(List.of(post1, post2));

        cleanPersistenceContext();

        Pageable pageable = PageRequest.of(0, 2);
        List<Long> memberIds = List.of(member1.getId(), member2.getId());

        Slice<Post> userPostPage = postRepository.getUserPostPageByMemberIds(memberIds, pageable);

        assertThat(userPostPage.hasNext()).isFalse();

        List<Post> userPosts = userPostPage.getContent();

        assertThat(userPosts).hasSize(2);
        assertThat(userPosts).extracting("id").containsExactly(post2.getId(), post1.getId());
        assertThat(userPosts).extracting("member").extracting("id")
                .containsExactly(member2.getId(), member1.getId());
    }

    @Test
    @DisplayName("공개 커뮤니티와 요청한 유저가 가입한 커뮤니티 내에서 해시태그 기반으로 게시글을 페이지네이션해서 조회한다.")
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

        cleanPersistenceContext();

        //when
        PostQueryRequest request = new PostQueryRequest("헤헤", PostListingOrder.NEWER);
        Pageable pageable = PageRequest.of(0, 3);
        Slice<SearchPostDto> postPage = postRepository.getSearchedPosts(pageable, request, user.getId());

        //then
        List<SearchPostDto> posts = postPage.getContent();

        assertThat(posts).hasSize(2);
        assertThat(posts).extracting("id").containsExactly(p1.getId(), p3.getId());

        SearchPostDto first = posts.get(0);
        SearchPostDto second = posts.get(1);

        assertThat(first.getHashtags()).containsExactlyInAnyOrder("ㅋㅋ", "헤헤");
        assertThat(second.getHashtags()).containsExactlyInAnyOrder("헤헤");

        assertThat(second.getPostMedias()).extracting("url")
                .containsExactlyInAnyOrder("123", "456");
    }
}