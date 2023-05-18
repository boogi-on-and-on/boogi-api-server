package boogi.apiserver.domain.post.post.application;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.dto.response.CommunityPostsResponse;
import boogi.apiserver.domain.community.community.repository.CommunityRepository;
import boogi.apiserver.domain.like.application.LikeQuery;
import boogi.apiserver.domain.member.application.MemberQuery;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.repository.MemberRepository;
import boogi.apiserver.domain.member.vo.NullMember;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.dto.LatestCommunityPostDto;
import boogi.apiserver.domain.post.post.dto.dto.UserPostDto;
import boogi.apiserver.domain.post.post.dto.response.HotPostsResponse;
import boogi.apiserver.domain.post.post.dto.response.PostDetailResponse;
import boogi.apiserver.domain.post.post.dto.response.UserPostPageResponse;
import boogi.apiserver.domain.post.post.repository.PostRepository;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.post.postmedia.repository.PostMediaRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.repository.UserRepository;
import boogi.apiserver.global.dto.PaginationDto;
import boogi.apiserver.global.util.PageableUtil;
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

import java.util.List;
import java.util.Optional;

import static boogi.apiserver.utils.fixture.CommunityFixture.ENGLISH;
import static boogi.apiserver.utils.fixture.CommunityFixture.POCS;
import static boogi.apiserver.utils.fixture.MemberFixture.*;
import static boogi.apiserver.utils.fixture.PostFixture.*;
import static boogi.apiserver.utils.fixture.PostMediaFixture.POSTMEDIA1;
import static boogi.apiserver.utils.fixture.TimeFixture.STANDARD;
import static boogi.apiserver.utils.fixture.UserFixture.SUNDO;
import static boogi.apiserver.utils.fixture.UserFixture.YONGJIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostQueryTest {
    @InjectMocks
    PostQuery postQuery;

    @Mock
    PostRepository postRepository;

    @Mock
    MemberRepository memberRepository;

    @Mock
    MemberQuery memberQuery;

    @Mock
    LikeQuery likeQuery;

    @Mock
    PostMediaRepository postMediaRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    CommunityRepository communityRepository;

    private final User user = SUNDO.toUser(1L);
    private final Community community = POCS.toCommunity(2L, List.of());
    private final Member member = SUNDO_POCS.toMember(3L, user, community);

    @Nested
    @DisplayName("글 상세 조회할시")
    class GetPostDetailTest {
        @Test
        @DisplayName("글이 작성된 공개 커뮤니티에 비가입상태로 조회시 성공한다.")
        void notJoinedUserRequestSuccess() {
            Post post = POST1.toPost(4L, member, community, List.of(), List.of());
            PostMedia postMedia = POSTMEDIA1.toPostMedia(5L, post);

            given(postRepository.getPostWithAll(anyLong())).willReturn(Optional.of(post));
            given(memberQuery.getViewableMember(anyLong(), any(Community.class)))
                    .willReturn(new NullMember());
            given(postMediaRepository.findByPost(any(Post.class)))
                    .willReturn(List.of(postMedia));

            PostDetailResponse response = postQuery.getPostDetail(post.getId(), 2L);

            assertThat(response.getId()).isEqualTo(4L);
            assertThat(response.getUser().getId()).isEqualTo(1L);
            assertThat(response.getCommunity().getId()).isEqualTo(2L);
            assertThat(response.getMember().getId()).isEqualTo(3L);
            assertThat(response.getLikeId()).isNull();
            assertThat(response.getCreatedAt()).isEqualTo(post.getCreatedAt());
            assertThat(response.getContent()).isEqualTo(POST1.content);
            assertThat(response.getPostMedias()).extracting("type").containsOnly(POSTMEDIA1.mediaType);
            assertThat(response.getPostMedias()).extracting("url").containsOnly(POSTMEDIA1.mediaURL);
            assertThat(response.getLikeCount()).isEqualTo(POST1.likeCount);
            assertThat(response.getCommentCount()).isEqualTo(POST1.commentCount);
            assertThat(response.getMe()).isFalse();
        }

        @Test
        @DisplayName("글이 작성된 공개 커뮤니티에 가입상태로 조회시 성공한다.")
        void joinedUserRequestSuccess() {
            final Long POST_LIKE_ID = 6L;

            Post post = POST1.toPost(4L, member, community, List.of(), List.of());
            PostMedia postMedia = POSTMEDIA1.toPostMedia(5L, post);

            given(postRepository.getPostWithAll(anyLong())).willReturn(Optional.of(post));
            given(memberQuery.getViewableMember(anyLong(), any(Community.class))).willReturn(member);
            given(postMediaRepository.findByPost(any(Post.class))).willReturn(List.of(postMedia));
            given(likeQuery.getPostLikeId(any(Post.class), any(Member.class))).willReturn(POST_LIKE_ID);

            PostDetailResponse response = postQuery.getPostDetail(post.getId(), 1L);

            assertThat(response.getId()).isEqualTo(4L);
            assertThat(response.getUser().getId()).isEqualTo(1L);
            assertThat(response.getCommunity().getId()).isEqualTo(2L);
            assertThat(response.getMember().getId()).isEqualTo(3L);
            assertThat(response.getPostMedias()).hasSize(1);
            assertThat(response.getPostMedias().get(0).getType()).isEqualTo(POSTMEDIA1.mediaType);
            assertThat(response.getPostMedias().get(0).getUrl()).isEqualTo(POSTMEDIA1.mediaURL);
            assertThat(response.getLikeId()).isEqualTo(POST_LIKE_ID);
            assertThat(response.getCreatedAt()).isEqualTo(STANDARD);
            assertThat(response.getContent()).isEqualTo(POST1.content);
            assertThat(response.getLikeCount()).isEqualTo(POST1.likeCount);
            assertThat(response.getCommentCount()).isEqualTo(POST1.commentCount);
            assertThat(response.getMe()).isTrue();
        }
    }

    @DisplayName("핫한 게시물을 조회에 성공한다.")
    @Test
    void getHotPostsTestSuccess() {
        Post post1 = POST1.toPost(1L, member, community, List.of(), List.of());
        Post post2 = POST2.toPost(2L, member, community, List.of(), List.of());
        Post post3 = POST3.toPost(3L, member, community, List.of(), List.of());

        given(postRepository.getHotPosts()).willReturn(List.of(post1, post2, post3));

        HotPostsResponse response = postQuery.getHotPosts();

        assertThat(response.getHots()).hasSize(3);
        assertThat(response.getHots()).extracting("postId")
                .containsExactly(1L, 2L, 3L);
        assertThat(response.getHots()).extracting("content")
                .containsExactly(POST1.content, POST2.content, POST3.content);
        assertThat(response.getHots()).extracting("communityId")
                .containsOnly(2L);
    }

    @Nested
    @DisplayName("커뮤니티 최신 게시글을 가져올시")
    class GetLatestPostOfCommunityTest {

        @DisplayName("비공개 커뮤니티에 가입되지 않은 경우 null을 반환한다.")
        @Test
        void notViewableMemberSuccess() {
            Community community = ENGLISH.toCommunity(4L, List.of());
            Member member = new NullMember();

            List<LatestCommunityPostDto> latestPosts = postQuery.getLatestPostOfCommunity(member, community);

            assertThat(latestPosts).isNull();
        }

        @DisplayName("해당 커뮤니티에 가입되거나, 공개 커뮤니티일 경우 최신 게시글들을 반환한다.")
        @Test
        void viewableMemberSuccess() {
            Post post = POST1.toPost(4L, member, community, List.of(), List.of());

            given(postRepository.getLatestPostOfCommunity(anyLong())).willReturn(List.of(post));

            List<LatestCommunityPostDto> latestPost = postQuery.getLatestPostOfCommunity(member, community);

            assertThat(latestPost.size()).isOne();
            assertThat(latestPost.get(0).getId()).isEqualTo(4L);
            assertThat(latestPost.get(0).getContent()).isEqualTo(POST1.content);
            assertThat(latestPost.get(0).getCreatedAt()).isEqualTo(STANDARD);
        }
    }

    @Nested
    @DisplayName("커뮤니티의 게시글을 가져올시")
    class GetPostsOfCommunity {

        @DisplayName("해당 커뮤니티에 비가입 유저일 경우 멤버타입이 null이다")
        @Test
        void nullMemberSuccess() {
            Community community = ENGLISH.toCommunity(4L, List.of());
            Member member = YONGJIN_ENGLISH.toMember(5L, user, community);
            Post post = POST1.toPost(6L, member, community, List.of(), List.of());

            given(communityRepository.findCommunityById(anyLong())).willReturn(community);
            given(memberQuery.getViewableMember(anyLong(), any(Community.class)))
                    .willReturn(new NullMember());

            Pageable pageable = PageRequest.of(0, 1);
            Slice<Post> postPage = PageableUtil.getSlice(List.of(post), pageable);
            given(postRepository.getPostsOfCommunity(any(Pageable.class), anyLong()))
                    .willReturn(postPage);

            CommunityPostsResponse response = postQuery.getPostsOfCommunity(pageable, 2L, 2L);

            assertThat(response.getCommunityName()).isEqualTo(ENGLISH.communityName);
            assertThat(response.getMemberType()).isNull();
            assertThat(response.getPosts()).hasSize(1);
            assertThat(response.getPosts().get(0).getId()).isEqualTo(6L);
            assertThat(response.getPosts().get(0).getContent()).isEqualTo(POST1.content);
            assertThat(response.getPageInfo().getNextPage()).isEqualTo(1);
            assertThat(response.getPageInfo().isHasNext()).isFalse();
        }

        @DisplayName("해당 커뮤니티에 가입 유저일 경우 해당 멤버의 타입이 반환된다.")
        @Test
        void memberSuccess() {
            Post post = POST1.toPost(4L, member, community, List.of(), List.of());

            given(communityRepository.findCommunityById(anyLong())).willReturn(community);
            given(memberQuery.getViewableMember(anyLong(), any(Community.class))).willReturn(member);

            Pageable pageable = PageRequest.of(0, 1);
            Slice<Post> postPage = PageableUtil.getSlice(List.of(post), pageable);
            given(postRepository.getPostsOfCommunity(any(Pageable.class), anyLong()))
                    .willReturn(postPage);

            CommunityPostsResponse response = postQuery.getPostsOfCommunity(pageable, 2L, 2L);

            assertThat(response.getCommunityName()).isEqualTo(POCS.communityName);
            assertThat(response.getMemberType()).isEqualTo(SUNDO_POCS.memberType);
            assertThat(response.getPosts()).hasSize(1);
            assertThat(response.getPosts().get(0).getId()).isEqualTo(4L);
            assertThat(response.getPosts().get(0).getContent()).isEqualTo(POST1.content);
            assertThat(response.getPageInfo().getNextPage()).isEqualTo(1);
            assertThat(response.getPageInfo().isHasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("유저가 작성한 게시글들 조회시")
    class GetUserPostsTest {

        @Test
        @DisplayName("본인 세션으로 본인의 작성글들을 요청한 경우 본인의 작성글을 페이지네이션해서 조회한다.")
        void myPostRequestWithMySessionUserSuccess() {
            Community community1 = ENGLISH.toCommunity(1L, List.of());
            User user = YONGJIN.toUser(2L);
            Member member1 = YONGJIN_POCS.toMember(3L, user, community);
            Member member2 = YONGJIN_ENGLISH.toMember(4L, user, community1);
            Post post1 = POST1.toPost(5L, member1, community, List.of(), List.of());
            Post post2 = POST2.toPost(6L, member2, community1, List.of(), List.of());

            given(memberRepository.findMemberIdsForQueryUserPost(anyLong()))
                    .willReturn(List.of(member1.getId(), member2.getId()));

            Pageable pageable = PageRequest.of(0, 2);
            Slice<Post> postPage = PageableUtil.getSlice(List.of(post1, post2), pageable);
            given(postRepository.getUserPostPageByMemberIds(anyList(), any(Pageable.class)))
                    .willReturn(postPage);

            UserPostPageResponse response = postQuery.getUserPosts(2L, 2L, pageable);

            List<UserPostDto> userPosts = response.getPosts();
            assertThat(userPosts).hasSize(2);
            assertThat(userPosts).extracting("id").containsExactlyInAnyOrder(5L, 6L);
            assertThat(userPosts).extracting("community").extracting("id")
                    .containsExactlyInAnyOrder(1L, 2L);

            PaginationDto pageInfo = response.getPageInfo();
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.isHasNext()).isFalse();
        }

        @Test
        @DisplayName("세션 유저와 요청한 유저가 다른 경우 요청한 유저가 작성한 글을 페이지네이션해서 조회한다.")
        void othersPostRequestWithMySessionUserSuccess() {
            Community community1 = ENGLISH.toCommunity(1L, List.of());
            User user2 = YONGJIN.toUser(2L);
            Member member1 = YONGJIN_POCS.toMember(3L, user2, community);
            Member member2 = YONGJIN_ENGLISH.toMember(4L, user2, community1);
            Post post1 = POST1.toPost(5L, member1, community, List.of(), List.of());
            Post post2 = POST2.toPost(6L, member2, community1, List.of(), List.of());

            given(memberRepository.findMemberIdsForQueryUserPost(anyLong(), anyLong()))
                    .willReturn(List.of(3L));

            Pageable pageable = PageRequest.of(0, 2);
            Slice<Post> postPage = PageableUtil.getSlice(List.of(post1, post2), pageable);
            given(postRepository.getUserPostPageByMemberIds(anyList(), any(Pageable.class)))
                    .willReturn(postPage);

            UserPostPageResponse response =
                    postQuery.getUserPosts(2L, 1L, pageable);

            verify(userRepository, times(1)).findUserById(anyLong());

            List<UserPostDto> userPosts = response.getPosts();
            assertThat(userPosts).hasSize(2);
            assertThat(userPosts).extracting("id").containsExactlyInAnyOrder(5L, 6L);
            assertThat(userPosts).extracting("community").extracting("id")
                    .containsExactlyInAnyOrder(1L, 2L);

            PaginationDto pageInfo = response.getPageInfo();
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.isHasNext()).isFalse();
        }
    }
}