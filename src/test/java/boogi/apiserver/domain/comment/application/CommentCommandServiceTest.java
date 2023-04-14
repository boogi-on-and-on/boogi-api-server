package boogi.apiserver.domain.comment.application;

import boogi.apiserver.builder.*;
import boogi.apiserver.domain.comment.repository.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.dto.request.CreateCommentRequest;
import boogi.apiserver.domain.comment.exception.CanNotDeleteCommentException;
import boogi.apiserver.domain.comment.exception.CommentMaxDepthOverException;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.like.application.LikeCommandService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.post.post.repository.PostRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
class CommentCommandServiceTest {

    @InjectMocks
    CommentCommandService commentCommandService;

    @Mock
    MemberQueryService memberQueryService;

    @Mock
    CommentRepository commentRepository;

    @Mock
    PostRepository postRepository;

    @Mock
    LikeCommandService likeCommandService;

    @Nested
    @DisplayName("댓글 생성시")
    class CreateCommentRequestTest {

        @Captor
        ArgumentCaptor<Comment> commentCaptor;

        @Test
        @DisplayName("parentCommentId를 null로 주면 부모 댓글이 생성된다.")
        void createParentCommentSuccess() {
            final Community community = TestCommunity.builder().id(1L).build();

            final Member member = TestMember.builder().build();

            final Post post = TestPost.builder()
                    .id(2L)
                    .community(community)
                    .build();

            given(postRepository.findPostById(anyLong()))
                    .willReturn(post);
            given(memberQueryService.getMember(anyLong(), anyLong()))
                    .willReturn(member);

            CreateCommentRequest request =
                    new CreateCommentRequest(2L, null, "hello", List.of());

            commentCommandService.createComment(request, 3L);

            verify(commentRepository, times(1)).save(commentCaptor.capture());

            Comment newComment = commentCaptor.getValue();

            assertThat(newComment.getContent()).isEqualTo("hello");
            assertThat(newComment.getParent()).isNull();
            assertThat(newComment.getChild()).isFalse();
            assertThat(post.getCommentCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("ParentCommentId에 부모 댓글의 Id값을 주면 자식 댓글이 생성된다.")
        void createChildCommentSuccess() {
            final Community community = TestCommunity.builder().id(1L).build();

            final Post post = TestPost.builder()
                    .id(2L)
                    .community(community)
                    .commentCount(1)
                    .build();
            given(postRepository.findPostById(anyLong()))
                    .willReturn(post);

            final Member member = TestMember.builder()
                    .id(3L)
                    .community(community)
                    .build();
            given(memberQueryService.getMember(anyLong(), anyLong()))
                    .willReturn(member);

            final Comment parentComment = TestComment.builder()
                    .id(4L)
                    .member(member)
                    .post(post)
                    .parent(null)
                    .content("부모댓글")
                    .build();
            given(commentRepository.findById(anyLong()))
                    .willReturn(Optional.of(parentComment));

            CreateCommentRequest request =
                    new CreateCommentRequest(2L, 4L, "자식댓글", List.of());

            commentCommandService.createComment(request, 5L);

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
            final Community community = TestCommunity.builder().id(1L).build();

            final Post post = TestPost.builder()
                    .id(2L)
                    .community(community)
                    .commentCount(2)
                    .build();

            final Member member = TestMember.builder()
                    .id(3L)
                    .community(community)
                    .build();

            final Comment firstDepthComment = TestComment.builder()
                    .id(4L)
                    .member(member)
                    .post(post)
                    .parent(null)
                    .content("댓글")
                    .build();

            final Comment secondDepthComment = TestComment.builder()
                    .id(5L)
                    .member(member)
                    .post(post)
                    .parent(firstDepthComment)
                    .content("대댓글")
                    .build();

            given(postRepository.findPostById(anyLong())).willReturn(post);
            given(memberQueryService.getMember(anyLong(), anyLong())).willReturn(member);

            given(commentRepository.findById(anyLong())).willReturn(Optional.of(secondDepthComment));

            CreateCommentRequest request =
                    new CreateCommentRequest(2L, 5L, "대대댓글", List.of());

            assertThatThrownBy(() -> commentCommandService.createComment(request, 1L))
                    .isInstanceOf(CommentMaxDepthOverException.class);
        }
    }

    @Nested
    @DisplayName("댓글 삭제시")
    class DeleteCommentTest {

        @Test
        @DisplayName("작성자 본인이 요청할때 댓글과 모든 좋아요를 삭제한다.")
        void commentorDeleteCommentSuccess() {
            final User user = TestUser.builder().id(1L).build();

            final Community community = TestCommunity.builder().id(2L).build();

            final Member member = TestMember.builder()
                    .id(3L)
                    .user(user)
                    .community(community)
                    .build();

            final Post post = TestPost.builder()
                    .id(4L)
                    .commentCount(1)
                    .build();

            final Comment comment = TestComment.builder()
                    .id(5L)
                    .member(member)
                    .post(post)
                    .content("댓글")
                    .build();
            given(commentRepository.findCommentById(anyLong()))
                    .willReturn(comment);

            commentCommandService.deleteComment(comment.getId(), user.getId());

            assertThat(comment.getDeletedAt()).isNotNull();
            assertThat(comment.getPost().getCommentCount()).isZero();

            verify(likeCommandService, times(1)).removeAllCommentLikes(comment.getId());
        }

        @Test
        @DisplayName("해당 커뮤니티 (부)관리자가 요청할때 댓글과 모든 좋아요를 삭제한다.")
        void managerDeleteCommentSuccess() {
            final User user1 = TestUser.builder().id(1L).build();
            final User user2 = TestUser.builder().id(2L).build();

            final Community community = TestCommunity.builder().id(3L).build();

            final Member member1 = TestMember.builder()
                    .id(4L)
                    .user(user1)
                    .community(community)
                    .build();
            final Member manager = TestMember.builder()
                    .id(5L)
                    .community(community)
                    .memberType(MemberType.MANAGER)
                    .build();

            final Post post = TestPost.builder()
                    .id(6L)
                    .community(community)
                    .commentCount(1)
                    .build();

            final Comment comment = TestComment.builder()
                    .id(7L)
                    .member(member1)
                    .post(post)
                    .content("댓글")
                    .build();
            given(commentRepository.findCommentById(anyLong()))
                    .willReturn(comment);

            given(memberQueryService.getMember(anyLong(), anyLong()))
                    .willReturn(manager);

            commentCommandService.deleteComment(comment.getId(), user2.getId());

            assertThat(comment.getDeletedAt()).isNotNull();
            assertThat(comment.getPost().getCommentCount()).isZero();

            verify(likeCommandService, times(1)).removeAllCommentLikes(7L);
        }

        @Test
        @DisplayName("권한이 없는 멤버가 삭제요청시 CanNotDeleteCommentException 발생")
        void canNotDeleteCommentFail() {
            final User user = TestUser.builder().id(1L).build();

            final Community community = TestCommunity.builder().id(2L).build();

            final Member member = TestMember.builder()
                    .id(3L)
                    .user(user)
                    .community(community)
                    .build();
            final Member normalMember = TestMember.builder()
                    .id(4L)
                    .memberType(MemberType.NORMAL)
                    .build();

            final Post post = TestPost.builder()
                    .id(4L)
                    .community(community)
                    .commentCount(1)
                    .build();

            final Comment comment = TestComment.builder()
                    .id(5L)
                    .member(member)
                    .post(post)
                    .content("댓글")
                    .build();
            given(commentRepository.findCommentById(anyLong()))
                    .willReturn(comment);

            given(memberQueryService.getMember(anyLong(), anyLong()))
                    .willReturn(normalMember);

            assertThatThrownBy(() -> commentCommandService.deleteComment(comment.getId(), 2L))
                    .isInstanceOf(CanNotDeleteCommentException.class);
        }
    }
}