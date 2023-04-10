package boogi.apiserver.domain.post.post.application;

import boogi.apiserver.builder.*;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.dto.response.CommunityPostsResponse;
import boogi.apiserver.domain.like.application.LikeQueryService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.vo.NullMember;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.dto.LatestCommunityPostDto;
import boogi.apiserver.domain.post.post.dto.dto.UserPostDto;
import boogi.apiserver.domain.post.post.dto.response.HotPostsResponse;
import boogi.apiserver.domain.post.post.dto.response.PostDetailResponse;
import boogi.apiserver.domain.post.post.dto.response.UserPostPageResponse;
import boogi.apiserver.domain.post.postmedia.dao.PostMediaRepository;
import boogi.apiserver.domain.post.postmedia.domain.MediaType;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.dto.PaginationDto;
import boogi.apiserver.global.util.PageableUtil;
import boogi.apiserver.utils.TestTimeReflection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostQueryServiceTest {
    @InjectMocks
    PostQueryService postQueryService;

    @Mock
    PostRepository postRepository;

    @Mock
    MemberRepository memberRepository;

    @Mock
    MemberQueryService memberQueryService;

    @Mock
    LikeQueryService likeQueryService;

    @Mock
    PostMediaRepository postMediaRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    CommunityRepository communityRepository;

    @Nested
    @DisplayName("글 상세 조회할시")
    class GetPostDetailTest {
        private static final String POST_CONTENT = "커뮤니티의 게시글입니다.";
        private static final String MEDIA_URL = "media url";

        @Test
        @DisplayName("글이 작성된 공개 커뮤니티에 비가입상태로 조회시 성공한다.")
        void notJoinedUserRequestSuccess() {
            final User user = TestUser.builder().id(1L).build();

            final Community community = TestCommunity.builder().id(2L).build();

            final Member member = TestMember.builder()
                    .id(3L)
                    .community(community)
                    .user(user)
                    .build();

            final Post post = TestPost.builder()
                    .id(4L)
                    .content(POST_CONTENT)
                    .member(member)
                    .community(community)
                    .likeCount(0)
                    .commentCount(0)
                    .hashtags(List.of())
                    .build();
            TestTimeReflection.setCreatedAt(post, LocalDateTime.now());

            PostMedia postMedia = TestPostMedia.builder()
                    .id(5L)
                    .mediaType(MediaType.IMG)
                    .mediaURL(MEDIA_URL)
                    .build();

            given(postRepository.getPostWithAll(anyLong()))
                    .willReturn(Optional.of(post));
            given(memberQueryService.getViewableMember(anyLong(), any(Community.class)))
                    .willReturn(new NullMember());
            given(postMediaRepository.findByPost(any(Post.class)))
                    .willReturn(List.of(postMedia));

            PostDetailResponse response = postQueryService.getPostDetail(post.getId(), 2L);

            assertThat(response.getId()).isEqualTo(4L);
            assertThat(response.getUser().getId()).isEqualTo(1L);
            assertThat(response.getCommunity().getId()).isEqualTo(2L);
            assertThat(response.getMember().getId()).isEqualTo(3L);
            assertThat(response.getLikeId()).isNull();
            assertThat(response.getCreatedAt()).isEqualTo(post.getCreatedAt());
            assertThat(response.getContent()).isEqualTo(POST_CONTENT);
            assertThat(response.getPostMedias()).extracting("type").containsOnly(MediaType.IMG);
            assertThat(response.getPostMedias()).extracting("url").containsOnly(MEDIA_URL);
            assertThat(response.getLikeCount()).isEqualTo(0);
            assertThat(response.getCommentCount()).isEqualTo(0);
            assertThat(response.getMe()).isFalse();
        }

        @Test
        @DisplayName("글이 작성된 공개 커뮤니티에 가입상태로 조회시 성공한다.")
        void joinedUserRequestSuccess() {
            final Long POST_LIKE_ID = 6L;

            final User user = TestUser.builder().id(1L).build();

            final Community community = TestCommunity.builder().id(2L).build();

            final Member member = TestMember.builder()
                    .id(3L)
                    .community(community)
                    .user(user)
                    .build();

            final Post post = TestPost.builder()
                    .id(4L)
                    .content(POST_CONTENT)
                    .member(member)
                    .community(community)
                    .likeCount(1)
                    .commentCount(0)
                    .hashtags(List.of())
                    .build();
            TestTimeReflection.setCreatedAt(post, LocalDateTime.now());

            PostMedia postMedia = TestPostMedia.builder()
                    .id(5L)
                    .mediaType(MediaType.IMG)
                    .mediaURL(MEDIA_URL)
                    .build();

            given(postRepository.getPostWithAll(anyLong()))
                    .willReturn(Optional.of(post));
            given(memberQueryService.getViewableMember(anyLong(), any(Community.class)))
                    .willReturn(member);
            given(postMediaRepository.findByPost(any(Post.class)))
                    .willReturn(List.of(postMedia));
            given(likeQueryService.getPostLikeId(any(Post.class), any(Member.class)))
                    .willReturn(POST_LIKE_ID);

            PostDetailResponse response = postQueryService.getPostDetail(post.getId(), 1L);

            assertThat(response.getId()).isEqualTo(4L);
            assertThat(response.getUser().getId()).isEqualTo(1L);
            assertThat(response.getCommunity().getId()).isEqualTo(2L);
            assertThat(response.getMember().getId()).isEqualTo(3L);
            assertThat(response.getPostMedias().size()).isEqualTo(1);
            assertThat(response.getPostMedias().get(0).getType()).isEqualTo(MediaType.IMG);
            assertThat(response.getPostMedias().get(0).getUrl()).isEqualTo(MEDIA_URL);
            assertThat(response.getLikeId()).isEqualTo(POST_LIKE_ID);
            assertThat(response.getCreatedAt()).isEqualTo(post.getCreatedAt());
            assertThat(response.getContent()).isEqualTo(POST_CONTENT);
            assertThat(response.getLikeCount()).isEqualTo(1);
            assertThat(response.getCommentCount()).isEqualTo(0);
            assertThat(response.getMe()).isTrue();
        }
    }

    @DisplayName("핫한 게시물을 조회에 성공한다.")
    @Test
    void getHotPostsTestSuccess() {
        final Community community = TestCommunity.builder().id(4L).build();
        List<Post> threeHotPosts = LongStream.rangeClosed(1, 3)
                .mapToObj(l -> TestPost.builder().id(l).content("핫한 게시물입니다" + l).community(community).build())
                .collect(Collectors.toList());

        given(postRepository.getHotPosts()).willReturn(threeHotPosts);

        HotPostsResponse response = postQueryService.getHotPosts();

        assertThat(response.getHots()).hasSize(3);
        assertThat(response.getHots()).extracting("postId")
                .containsOnly(1L, 2L, 3L);
        assertThat(response.getHots()).extracting("content")
                .containsOnly("핫한 게시물입니다1", "핫한 게시물입니다2", "핫한 게시물입니다3");
        assertThat(response.getHots()).extracting("communityId")
                .contains(4L, 4L, 4L);
    }

    @Nested
    @DisplayName("커뮤니티 최신 게시글을 가져올시")
    class GetLatestPostOfCommunityTest {

        @DisplayName("비공개 커뮤니티에 가입되지 않은 경우 null을 반환한다.")
        @Test
        void notViewableMemberSuccess() {
            Community community = TestCommunity.builder()
                    .id(1L)
                    .isPrivate(true)
                    .build();

            Member member = new NullMember();

            List<LatestCommunityPostDto> latestPosts = postQueryService.getLatestPostOfCommunity(member, community);

            assertThat(latestPosts).isNull();
        }

        @DisplayName("해당 커뮤니티에 가입되거나, 공개 커뮤니티일 경우 최신 게시글들을 반환한다.")
        @Test
        void viewableMemberSuccess() {
            final String POST_CONTENT = "게시글 내용입니다.";
            Community community = TestCommunity.builder()
                    .id(1L)
                    .isPrivate(false)
                    .build();

            Member member = TestMember.builder().id(2L).build();

            Post post = TestPost.builder()
                    .id(3L)
                    .content(POST_CONTENT)
                    .build();
            TestTimeReflection.setCreatedAt(post, LocalDateTime.now());

            given(postRepository.getLatestPostOfCommunity(anyLong()))
                    .willReturn(List.of(post));

            List<LatestCommunityPostDto> latestPost = postQueryService.getLatestPostOfCommunity(member, community);

            assertThat(latestPost.size()).isOne();
            assertThat(latestPost.get(0).getId()).isEqualTo(3L);
            assertThat(latestPost.get(0).getContent()).isEqualTo(POST_CONTENT);
            assertThat(latestPost.get(0).getCreatedAt()).isEqualTo(post.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("커뮤니티의 게시글을 가져올시")
    class GetPostsOfCommunity {

        @DisplayName("해당 커뮤니티에 비가입 유저일 경우 멤버타입이 null이다")
        @Test
        void nullMemberSuccess() {
            final String COMMUNITY_NAME = "커뮤니티 이름";
            final String POST_CONTENT = "게시글 내용입니다.";

            User user = TestUser.builder().id(1L).build();

            Member member = TestMember.builder().id(2L).user(user).build();

            Community community = TestCommunity.builder()
                    .id(3L)
                    .isPrivate(true)
                    .communityName(COMMUNITY_NAME)
                    .build();

            Post post = TestPost.builder()
                    .id(4L)
                    .content(POST_CONTENT)
                    .member(member)
                    .community(community)
                    .build();

            given(communityRepository.findCommunityById(anyLong()))
                    .willReturn(community);
            given(memberQueryService.getViewableMember(anyLong(), any(Community.class)))
                    .willReturn(new NullMember());
            Pageable pageable = PageRequest.of(0, 1);
            Slice<Post> postPage = PageableUtil.getSlice(List.of(post), pageable);
            given(postRepository.getPostsOfCommunity(any(Pageable.class), anyLong()))
                    .willReturn(postPage);

            CommunityPostsResponse response = postQueryService.getPostsOfCommunity(pageable, 2L, 2L);

            assertThat(response.getCommunityName()).isEqualTo(COMMUNITY_NAME);
            assertThat(response.getMemberType()).isNull();
            assertThat(response.getPosts()).hasSize(1);
            assertThat(response.getPosts().get(0).getId()).isEqualTo(4L);
            assertThat(response.getPosts().get(0).getContent()).isEqualTo(POST_CONTENT);
            assertThat(response.getPageInfo().getNextPage()).isEqualTo(1);
            assertThat(response.getPageInfo().isHasNext()).isEqualTo(false);
        }

        @DisplayName("해당 커뮤니티에 가입 유저일 경우 해당 멤버의 타입이 반환된다.")
        @Test
        void memberSuccess() {
            final String COMMUNITY_NAME = "커뮤니티 이름";
            final String POST_CONTENT = "게시글 내용입니다.";

            User user = TestUser.builder().id(1L).build();

            Member member = TestMember.builder()
                    .id(2L)
                    .user(user)
                    .memberType(MemberType.NORMAL)
                    .build();

            Community community = TestCommunity.builder()
                    .id(3L)
                    .isPrivate(true)
                    .communityName(COMMUNITY_NAME)
                    .build();

            Post post = TestPost.builder()
                    .id(4L)
                    .content(POST_CONTENT)
                    .member(member)
                    .community(community)
                    .build();

            given(communityRepository.findCommunityById(anyLong()))
                    .willReturn(community);
            given(memberQueryService.getViewableMember(anyLong(), any(Community.class)))
                    .willReturn(member);
            Pageable pageable = PageRequest.of(0, 1);
            Slice<Post> postPage = PageableUtil.getSlice(List.of(post), pageable);
            given(postRepository.getPostsOfCommunity(any(Pageable.class), anyLong()))
                    .willReturn(postPage);

            CommunityPostsResponse response = postQueryService.getPostsOfCommunity(pageable, 2L, 2L);

            assertThat(response.getCommunityName()).isEqualTo(COMMUNITY_NAME);
            assertThat(response.getMemberType()).isEqualTo(MemberType.NORMAL);
            assertThat(response.getPosts()).hasSize(1);
            assertThat(response.getPosts().get(0).getId()).isEqualTo(4L);
            assertThat(response.getPosts().get(0).getContent()).isEqualTo(POST_CONTENT);
            assertThat(response.getPageInfo().getNextPage()).isEqualTo(1);
            assertThat(response.getPageInfo().isHasNext()).isEqualTo(false);
        }
    }

    @Nested
    @DisplayName("유저가 작성한 게시글들 조회시")
    class GetUserPostsTest {

        @Test
        @DisplayName("본인 세션으로 본인의 작성글들을 요청한 경우 본인의 작성글을 페이지네이션해서 조회한다.")
        void myPostRequestWithMySessionUserSuccess() {
            final Long SESSION_USER_ID = 1L;
            final User user = TestUser.builder().id(SESSION_USER_ID).build();

            final Community community = TestCommunity.builder().id(2L).build();

            final Member member1 = TestMember.builder()
                    .id(3L)
                    .user(user)
                    .build();
            final Member member2 = TestMember.builder()
                    .id(4L)
                    .user(user)
                    .build();

            final Post post1 = TestPost.builder()
                    .id(5L)
                    .community(community)
                    .member(member1)
                    .build();
            final Post post2 = TestPost.builder()
                    .id(6L)
                    .community(community)
                    .member(member2)
                    .build();

            given(memberRepository.findMemberIdsForQueryUserPost(anyLong()))
                    .willReturn(List.of(member1.getId(), member2.getId()));

            Pageable pageable = PageRequest.of(0, 2);
            Slice<Post> postPage = PageableUtil.getSlice(List.of(post1, post2), pageable);

            given(postRepository.getUserPostPageByMemberIds(anyList(), any(Pageable.class)))
                    .willReturn(postPage);

            UserPostPageResponse response = postQueryService.getUserPosts(SESSION_USER_ID, SESSION_USER_ID, pageable);

            List<UserPostDto> userPosts = response.getPosts();
            assertThat(userPosts).hasSize(2);
            assertThat(userPosts).extracting("id").containsExactlyInAnyOrder(5L, 6L);
            assertThat(userPosts).extracting("community").extracting("id")
                    .containsExactlyInAnyOrder(2L, 2L);

            PaginationDto pageInfo = response.getPageInfo();
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.isHasNext()).isFalse();
        }

        @Test
        @DisplayName("세션 유저와 요청한 유저가 다른 경우 요청한 유저가 작성한 글을 페이지네이션해서 조회한다.")
        void othersPostRequestWithMySessionUserSuccess() {
            final Long NOT_SESSION_USER_ID = 1L;
            final Long SESSION_USER_ID = 2L;

            final User user = TestUser.builder().id(NOT_SESSION_USER_ID).build();

            final Community community = TestCommunity.builder().id(2L).build();

            final Member member = TestMember.builder()
                    .id(3L)
                    .user(user)
                    .community(community)
                    .build();

            final Post post1 = TestPost.builder()
                    .id(4L)
                    .community(community)
                    .member(member)
                    .build();
            final Post post2 = TestPost.builder()
                    .id(5L)
                    .community(community)
                    .member(member)
                    .build();

            given(memberRepository.findMemberIdsForQueryUserPost(anyLong(), anyLong()))
                    .willReturn(List.of(3L));

            Pageable pageable = PageRequest.of(0, 2);
            Slice<Post> postPage = PageableUtil.getSlice(List.of(post1, post2), pageable);
            given(postRepository.getUserPostPageByMemberIds(anyList(), any(Pageable.class)))
                    .willReturn(postPage);

            UserPostPageResponse response =
                    postQueryService.getUserPosts(NOT_SESSION_USER_ID, SESSION_USER_ID, pageable);

            verify(userRepository, times(1)).findUserById(anyLong());

            List<UserPostDto> userPosts = response.getPosts();
            assertThat(userPosts).hasSize(2);
            assertThat(userPosts).extracting("id").containsExactlyInAnyOrder(4L, 5L);
            assertThat(userPosts).extracting("community").extracting("id")
                    .containsExactlyInAnyOrder(2L, 2L);

            PaginationDto pageInfo = response.getPageInfo();
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.isHasNext()).isFalse();
        }
    }
}