package boogi.apiserver.domain.post.post.application;

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
import boogi.apiserver.domain.post.post.dto.response.PostDetail;
import boogi.apiserver.domain.post.post.dto.response.UserPostPage;
import boogi.apiserver.domain.post.post.dto.response.UserPostsDto;
import boogi.apiserver.domain.post.postmedia.dao.PostMediaRepository;
import boogi.apiserver.domain.post.postmedia.domain.MediaType;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.dto.PaginationDto;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.test.util.ReflectionTestUtils;

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
            final User user = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user, "id", 1L);

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 2L);

            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "id", 3L);
            ReflectionTestUtils.setField(member, "community", community);
            ReflectionTestUtils.setField(member, "user", user);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 4L);
            ReflectionTestUtils.setField(post, "content", "글");
            ReflectionTestUtils.setField(post, "member", member);
            ReflectionTestUtils.setField(post, "community", community);
            ReflectionTestUtils.setField(post, "likeCount", 0);
            ReflectionTestUtils.setField(post, "commentCount", 0);
            ReflectionTestUtils.setField(post, "hashtags", List.of());
            ReflectionTestUtils.setField(post, "createdAt", LocalDateTime.now());

            given(postRepository.getPostWithUserAndMemberAndCommunityByPostId(anyLong()))
                    .willReturn(Optional.of(post));

            given(memberQueryService.getViewableMember(anyLong(), any(Community.class)))
                    .willReturn(new NullMember());

            given(postMediaRepository.findByPostId(anyLong()))
                    .willReturn(List.of());

            given(likeQueryService.getPostLikeIdForView(anyLong(), any(Member.class)))
                    .willReturn(null);

            PostDetail postDetail = postQueryService.getPostDetail(post.getId(), 2L);

            assertThat(postDetail.getId()).isEqualTo(4L);
            assertThat(postDetail.getUser().getId()).isEqualTo(1L);
            assertThat(postDetail.getMember().getId()).isEqualTo(3L);
            assertThat(postDetail.getCommunity().getId()).isEqualTo(2L);
            assertThat(postDetail.getLikeId()).isNull();
            assertThat(postDetail.getCreatedAt()).isEqualTo(post.getCreatedAt());
            assertThat(postDetail.getContent()).isEqualTo("글");
            assertThat(postDetail.getLikeCount()).isEqualTo(0);
            assertThat(postDetail.getCommentCount()).isEqualTo(0);
            assertThat(postDetail.getMe()).isFalse();
        }

        @Test
        @DisplayName("글이 작성된 공개 커뮤니티에 가입상태로 조회시 성공한다.")
        void joinedUserRequestSuccess() {
            final User user = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user, "id", 1L);

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);

            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "id", 1L);
            ReflectionTestUtils.setField(member, "community", community);
            ReflectionTestUtils.setField(member, "user", user);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 1L);
            ReflectionTestUtils.setField(post, "content", "글");
            ReflectionTestUtils.setField(post, "member", member);
            ReflectionTestUtils.setField(post, "community", community);
            ReflectionTestUtils.setField(post, "likeCount", 1);
            ReflectionTestUtils.setField(post, "commentCount", 0);
            ReflectionTestUtils.setField(post, "hashtags", List.of());
            ReflectionTestUtils.setField(post, "createdAt", LocalDateTime.now());

            given(postRepository.getPostWithUserAndMemberAndCommunityByPostId(anyLong()))
                    .willReturn(Optional.of(post));

            given(memberQueryService.getViewableMember(anyLong(), any(Community.class)))
                    .willReturn(member);

            final PostMedia postMedia = TestEmptyEntityGenerator.PostMedia();
            ReflectionTestUtils.setField(postMedia, "id", 1L);
            ReflectionTestUtils.setField(postMedia, "mediaURL", "url");
            ReflectionTestUtils.setField(postMedia, "mediaType", MediaType.IMG);

            given(postMediaRepository.findByPostId(anyLong()))
                    .willReturn(List.of(postMedia));

            final Like like = TestEmptyEntityGenerator.Like();
            ReflectionTestUtils.setField(like, "id", 1L);

            given(likeQueryService.getPostLikeIdForView(anyLong(), any(Member.class)))
                    .willReturn(like.getId());

            PostDetail postDetail = postQueryService.getPostDetail(post.getId(), 1L);

            assertThat(postDetail.getId()).isEqualTo(post.getId());
            assertThat(postDetail.getUser().getId()).isEqualTo(user.getId());
            assertThat(postDetail.getMember().getId()).isEqualTo(member.getId());
            assertThat(postDetail.getCommunity().getId()).isEqualTo(community.getId());
            assertThat(postDetail.getPostMedias().size()).isEqualTo(1);
            assertThat(postDetail.getPostMedias().get(0).getType()).isEqualTo(postMedia.getMediaType());
            assertThat(postDetail.getPostMedias().get(0).getUrl()).isEqualTo(postMedia.getMediaURL());
            assertThat(postDetail.getLikeId()).isEqualTo(like.getId());
            assertThat(postDetail.getCreatedAt()).isEqualTo(post.getCreatedAt());
            assertThat(postDetail.getContent()).isEqualTo(post.getContent());
            assertThat(postDetail.getLikeCount()).isEqualTo(1);
            assertThat(postDetail.getCommentCount()).isEqualTo(0);
            assertThat(postDetail.getMe()).isTrue();
        }

        @Test
        @DisplayName("글이 작성된 비공개 커뮤니티에 비가입상태로 조회시 NotViewableMemberException 발생한다.")
        void notJoinedPrivateCommunityFail() {
            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);
            ReflectionTestUtils.setField(community, "isPrivate", true);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 1L);
            ReflectionTestUtils.setField(post, "community", community);

            given(postRepository.getPostWithUserAndMemberAndCommunityByPostId(anyLong()))
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
            final User user = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user, "id", 1L);

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);

            final Member member1 = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member1, "id", 1L);
            ReflectionTestUtils.setField(member1, "user", user);

            final Member member2 = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member2, "id", 2L);
            ReflectionTestUtils.setField(member2, "user", user);


            LocalDateTime now = LocalDateTime.now();

            final Post post1 = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post1, "id", 1L);
            ReflectionTestUtils.setField(post1, "community", community);
            ReflectionTestUtils.setField(post1, "member", member1);
            ReflectionTestUtils.setField(post1, "createdAt", now);

            final Post post2 = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post2, "id", 2L);
            ReflectionTestUtils.setField(post2, "community", community);
            ReflectionTestUtils.setField(post2, "member", member2);
            ReflectionTestUtils.setField(post2, "createdAt", now);

            List<Post> posts = List.of(post1, post2);
            given(memberRepository.findMemberIdsForQueryUserPost(anyLong()))
                    .willReturn(List.of(member1.getId(), member2.getId()));

            Pageable pageable = PageRequest.of(0, 2);
            Page<Post> postPage = PageableExecutionUtils.getPage(posts, pageable, () -> posts.size());

            given(postRepository.getUserPostPageByMemberIds(anyList(), any(Pageable.class)))
                    .willReturn(postPage);

            UserPostPage userPostPage = postQueryService.getUserPosts(user.getId(), 1L, pageable);

            List<UserPostsDto> userPosts = userPostPage.getPosts();
            assertThat(userPosts.size()).isEqualTo(2);
            assertThat(userPosts.get(0).getId()).isEqualTo(post1.getId());
            assertThat(userPosts.get(0).getCommunity().getId()).isEqualTo(community.getId());
            assertThat(userPosts.get(0).getCreatedAt()).isEqualTo(now.toString());
            assertThat(userPosts.get(1).getId()).isEqualTo(post2.getId());
            assertThat(userPosts.get(1).getCommunity().getId()).isEqualTo(community.getId());
            assertThat(userPosts.get(1).getCreatedAt()).isEqualTo(now.toString());

            PaginationDto pageInfo = userPostPage.getPageInfo();
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.isHasNext()).isFalse();
        }

        @Test
        @DisplayName("세션 유저와 요청한 유저가 다른 경우 세션 유저가 비가입된 비공개 커뮤니티의 글을 제외하고 페이지네이션해서 조회한다.")
        void othersPostRequestWithMySessionUserSuccess() {
            final User user1 = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user1, "id", 1L);

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);
            ReflectionTestUtils.setField(community, "isPrivate", false);

            final Community pCommunity = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(pCommunity, "id", 2L);
            ReflectionTestUtils.setField(pCommunity, "isPrivate", true);

            final Member member1 = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member1, "id", 1L);
            ReflectionTestUtils.setField(member1, "user", user1);
            ReflectionTestUtils.setField(member1, "community", pCommunity);

            final Member member2 = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member2, "id", 1L);
            ReflectionTestUtils.setField(member2, "user", user1);
            ReflectionTestUtils.setField(member2, "community", community);

            LocalDateTime now = LocalDateTime.now();

            final Post post1 = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post1, "id", 1L);
            ReflectionTestUtils.setField(post1, "community", pCommunity);
            ReflectionTestUtils.setField(post1, "member", member1);
            ReflectionTestUtils.setField(post1, "createdAt", now);

            final Post post2 = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post2, "id", 2L);
            ReflectionTestUtils.setField(post2, "community", community);
            ReflectionTestUtils.setField(post2, "member", member2);
            ReflectionTestUtils.setField(post2, "createdAt", now);

            given(userRepository.findByUserId(anyLong()))
                    .willReturn(user1);

            List<Post> posts = List.of(post2);
            given(memberRepository.findMemberIdsForQueryUserPost(anyLong(), anyLong()))
                    .willReturn(List.of(member2.getId()));

            Pageable pageable = PageRequest.of(0, 2);
            Page<Post> postPage = PageableExecutionUtils.getPage(posts, pageable, () -> posts.size());

            given(postRepository.getUserPostPageByMemberIds(anyList(), any(Pageable.class)))
                    .willReturn(postPage);

            UserPostPage userPostPage = postQueryService.getUserPosts(user1.getId(), 2L, pageable);

            List<UserPostsDto> userPosts = userPostPage.getPosts();
            assertThat(userPosts.size()).isEqualTo(1);
            assertThat(userPosts.get(0).getId()).isEqualTo(post2.getId());
            assertThat(userPosts.get(0).getCommunity().getId()).isEqualTo(community.getId());
            assertThat(userPosts.get(0).getCreatedAt()).isEqualTo(now.toString());

            PaginationDto pageInfo = userPostPage.getPageInfo();
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.isHasNext()).isFalse();
        }
    }
}