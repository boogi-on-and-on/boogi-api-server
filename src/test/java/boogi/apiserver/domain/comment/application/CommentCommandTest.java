package boogi.apiserver.domain.comment.application;

import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.dto.request.CreateCommentRequest;
import boogi.apiserver.domain.comment.exception.CanNotDeleteCommentException;
import boogi.apiserver.domain.comment.exception.CommentMaxDepthOverException;
import boogi.apiserver.domain.comment.repository.CommentRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.like.application.LikeCommand;
import boogi.apiserver.domain.member.application.MemberQuery;
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

import java.util.List;
import java.util.Optional;

import static boogi.apiserver.utils.fixture.CommentFixture.COMMENT1;
import static boogi.apiserver.utils.fixture.CommentFixture.COMMENT3;
import static boogi.apiserver.utils.fixture.CommunityFixture.POCS;
import static boogi.apiserver.utils.fixture.MemberFixture.*;
import static boogi.apiserver.utils.fixture.PostFixture.POST1;
import static boogi.apiserver.utils.fixture.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
class CommentCommandTest {

    @InjectMocks
    CommentCommand commentCommand;

    @Mock
    MemberQuery memberQuery;

    @Mock
    CommentRepository commentRepository;

    @Mock
    PostRepository postRepository;

    @Mock
    LikeCommand likeCommand;

    private User user;
    private Community community;
    private Member member;
    private Post post;

    @BeforeEach
    void init() {
        user = SUNDO.toUser(1L);
        community = POCS.toCommunity(2L, List.of());
        member = SUNDO_POCS.toMember(3L, user, community);
        post = POST1.toPost(4L, member, community, List.of(), List.of());
    }

    @Nested
    @DisplayName("댓글 생성시")
    class CreateCommentRequestTest {

        @Captor
        ArgumentCaptor<Comment> commentCaptor;

        @Test
        @DisplayName("parentCommentId를 null로 주면 부모 댓글이 생성된다.")
        void createParentCommentSuccess() {
            given(postRepository.findPostById(anyLong())).willReturn(post);
            given(memberQuery.getMember(anyLong(), anyLong())).willReturn(member);

            CreateCommentRequest request =
                    new CreateCommentRequest(2L, null, "hello", List.of());

            commentCommand.createComment(request, 3L);

            verify(commentRepository, times(1)).save(commentCaptor.capture());

            Comment newComment = commentCaptor.getValue();
            assertThat(newComment.getContent()).isEqualTo("hello");
            assertThat(newComment.getParent()).isNull();
            assertThat(newComment.getChild()).isFalse();
            assertThat(post.getCommentCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("ParentCommentId에 부모 댓글의 Id값을 주면 자식 댓글이 생성된다.")
        void createChildCommentSuccess() {
            Comment parentComment = COMMENT1.toComment(4L, post, member, null);

            given(postRepository.findPostById(anyLong())).willReturn(post);
            given(memberQuery.getMember(anyLong(), anyLong())).willReturn(member);
            given(commentRepository.findById(anyLong())).willReturn(Optional.of(parentComment));

            CreateCommentRequest request =
                    new CreateCommentRequest(2L, 4L, "자식댓글", List.of());

            commentCommand.createComment(request, 5L);

            verify(commentRepository, times(1)).save(commentCaptor.capture());

            Comment newComment = commentCaptor.getValue();

            assertThat(newComment.getContent()).isEqualTo("자식댓글");
            assertThat(newComment.getParent().getId()).isEqualTo(4L);
            assertThat(newComment.getChild()).isTrue();
            assertThat(post.getCommentCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("대대댓글 이상인 경우 CommentMaxDepthOverException 발생")
        void commentDepthOverFail() {
            Comment firstDepthComment = COMMENT1.toComment(4L, post, member, null);
            Comment secondDepthComment = COMMENT3.toComment(5L, post, member, firstDepthComment);

            given(postRepository.findPostById(anyLong())).willReturn(post);
            given(memberQuery.getMember(anyLong(), anyLong())).willReturn(member);
            given(commentRepository.findById(anyLong())).willReturn(Optional.of(secondDepthComment));

            CreateCommentRequest request =
                    new CreateCommentRequest(2L, 5L, "대대댓글", List.of());

            assertThatThrownBy(() -> commentCommand.createComment(request, 1L))
                    .isInstanceOf(CommentMaxDepthOverException.class);
        }
    }

    @Nested
    @DisplayName("댓글 삭제시")
    class DeleteCommentTest {

        @Test
        @DisplayName("작성자 본인이 요청할때 댓글과 모든 좋아요를 삭제한다.")
        void commentorDeleteCommentSuccess() {
            Comment comment = COMMENT1.toComment(5L, post, member, null);

            given(commentRepository.findCommentById(anyLong())).willReturn(comment);

            commentCommand.deleteComment(comment.getId(), user.getId());

            verify(likeCommand, times(1)).removeAllCommentLikes(comment.getId());

            assertThat(comment.getDeletedAt()).isNotNull();
            assertThat(comment.getPost().getCommentCount()).isZero();
        }

        @Test
        @DisplayName("해당 커뮤니티 (부)관리자가 요청할때 댓글과 모든 좋아요를 삭제한다.")
        void managerDeleteCommentSuccess() {
            User user2 = YONGJIN.toUser(2L);

            Member member = YONGJIN_POCS.toMember(4L, user2, community);
            Member manager = SUNDO_POCS.toMember(5L, user, community);

            Post post = POST1.toPost(6L, member, community, List.of(), List.of());

            Comment comment = COMMENT1.toComment(7L, post, member, null);

            given(commentRepository.findCommentById(anyLong())).willReturn(comment);
            given(memberQuery.getMember(anyLong(), anyLong())).willReturn(manager);

            commentCommand.deleteComment(comment.getId(), user.getId());

            verify(likeCommand, times(1)).removeAllCommentLikes(7L);

            assertThat(comment.getDeletedAt()).isNotNull();
            assertThat(comment.getPost().getCommentCount()).isZero();
        }

        @Test
        @DisplayName("권한이 없는 멤버가 삭제요청시 CanNotDeleteCommentException 발생")
        void canNotDeleteCommentFail() {
            User user2 = DEOKHWAN.toUser(2L);

            Member normalMember = DEOKHWAN_POCS.toMember(4L, user2, community);

            Comment comment = COMMENT1.toComment(5L, post, member, null);

            given(commentRepository.findCommentById(anyLong())).willReturn(comment);
            given(memberQuery.getMember(anyLong(), anyLong())).willReturn(normalMember);

            assertThatThrownBy(() -> commentCommand.deleteComment(comment.getId(), 2L))
                    .isInstanceOf(CanNotDeleteCommentException.class);
        }
    }
}