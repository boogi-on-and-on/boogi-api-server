package boogi.apiserver.domain.post.post.application;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.like.application.LikeQueryService;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.response.PostDetail;
import boogi.apiserver.domain.post.post.dto.response.UserPostPage;
import boogi.apiserver.domain.post.post.dto.response.UserPostsDto;
import boogi.apiserver.domain.post.postmedia.dao.PostMediaRepository;
import boogi.apiserver.domain.post.postmedia.domain.MediaType;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.user.application.UserQueryService;
import boogi.apiserver.domain.user.domain.User;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
    UserQueryService userQueryService;


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
            User user = User.builder()
                    .id(1L)
                    .build();

            Community community = Community.builder()
                    .id(2L)
                    .build();

            Member member = Member.builder()
                    .id(3L)
                    .user(user)
                    .community(community)
                    .build();

            Post post = Post.builder()
                    .id(4L)
                    .content("글")
                    .member(member)
                    .community(community)
                    .likeCount(0)
                    .commentCount(0)
                    .hashtags(List.of())
                    .build();
            post.setCreatedAt(LocalDateTime.now());
            given(postRepository.getPostWithUserAndMemberAndCommunityByPostId(anyLong()))
                    .willReturn(Optional.of(post));

            given(memberQueryService.getViewableMember(anyLong(), any(Community.class)))
                    .willReturn(member);

            given(likeQueryService.getPostLikeIdForView(anyLong(), any(Member.class)))
                    .willReturn(null);

            given(postMediaRepository.findByPostId(anyLong()))
                    .willReturn(List.of());

            PostDetail postDetail = postQueryService.getPostDetail(post.getId(), 1L);

            assertThat(postDetail.getId()).isEqualTo(4L);
            assertThat(postDetail.getUser().getId()).isEqualTo(1L);
            assertThat(postDetail.getMember().getId()).isEqualTo(3L);
            assertThat(postDetail.getCommunity().getId()).isEqualTo(2L);
            assertThat(postDetail.getLikeId()).isNull();
            assertThat(postDetail.getCreatedAt()).isEqualTo(post.getCreatedAt());
            assertThat(postDetail.getContent()).isEqualTo(post.getContent());
            assertThat(postDetail.getLikeCount()).isEqualTo(0);
            assertThat(postDetail.getCommentCount()).isEqualTo(0);
            assertThat(postDetail.getMe()).isTrue();
        }

        @Test
        @DisplayName("글이 작성된 공개 커뮤니티에 가입상태로 조회시 성공한다.")
        void joinedUserRequestSuccess() {
            User user = User.builder()
                    .id(1L)
                    .build();

            Community community = Community.builder()
                    .id(2L)
                    .build();

            Member member = Member.builder()
                    .id(3L)
                    .user(user)
                    .community(community)
                    .build();

            Post post = Post.builder()
                    .id(4L)
                    .content("글")
                    .member(member)
                    .community(community)
                    .likeCount(1)
                    .commentCount(0)
                    .hashtags(List.of())
                    .build();
            post.setCreatedAt(LocalDateTime.now());
            given(postRepository.getPostWithUserAndMemberAndCommunityByPostId(anyLong()))
                    .willReturn(Optional.of(post));

            given(memberQueryService.getViewableMember(anyLong(), any(Community.class)))
                    .willReturn(member);

            PostMedia postMedia = PostMedia.builder()
                    .id(5L)
                    .mediaType(MediaType.IMG)
                    .mediaURL("url")
                    .build();
            given(postMediaRepository.findByPostId(anyLong()))
                    .willReturn(List.of(postMedia));

            Like like = Like.builder()
                    .id(6L)
                    .build();
            given(likeQueryService.getPostLikeIdForView(anyLong(), any(Member.class)))
                    .willReturn(like.getId());

            PostDetail postDetail = postQueryService.getPostDetail(post.getId(), 1L);

            assertThat(postDetail.getId()).isEqualTo(4L);
            assertThat(postDetail.getUser().getId()).isEqualTo(1L);
            assertThat(postDetail.getMember().getId()).isEqualTo(3L);
            assertThat(postDetail.getCommunity().getId()).isEqualTo(2L);
            assertThat(postDetail.getPostMedias().size()).isEqualTo(1);
            assertThat(postDetail.getPostMedias().get(0).getType()).isEqualTo(MediaType.IMG);
            assertThat(postDetail.getPostMedias().get(0).getUrl()).isEqualTo("url");
            assertThat(postDetail.getLikeId()).isEqualTo(6L);
            assertThat(postDetail.getCreatedAt()).isEqualTo(post.getCreatedAt());
            assertThat(postDetail.getContent()).isEqualTo("글");
            assertThat(postDetail.getLikeCount()).isEqualTo(1);
            assertThat(postDetail.getCommentCount()).isEqualTo(0);
            assertThat(postDetail.getMe()).isTrue();
        }
    }

    @Nested
    @DisplayName("유저가 작성한 게시글들 조회시")
    class GetUserPostsTest {

        @Test
        @DisplayName("본인 세션으로 본인의 작성글들을 요청한 경우 본인의 작성글을 페이지네이션해서 조회한다.")
        void myPostRequestWithMySessionUserSuccess() {
            User user = User.builder()
                    .id(1L)
                    .build();

            Community community = Community.builder()
                    .id(2L)
                    .build();

            Member member1 = Member.builder()
                    .id(3L)
                    .user(user)
                    .build();
            Member member2 = Member.builder()
                    .id(4L)
                    .user(user)
                    .build();

            LocalDateTime now = LocalDateTime.now();
            Post post1 = Post.builder()
                    .id(5L)
                    .community(community)
                    .member(member1)
                    .hashtags(List.of())
                    .build();
            post1.setCreatedAt(now);
            Post post2 = Post.builder()
                    .id(6L)
                    .community(community)
                    .member(member2)
                    .hashtags(List.of())
                    .build();
            post2.setCreatedAt(now);
            List<Post> posts = List.of(post1, post2);
            given(memberRepository.findMemberIdsForQueryUserPost(anyLong()))
                    .willReturn(List.of(member1.getId(), member2.getId()));

            Pageable pageable = PageRequest.of(0, 2);
            Slice<Post> postPage = PageableUtil.getSlice(posts, pageable);

            given(postRepository.getUserPostPageByMemberIds(anyList(), any(Pageable.class)))
                    .willReturn(postPage);

            UserPostPage userPostPage = postQueryService.getUserPosts(user.getId(), 1L, pageable);

            List<UserPostsDto> userPosts = userPostPage.getPosts();
            assertThat(userPosts.size()).isEqualTo(2);
            assertThat(userPosts.get(0).getId()).isEqualTo(5L);
            assertThat(userPosts.get(0).getCommunity().getId()).isEqualTo(2L);
            assertThat(userPosts.get(0).getCreatedAt()).isEqualTo(now.toString());
            assertThat(userPosts.get(1).getId()).isEqualTo(6L);
            assertThat(userPosts.get(1).getCommunity().getId()).isEqualTo(2L);
            assertThat(userPosts.get(1).getCreatedAt()).isEqualTo(now.toString());

            PaginationDto pageInfo = userPostPage.getPageInfo();
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.isHasNext()).isFalse();
        }

        @Test
        @DisplayName("세션 유저와 요청한 유저가 다른 경우 세션 유저가 비가입된 비공개 커뮤니티의 글을 제외하고 페이지네이션해서 조회한다.")
        void othersPostRequestWithMySessionUserSuccess() {
            User user1 = User.builder()
                    .id(1L)
                    .build();

            Community community = Community.builder()
                    .id(2L)
                    .isPrivate(false)
                    .build();
            Community pCommunity = Community.builder()
                    .id(3L)
                    .isPrivate(true)
                    .build();

            Member member1 = Member.builder()
                    .id(4L)
                    .user(user1)
                    .community(pCommunity)
                    .build();
            Member member2 = Member.builder()
                    .id(5L)
                    .user(user1)
                    .community(community)
                    .build();

            LocalDateTime now = LocalDateTime.now();
            Post post1 = Post.builder()
                    .id(6L)
                    .community(pCommunity)
                    .member(member1)
                    .hashtags(List.of())
                    .build();
            post1.setCreatedAt(now);
            Post post2 = Post.builder()
                    .id(7L)
                    .community(community)
                    .member(member2)
                    .hashtags(List.of())
                    .build();
            post2.setCreatedAt(now);
            given(userQueryService.getUser(anyLong()))
                    .willReturn(user1);

            List<Post> posts = List.of(post2);
            given(memberRepository.findMemberIdsForQueryUserPost(anyLong(), anyLong()))
                    .willReturn(List.of(member2.getId()));

            Pageable pageable = PageRequest.of(0, 2);
            Slice<Post> postPage = PageableUtil.getSlice(posts, pageable);

            given(postRepository.getUserPostPageByMemberIds(anyList(), any(Pageable.class)))
                    .willReturn(postPage);

            UserPostPage userPostPage = postQueryService.getUserPosts(user1.getId(), 2L, pageable);

            List<UserPostsDto> userPosts = userPostPage.getPosts();
            assertThat(userPosts.size()).isEqualTo(1);
            assertThat(userPosts.get(0).getId()).isEqualTo(7L);
            assertThat(userPosts.get(0).getCommunity().getId()).isEqualTo(2L);
            assertThat(userPosts.get(0).getCreatedAt()).isEqualTo(now.toString());

            PaginationDto pageInfo = userPostPage.getPageInfo();
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.isHasNext()).isFalse();
        }
    }
}