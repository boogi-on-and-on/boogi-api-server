package boogi.apiserver.domain.like.application;

import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.repository.CommentRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.like.exception.AlreadyDoCommentLikeException;
import boogi.apiserver.domain.like.exception.AlreadyDoPostLikeException;
import boogi.apiserver.domain.like.exception.UnmatchedLikeUserException;
import boogi.apiserver.domain.like.repository.LikeRepository;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.repository.PostRepository;
import boogi.apiserver.domain.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static boogi.apiserver.utils.fixture.CommentFixture.COMMENT1;
import static boogi.apiserver.utils.fixture.CommunityFixture.POCS;
import static boogi.apiserver.utils.fixture.MemberFixture.SUNDO_POCS;
import static boogi.apiserver.utils.fixture.PostFixture.POST1;
import static boogi.apiserver.utils.fixture.UserFixture.SUNDO;
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
    @DisplayName("글에 좋아요할 시")
    class DoLikeAtPostTest {

        @Test
        @DisplayName("해당 글의 좋아요 수가 1 증가하고, 좋아요가 생성된다.")
        void doLikeAtPostSuccess() {
            given(postRepository.findPostById(anyLong())).willReturn(post);
            given(memberQueryService.getMember(anyLong(), anyLong())).willReturn(member);
            given(likeRepository.existsLikeByPostIdAndMemberId(anyLong(), anyLong()))
                    .willReturn(false);

            likeCommandService.doPostLike(post.getId(), 3L);

            verify(likeRepository, times(1)).save(likeCaptor.capture());

            Like newLike = likeCaptor.getValue();
            assertThat(newLike.getPost().getId()).isEqualTo(4L);
            assertThat(newLike.getMember().getId()).isEqualTo(3L);
            assertThat(newLike.getPost().getLikeCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("해당 멤버가 해당 글에 이미 좋아요를 누른 경우 AlreadyDoPostLikeException 발생한다.")
        void alreadyDoLikeFail() {
            given(postRepository.findPostById(anyLong())).willReturn(post);
            given(memberQueryService.getMember(anyLong(), anyLong())).willReturn(member);
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
            given(commentRepository.findCommentById(anyLong())).willReturn(comment);
            given(memberQueryService.getMember(anyLong(), anyLong())).willReturn(member);
            given(likeRepository.existsLikeByCommentIdAndMemberId(anyLong(), anyLong()))
                    .willReturn(false);

            likeCommandService.doCommentLike(comment.getId(), 1L);

            verify(likeRepository, times(1)).save(likeCaptor.capture());

            Like newLike = likeCaptor.getValue();
            assertThat(newLike.getComment().getId()).isEqualTo(5L);
            assertThat(newLike.getMember().getId()).isEqualTo(3L);
        }

        @Test
        @DisplayName("해당 멤버가 해당 댓글에 이미 좋아요를 누른 경우 AlreadyDoCommentLikeException 발생한다.")
        void alreadyDoLikeFail() {
            given(commentRepository.findCommentById(anyLong())).willReturn(comment);
            given(memberQueryService.getMember(anyLong(), anyLong())).willReturn(member);
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
            Like like = Like.ofComment(comment, member);
            ReflectionTestUtils.setField(like, "id", 6L);

            given(likeRepository.findLikeById(anyLong())).willReturn(like);

            likeCommandService.doUnlike(like.getId(), 1L);

            verify(likeRepository, times(1)).delete(any(Like.class));
        }

        @Test
        @DisplayName("해당 글의 좋아요 수가 1 감소하고, 글에 한 좋아요가 삭제된다.")
        void doUnlikeAtPostSuccess() {
            Like like = Like.ofPost(post, member);
            ReflectionTestUtils.setField(like, "id", 6L);

            given(likeRepository.findLikeById(anyLong())).willReturn(like);

            likeCommandService.doUnlike(like.getId(), 1L);

            verify(likeRepository, times(1)).delete(any(Like.class));
            assertThat(post.getLikeCount()).isOne();
        }

        @Test
        @DisplayName("요청한 세션 유저와 좋아요 한 유저가 서로 다를 경우 UnmatchedLikeUserException 발생한다.")
        void requestedUserAndLikedUserNotSameFail() {
            Like like = Like.ofComment(comment, member);
            ReflectionTestUtils.setField(like, "id", 6L);

            given(likeRepository.findLikeById(anyLong())).willReturn(like);

            assertThatThrownBy(() -> likeCommandService.doUnlike(like.getId(), 2L))
                    .isInstanceOf(UnmatchedLikeUserException.class);
        }
    }
}