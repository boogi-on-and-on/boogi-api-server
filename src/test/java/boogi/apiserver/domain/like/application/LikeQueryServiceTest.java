package boogi.apiserver.domain.like.application;

import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.repository.CommentRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.like.dto.response.LikeMembersAtCommentResponse;
import boogi.apiserver.domain.like.dto.response.LikeMembersAtPostResponse;
import boogi.apiserver.domain.like.repository.LikeRepository;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.repository.PostRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.repository.UserRepository;
import boogi.apiserver.global.dto.PaginationDto;
import boogi.apiserver.global.util.PageableUtil;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static boogi.apiserver.utils.fixture.CommentFixture.COMMENT1;
import static boogi.apiserver.utils.fixture.CommunityFixture.POCS;
import static boogi.apiserver.utils.fixture.MemberFixture.SUNDO_POCS;
import static boogi.apiserver.utils.fixture.PostFixture.POST1;
import static boogi.apiserver.utils.fixture.UserFixture.SUNDO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LikeQueryServiceTest {

    @InjectMocks
    LikeQueryService likeQueryService;

    @Mock
    MemberQueryService memberQueryService;

    @Mock
    PostRepository postRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    LikeRepository likeRepository;

    @Mock
    CommentRepository commentRepository;

    private User user;
    private Community community;
    private Member member;
    private Post post;
    private Comment comment;

    @BeforeEach
    void init() {
        user = SUNDO.toUser(1L);
        community = POCS.toCommunity(2L, List.of());
        member = SUNDO_POCS.toMember(3L, user, community);
        post = POST1.toPost(4L, member, community, List.of(), List.of());
        comment = COMMENT1.toComment(5L, post, member, null);
    }

    @Nested
    @DisplayName("글에 한 좋아요의 Id를 가져올시")
    class GetPostLikeIdTest {
        @DisplayName("좋아요가 있을시 해당 좋아요의 Id를 추출해서 가져온다.")
        @Test
        void existsPostLike() {
            Like like = Like.ofPost(post, member);
            ReflectionTestUtils.setField(like, "id", 6L);

            given(likeRepository.findPostLikeByPostAndMember(any(Post.class), any(Member.class)))
                    .willReturn(Optional.of(like));

            Long postLikeId = likeQueryService.getPostLikeId(post, member);

            assertThat(postLikeId).isEqualTo(6L);
        }

        @DisplayName("좋아요가 없을시 null을 가져온다.")
        @Test
        void notExistsPostLike() {
            given(likeRepository.findPostLikeByPostAndMember(any(Post.class), any(Member.class)))
                    .willReturn(Optional.empty());

            Long postLikeId = likeQueryService.getPostLikeId(post, member);

            assertThat(postLikeId).isNull();
        }
    }

    @Test
    @DisplayName("글에 좋아요한 유저들 조회시 유저들을 페이지네이션해서 가져온다.")
    void getLikeMembersAtPostSuccess() {
        Like like = Like.ofPost(post, member);
        ReflectionTestUtils.setField(like, "id", 6L);

        given(postRepository.findPostById(anyLong())).willReturn(post);

        Pageable pageable = PageRequest.of(0, 1);
        List<Like> likes = List.of(like);
        Slice<Like> likePage = PageableUtil.getSlice(likes, pageable);
        given(likeRepository.findPostLikePageWithMemberByPostId(anyLong(), any(Pageable.class)))
                .willReturn(likePage);

        List<User> users = List.of(user);
        given(userRepository.findUsersByIds(anyList())).willReturn(users);

        LikeMembersAtPostResponse response =
                likeQueryService.getLikeMembersAtPost(post.getId(), 2L, pageable);

        verify(memberQueryService, times(1))
                .getViewableMember(anyLong(), any(Community.class));

        PaginationDto pageInfo = response.getPageInfo();
        assertThat(response.getMembers()).hasSize(1);
        assertThat(response.getMembers().get(0).getId()).isEqualTo(1L);
        assertThat(pageInfo.getNextPage()).isEqualTo(1);
        assertThat(pageInfo.isHasNext()).isFalse();
    }

    @Test
    @DisplayName("댓글에 좋아요한 유저들 조회시 유저들을 페이지네이션해서 가져온다.")
    void getLikeMembersAtCommentSuccess() {
        Like like = Like.ofComment(comment, member);
        ReflectionTestUtils.setField(like, "id", 6L);

        given(commentRepository.findCommentById(anyLong())).willReturn(comment);

        Pageable pageable = PageRequest.of(0, 1);
        List<Like> likes = List.of(like);
        Slice<Like> likePage = PageableUtil.getSlice(likes, pageable);
        given(likeRepository.findCommentLikePageWithMemberByCommentId(anyLong(), any(Pageable.class)))
                .willReturn(likePage);

        List<User> users = List.of(user);
        given(userRepository.findUsersByIds(anyList())).willReturn(users);

        LikeMembersAtCommentResponse response =
                likeQueryService.getLikeMembersAtComment(comment.getId(), 2L, pageable);

        verify(memberQueryService, times(1))
                .getViewableMember(anyLong(), any(Community.class));

        PaginationDto pageInfo = response.getPageInfo();
        assertThat(response.getMembers()).hasSize(1);
        assertThat(response.getMembers().get(0).getId()).isEqualTo(1L);
        assertThat(pageInfo.getNextPage()).isEqualTo(1);
        assertThat(pageInfo.isHasNext()).isFalse();
    }
}