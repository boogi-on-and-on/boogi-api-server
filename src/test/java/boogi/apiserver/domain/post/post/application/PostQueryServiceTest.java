package boogi.apiserver.domain.post.post.application;

import boogi.apiserver.builder.*;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.like.application.LikeQueryService;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.exception.NotViewableMemberException;
import boogi.apiserver.domain.member.vo.NullMember;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.dto.UserPostDto;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

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


    //todo: Page 객체 직접 생성해서 테스트하기

//    @Test
//    void 유저가_가입한_최근_커뮤니티_글() {
//        //given
//        Community community = Community.builder()
//                .id(1L)
//                .communityName("커뮤니티1")
//                .build();
//
//        Post post = Post.builder()
//                .id(2L)
//                .community(community)
//                .content("글")
//                .likeCount(1)
//                .commentCount(2)
//                .postMedias(List.of(PostMedia.builder().mediaURL("111").build()))
//                .hashtags(List.of(PostHashtag.builder().tag("해시테그").build()))
//                .build();
//        post.setCreatedAt(LocalDateTime.now());
//
//        //when
//        given(postRepository.getLatestPostOfUserJoinedCommunities(anyLong()))
//                .willReturn(List.of(post));
//
//        //then
//        List<LatestPostOfUserJoinedCommunity> postDtos = postQueryService.getPostsOfUserJoinedCommunity(anyLong());
//
//        LatestPostOfUserJoinedCommunity dto = postDtos.get(0);
//
//        assertThat(dto.getId()).isEqualTo(1L);
//        assertThat(dto.getName()).isEqualTo("커뮤니티1");
//        assertThat(dto.getPost().getId()).isEqualTo(2L);
//    }

    @Nested
    @DisplayName("글 상세 조회할시")
    class GetPostDetailTest {

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
                    .content("커뮤니티의 게시글입니다.")
                    .member(member)
                    .community(community)
                    .likeCount(0)
                    .commentCount(0)
                    .hashtags(List.of())
                    .build();
            TestTimeReflection.setCreatedAt(post, LocalDateTime.now());
            given(postRepository.getPostWithAll(anyLong()))
                    .willReturn(Optional.of(post));

            given(memberQueryService.getViewableMember(anyLong(), any(Community.class)))
                    .willReturn(new NullMember());

            given(postMediaRepository.findByPostId(anyLong()))
                    .willReturn(List.of());

            given(likeQueryService.getPostLikeIdForView(anyLong(), any(Member.class)))
                    .willReturn(null);

            PostDetailResponse postDetailResponse = postQueryService.getPostDetail(post.getId(), 2L);

            assertThat(postDetailResponse.getId()).isEqualTo(4L);
            assertThat(postDetailResponse.getUser().getId()).isEqualTo(1L);
            assertThat(postDetailResponse.getMember().getId()).isEqualTo(3L);
            assertThat(postDetailResponse.getCommunity().getId()).isEqualTo(2L);
            assertThat(postDetailResponse.getLikeId()).isNull();
            assertThat(postDetailResponse.getCreatedAt()).isEqualTo(post.getCreatedAt());
            assertThat(postDetailResponse.getContent()).isEqualTo("커뮤니티의 게시글입니다.");
            assertThat(postDetailResponse.getLikeCount()).isEqualTo(0);
            assertThat(postDetailResponse.getCommentCount()).isEqualTo(0);
            assertThat(postDetailResponse.getMe()).isFalse();
        }

        @Test
        @DisplayName("글이 작성된 공개 커뮤니티에 가입상태로 조회시 성공한다.")
        void joinedUserRequestSuccess() {
            final User user = TestUser.builder().id(1L).build();

            final Community community = TestCommunity.builder().id(1L).build();

            final Member member = TestMember.builder()
                    .id(1L)
                    .community(community)
                    .user(user)
                    .build();

            final Post post = TestPost.builder()
                    .id(1L)
                    .content("커뮤니티의 게시글입니다.")
                    .member(member)
                    .community(community)
                    .likeCount(1)
                    .commentCount(0)
                    .hashtags(List.of())
                    .build();
            TestTimeReflection.setCreatedAt(post, LocalDateTime.now());

            given(postRepository.getPostWithAll(anyLong()))
                    .willReturn(Optional.of(post));

            given(memberQueryService.getViewableMember(anyLong(), any(Community.class)))
                    .willReturn(member);

            final PostMedia postMedia = TestPostMedia.builder()
                    .id(1L)
                    .mediaURL("url")
                    .mediaType(MediaType.IMG)
                    .build();
            given(postMediaRepository.findByPostId(anyLong()))
                    .willReturn(List.of(postMedia));

            final Like like = TestLike.builder().id(1L).build();
            given(likeQueryService.getPostLikeIdForView(anyLong(), any(Member.class)))
                    .willReturn(like.getId());

            PostDetailResponse postDetailResponse = postQueryService.getPostDetail(post.getId(), 1L);

            assertThat(postDetailResponse.getId()).isEqualTo(post.getId());
            assertThat(postDetailResponse.getUser().getId()).isEqualTo(user.getId());
            assertThat(postDetailResponse.getMember().getId()).isEqualTo(member.getId());
            assertThat(postDetailResponse.getCommunity().getId()).isEqualTo(community.getId());
            assertThat(postDetailResponse.getPostMedias().size()).isEqualTo(1);
            assertThat(postDetailResponse.getPostMedias().get(0).getType()).isEqualTo(postMedia.getMediaType());
            assertThat(postDetailResponse.getPostMedias().get(0).getUrl()).isEqualTo(postMedia.getMediaURL());
            assertThat(postDetailResponse.getLikeId()).isEqualTo(like.getId());
            assertThat(postDetailResponse.getCreatedAt()).isEqualTo(post.getCreatedAt());
            assertThat(postDetailResponse.getContent()).isEqualTo(post.getContent());
            assertThat(postDetailResponse.getLikeCount()).isEqualTo(1);
            assertThat(postDetailResponse.getCommentCount()).isEqualTo(0);
            assertThat(postDetailResponse.getMe()).isTrue();
        }

        @Test
        @DisplayName("글이 작성된 비공개 커뮤니티에 비가입상태로 조회시 NotViewableMemberException 발생한다.")
        void notJoinedPrivateCommunityFail() {
            final Community community = TestCommunity.builder()
                    .id(1L)
                    .isPrivate(true)
                    .build();

            final Post post = TestPost.builder()
                    .id(1L)
                    .community(community)
                    .build();
            given(postRepository.getPostWithAll(anyLong()))
                    .willReturn(Optional.of(post));

            given(memberQueryService.getViewableMember(anyLong(), any(Community.class)))
                    .willThrow(NotViewableMemberException.class);

            assertThatThrownBy(() -> postQueryService.getPostDetail(post.getId(), 1L))
                    .isInstanceOf(NotViewableMemberException.class);
        }
    }

    @Nested
    @DisplayName("유저가 작성한 게시글들 조회시")
    class GetUserPostsTest {

        @Test
        @DisplayName("본인 세션으로 본인의 작성글들을 요청한 경우 본인의 작성글을 페이지네이션해서 조회한다.")
        void myPostRequestWithMySessionUserSuccess() {
            final User user = TestUser.builder().id(1L).build();

            final Community community = TestCommunity.builder().id(1L).build();

            final Member member1 = TestMember.builder()
                    .id(1L)
                    .user(user)
                    .build();
            final Member member2 = TestMember.builder()
                    .id(2L)
                    .user(user)
                    .build();

            LocalDateTime now = LocalDateTime.now();

            final Post post1 = TestPost.builder()
                    .id(1L)
                    .community(community)
                    .member(member1)
                    .build();
            TestTimeReflection.setCreatedAt(post1, now);

            final Post post2 = TestPost.builder()
                    .id(2L)
                    .community(community)
                    .member(member2)
                    .build();
            TestTimeReflection.setCreatedAt(post2, now);

            List<Post> posts = List.of(post1, post2);
            given(memberRepository.findMemberIdsForQueryUserPost(anyLong()))
                    .willReturn(List.of(member1.getId(), member2.getId()));

            Pageable pageable = PageRequest.of(0, 2);
            Slice<Post> postPage = PageableUtil.getSlice(posts, pageable);

            given(postRepository.getUserPostPageByMemberIds(anyList(), any(Pageable.class)))
                    .willReturn(postPage);

            UserPostPageResponse userPostPageResponse = postQueryService.getUserPosts(user.getId(), 1L, pageable);

            List<UserPostDto> userPosts = userPostPageResponse.getPosts();
            assertThat(userPosts.size()).isEqualTo(2);
            assertThat(userPosts.get(0).getId()).isEqualTo(post1.getId());
            assertThat(userPosts.get(0).getCommunity().getId()).isEqualTo(community.getId());
            assertThat(userPosts.get(0).getCreatedAt()).isEqualTo(now.toString());
            assertThat(userPosts.get(1).getId()).isEqualTo(post2.getId());
            assertThat(userPosts.get(1).getCommunity().getId()).isEqualTo(community.getId());
            assertThat(userPosts.get(1).getCreatedAt()).isEqualTo(now.toString());

            PaginationDto pageInfo = userPostPageResponse.getPageInfo();
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.isHasNext()).isFalse();
        }

        @Test
        @DisplayName("세션 유저와 요청한 유저가 다른 경우 세션 유저가 비가입된 비공개 커뮤니티의 글을 제외하고 페이지네이션해서 조회한다.")
        void othersPostRequestWithMySessionUserSuccess() {
            final User user1 = TestUser.builder().id(1L).build();

            final Community community = TestCommunity.builder()
                    .id(1L)
                    .isPrivate(false)
                    .build();
            final Community pCommunity = TestCommunity.builder()
                    .id(2L)
                    .isPrivate(true)
                    .build();

            final Member member1 = TestMember.builder()
                    .id(1L)
                    .user(user1)
                    .community(pCommunity)
                    .build();
            final Member member2 = TestMember.builder()
                    .id(1L)
                    .user(user1)
                    .community(community)
                    .build();

            LocalDateTime now = LocalDateTime.now();

            final Post post1 = TestPost.builder()
                    .id(1L)
                    .community(pCommunity)
                    .member(member1)
                    .build();
            TestTimeReflection.setCreatedAt(post1, now);

            final Post post2 = TestPost.builder()
                    .id(2L)
                    .community(community)
                    .member(member2)
                    .build();
            TestTimeReflection.setCreatedAt(post2, now);

            given(userRepository.findByUserId(anyLong()))
                    .willReturn(user1);

            List<Post> posts = List.of(post2);
            given(memberRepository.findMemberIdsForQueryUserPost(anyLong(), anyLong()))
                    .willReturn(List.of(member2.getId()));

            Pageable pageable = PageRequest.of(0, 2);
            Slice<Post> postPage = PageableUtil.getSlice(posts, pageable);

            given(postRepository.getUserPostPageByMemberIds(anyList(), any(Pageable.class)))
                    .willReturn(postPage);

            UserPostPageResponse userPostPageResponse = postQueryService.getUserPosts(user1.getId(), 2L, pageable);

            List<UserPostDto> userPosts = userPostPageResponse.getPosts();
            assertThat(userPosts.size()).isEqualTo(1);
            assertThat(userPosts.get(0).getId()).isEqualTo(post2.getId());
            assertThat(userPosts.get(0).getCommunity().getId()).isEqualTo(community.getId());
            assertThat(userPosts.get(0).getCreatedAt()).isEqualTo(now.toString());

            PaginationDto pageInfo = userPostPageResponse.getPageInfo();
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.isHasNext()).isFalse();
        }
    }
}