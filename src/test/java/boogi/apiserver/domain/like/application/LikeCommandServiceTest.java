package boogi.apiserver.domain.like.application;

import boogi.apiserver.builder.*;
import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.like.dao.LikeRepository;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.like.exception.AlreadyDoCommentLikeException;
import boogi.apiserver.domain.like.exception.AlreadyDoPostLikeException;
import boogi.apiserver.domain.like.exception.UnmatchedLikeUserException;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class LikeCommandServiceTest {

    @InjectMocks
    LikeCommandService likeCommandService;

    @Mock
    MemberQueryService memberQueryService;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Captor
    ArgumentCaptor<Like> likeCaptor;

    @Nested
    @DisplayName("글에 좋아요할 시")
    class DoLikeAtPostTest {

        @Test
        @DisplayName("해당 글의 좋아요 수가 1 증가하고, 좋아요가 생성된다.")
        void doLikeAtPostSuccess() {
            final Community community = TestCommunity.builder().id(1L).build();

            final Post post = TestPost.builder()
                    .id(1L)
                    .community(community)
                    .likeCount(0)
                    .build();

            final Member member = TestMember.builder().id(2L).build();

            given(postRepository.findByPostId(anyLong()))
                    .willReturn(post);
            given(memberQueryService.getMember(anyLong(), anyLong()))
                    .willReturn(member);
            given(likeRepository.existsLikeByPostIdAndMemberId(anyLong(), anyLong()))
                    .willReturn(false);

            likeCommandService.doPostLike(post.getId(), 3L);

            verify(likeRepository, times(1)).save(likeCaptor.capture());
            Like newLike = likeCaptor.getValue();

            assertThat(newLike.getPost().getId()).isEqualTo(1L);
            assertThat(newLike.getMember().getId()).isEqualTo(2L);
            assertThat(newLike.getPost().getLikeCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("해당 멤버가 해당 글에 이미 좋아요를 누른 경우 AlreadyDoPostLikeException 발생한다.")
        void alreadyDoLikeFail() {
            final Community community = TestCommunity.builder().id(1L).build();

            final Post post = TestPost.builder()
                    .id(1L)
                    .community(community)
                    .build();

            final Member member = TestMember.builder().id(1L).build();

            given(postRepository.findByPostId(anyLong()))
                    .willReturn(post);
            given(memberQueryService.getMember(anyLong(), anyLong()))
                    .willReturn(member);
            given(likeRepository.existsLikeByPostIdAndMemberId(anyLong(), anyLong()))
                    .willReturn(true);

            assertThatThrownBy(() -> likeCommandService.doPostLike(post.getId(), 1L))
                    .isInstanceOf(AlreadyDoPostLikeException.class);
        }
    }

    @Nested
    @DisplayName("댓글에 좋아요할 시")
    class DoLikeAtCommentTest {
        @Test
        @DisplayName("해당 댓글에 좋아요가 생성된다.")
        void doLikeAtCommentSuccess() {
            final Community community = TestCommunity.builder().id(1L).build();

            final Post post = TestPost.builder().id(2L).community(community).build();

            final Member member = TestMember.builder().id(3L).community(community).build();

            final Comment comment = TestComment.builder().id(4L).post(post).member(member).build();

            given(commentRepository.findCommentById(anyLong()))
                    .willReturn(comment);
            given(memberQueryService.getMember(anyLong(), anyLong()))
                    .willReturn(member);
            given(likeRepository.existsLikeByCommentIdAndMemberId(anyLong(), anyLong()))
                    .willReturn(false);

            likeCommandService.doCommentLike(comment.getId(), 1L);

            verify(likeRepository, times(1)).save(likeCaptor.capture());
            Like newLike = likeCaptor.getValue();

            assertThat(newLike.getComment().getId()).isEqualTo(4L);
            assertThat(newLike.getMember().getId()).isEqualTo(3L);
        }

        @Test
        @DisplayName("해당 멤버가 해당 댓글에 이미 좋아요를 누른 경우 AlreadyDoCommentLikeException 발생한다.")
        void alreadyDoLikeFail() {
            final Community community = TestCommunity.builder().id(1L).build();

            final Post post = TestPost.builder().id(2L).community(community).build();

            final Member member = TestMember.builder().id(3L).community(community).build();

            final Comment comment = TestComment.builder().id(4L).post(post).member(member).build();

            given(commentRepository.findCommentById(anyLong()))
                    .willReturn(comment);
            given(memberQueryService.getMember(anyLong(), anyLong()))
                    .willReturn(member);
            given(likeRepository.existsLikeByCommentIdAndMemberId(anyLong(), anyLong()))
                    .willReturn(true);

            assertThatThrownBy(() -> likeCommandService.doCommentLike(comment.getId(), 1L))
                    .isInstanceOf(AlreadyDoCommentLikeException.class);
        }
    }

    @Nested
    @DisplayName("좋아요 취소할 시")
    class DoUnlikeTest {
        @Test
        @DisplayName("댓글에 한 좋아요가 삭제된다.")
        void doUnlikeAtCommentSuccess() {
            final User user = TestUser.builder().id(1L).build();

            final Member member = TestMember.builder()
                    .id(1L)
                    .user(user)
                    .build();

            final Comment comment = TestComment.builder().id(1L).build();

            final Like like = TestLike.builder()
                    .id(1L)
                    .comment(comment)
                    .member(member)
                    .build();
            given(likeRepository.findByLikeId(anyLong()))
                    .willReturn(like);

            likeCommandService.doUnlike(like.getId(), 1L);

            verify(likeRepository, times(1)).delete(any(Like.class));
        }

        @Test
        @DisplayName("해당 글의 좋아요 수가 1 감소하고, 글에 한 좋아요가 삭제된다.")
        void doUnlikeAtPostSuccess() {
            final User user = TestUser.builder().id(1L).build();

            final Member member = TestMember.builder()
                    .id(1L)
                    .user(user)
                    .build();

            final Post post = TestPost.builder()
                    .id(1L)
                    .member(member)
                    .likeCount(1)
                    .build();

            final Like like = TestLike.builder()
                    .id(1L)
                    .post(post)
                    .member(member)
                    .build();
            given(likeRepository.findByLikeId(anyLong()))
                    .willReturn(like);

            likeCommandService.doUnlike(like.getId(), 1L);

            assertThat(post.getLikeCount()).isZero();
            verify(likeRepository, times(1)).delete(any(Like.class));
        }

        @Test
        @DisplayName("요청한 세션 유저와 좋아요 한 유저가 서로 다를 경우 UnmatchedLikeUserException 발생한다.")
        void requestedUserAndLikedUserNotSameFail() {
            final User user = TestUser.builder().id(1L).build();

            final Member member = TestMember.builder()
                    .id(1L)
                    .user(user)
                    .build();

            final Comment comment = TestComment.builder().id(1L).build();

            final Like like = TestLike.builder()
                    .id(1L)
                    .comment(comment)
                    .member(member)
                    .build();
            given(likeRepository.findByLikeId(anyLong()))
                    .willReturn(like);

            assertThatThrownBy(() -> likeCommandService.doUnlike(like.getId(), 2L))
                    .isInstanceOf(UnmatchedLikeUserException.class);
        }
    }
}