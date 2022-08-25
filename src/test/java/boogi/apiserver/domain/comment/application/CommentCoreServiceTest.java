package boogi.apiserver.domain.comment.application;

import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.dto.CommentsAtPost;
import boogi.apiserver.domain.comment.dto.CreateComment;
import boogi.apiserver.domain.community.community.application.CommunityValidationService;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.like.application.LikeCoreService;
import boogi.apiserver.domain.like.dao.LikeRepository;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.member.application.MemberValidationService;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.exception.NotAuthorizedMemberException;
import boogi.apiserver.domain.member.exception.NotJoinedMemberException;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.dto.PaginationDto;
import boogi.apiserver.global.webclient.push.SendPushNotification;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
class CommentCoreServiceTest {

    @InjectMocks
    CommentCoreService commentCoreService;

    @Mock
    CommentRepository commentRepository;

    @Mock
    PostQueryService postQueryService;

    @Mock
    MemberValidationService memberValidationService;

    @Mock
    CommentValidationService commentValidationService;

    @Mock
    CommunityValidationService communityValidationService;

    @Mock
    LikeCoreService likeCoreService;

    @Mock
    SendPushNotification sendPushNotification;

    @Mock
    MemberRepository memberRepository;

    @Mock
    LikeRepository likeRepository;

    @Mock
    UserRepository userRepository;


    @Nested
    @DisplayName("댓글 생성시")
    class CreateCommentTest {
        @Test
        @DisplayName("parentCommentId를 null로 주면 부모 댓글이 생성된다.")
        void createParentCommentSuccess() {
            Community community = Community.builder()
                    .id(1L)
                    .build();

            Post post = Post.builder()
                    .id(2L)
                    .community(community)
                    .commentCount(0)
                    .build();
            given(postQueryService.getPost(anyLong()))
                    .willReturn(post);
            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(Member.builder().build()));

            CreateComment createComment = new CreateComment(post.getId(), null, "hello", List.of());

            Comment createdComment = commentCoreService.createComment(createComment, 3L);

            assertThat(createdComment.getContent()).isEqualTo(createComment.getContent());
            assertThat(createdComment.getParent()).isNull();
            assertThat(createdComment.getChild()).isFalse();
            assertThat(post.getCommentCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("ParentCommentId에 부모 댓글의 Id값을 주면 자식 댓글이 생성된다.")
        void createChildCommentSuccess() {
            Community community = Community.builder()
                    .id(1L)
                    .build();

            Post post = Post.builder()
                    .id(2L)
                    .community(community)
                    .commentCount(1)
                    .build();
            given(postQueryService.getPost(anyLong()))
                    .willReturn(post);

            Member member = Member.builder()
                    .id(3L)
                    .community(community)
                    .build();

            Comment parentComment = Comment.builder()
                    .id(4L)
                    .parent(null)
                    .member(member)
                    .post(post)
                    .content("부모댓글")
                    .build();

            given(commentRepository.findById(anyLong()))
                    .willReturn(Optional.of(parentComment));

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member));

            CreateComment createComment = new CreateComment(post.getId(), parentComment.getId(), "자식댓글", List.of());

            Comment createdComment = commentCoreService.createComment(createComment, 5L);

            assertThat(createdComment.getContent()).isEqualTo("자식댓글");
            assertThat(createdComment.getChild()).isTrue();
            assertThat(createdComment.getParent().getId()).isEqualTo(parentComment.getId());
            assertThat(post.getCommentCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("댓글 삭제시")
    class DeleteCommentTest {

        @Test
        @DisplayName("작성자 본인이 요청할때 댓글과 모든 좋아요를 삭제한다.")
        void commentorDeleteCommentSuccess() {
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
                    .commentCount(1)
                    .build();

            Comment comment = Comment.builder()
                    .id(5L)
                    .content("댓글")
                    .member(member)
                    .post(post)
                    .build();
            given(commentRepository.findCommentWithMemberByCommentId(eq(comment.getId())))
                    .willReturn(Optional.of(comment));

            commentCoreService.deleteComment(comment.getId(), user.getId());

            assertThat(comment.getDeletedAt()).isNotNull();
            assertThat(comment.getPost().getCommentCount()).isZero();

            verify(likeCoreService, times(1)).removeAllCommentLikes(comment.getId());
        }

        @Test
        @DisplayName("해당 커뮤니티 (부)관리자가 요청할때 댓글과 모든 좋아요를 삭제한다.")
        void managerDeleteCommentSuccess() {
            User user1 = User.builder()
                    .id(1L)
                    .build();

            User user2 = User.builder()
                    .id(2L)
                    .build();

            Community community = Community.builder()
                    .id(2L)
                    .build();

            Member member1 = Member.builder()
                    .id(3L)
                    .user(user1)
                    .community(community)
                    .build();

            Post post = Post.builder()
                    .id(4L)
                    .commentCount(1)
                    .build();

            Comment comment = Comment.builder()
                    .id(5L)
                    .content("댓글")
                    .member(member1)
                    .post(post)
                    .build();
            given(commentRepository.findCommentWithMemberByCommentId(eq(comment.getId())))
                    .willReturn(Optional.of(comment));

            given(memberValidationService.hasAuth(eq(user2.getId()), eq(community.getId()), eq(MemberType.SUB_MANAGER)))
                    .willReturn(true);

            commentCoreService.deleteComment(comment.getId(), user2.getId());

            assertThat(comment.getDeletedAt()).isNotNull();
            assertThat(comment.getPost().getCommentCount()).isZero();

            verify(likeCoreService, times(1)).removeAllCommentLikes(comment.getId());
        }

        @Test
        @DisplayName("권한이 없는 멤버가 삭제요청시 NotAuthorizedMemberException 발생")
        void notAuthorizedMemberDeleteCommentFail() {
            User user = User.builder()
                    .id(1L)
                    .build();
            User user2 = user.builder()
                    .id(2L)
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
                    .commentCount(1)
                    .build();

            Comment comment = Comment.builder()
                    .id(5L)
                    .content("댓글")
                    .member(member)
                    .post(post)
                    .build();
            given(commentRepository.findCommentWithMemberByCommentId(eq(comment.getId())))
                    .willReturn(Optional.of(comment));

            assertThatThrownBy(() -> commentCoreService.deleteComment(comment.getId(), user2.getId()))
                    .isInstanceOf(NotAuthorizedMemberException.class);
        }
    }

    @Nested
    @DisplayName("글에 작성된 댓글들 조회시")
    class GetCommentsAtPostTest {
        @Test
        @DisplayName("부모 댓글 개수를 기준으로 페이지네이션해서 가져온다.")
        void getCommentsAtPostSuccess() {
            User user = User.builder()
                    .id(1L)
                    .build();

            Community community = Community.builder()
                    .id(1L)
                    .build();

            Member member = Member.builder()
                    .id(1L)
                    .user(user)
                    .community(community)
                    .build();

            Post post = Post.builder()
                    .id(1L)
                    .community(community)
                    .build();

            Comment pComment1 = Comment.builder()
                    .id(1L)
                    .post(post)
                    .member(member)
                    .parent(null)
                    .build();
            Comment pComment2 = Comment.builder()
                    .id(2L)
                    .post(post)
                    .member(member)
                    .parent(null)
                    .build();
            Comment cComment1 = Comment.builder()
                    .id(3L)
                    .post(post)
                    .member(member)
                    .parent(pComment1)
                    .build();
            Comment cComment2 = Comment.builder()
                    .id(4L)
                    .post(post)
                    .member(member)
                    .parent(pComment2)
                    .build();

            Like like1 = Like.builder()
                    .id(1L)
                    .member(member)
                    .comment(pComment1)
                    .build();
            Like like2 = Like.builder()
                    .id(2L)
                    .member(member)
                    .comment(cComment1)
                    .build();
            given(postQueryService.getPost(anyLong()))
                    .willReturn(post);
            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member));

            Pageable pageable = PageRequest.of(0, 2);
            List<Comment> parentComments = List.of(pComment1, pComment2);
            Page<Comment> parentCommentPage = PageableExecutionUtils
                    .getPage(parentComments, pageable, () -> parentComments.size());
            given(commentRepository.findParentCommentsWithMemberByPostId(any(Pageable.class), anyLong()))
                    .willReturn(parentCommentPage);

            List<Comment> childComments = List.of(cComment1, cComment2);
            given(commentRepository.findChildCommentsWithMemberByParentCommentIds(anyList()))
                    .willReturn(childComments);
            Map<Long, Long> commentLikeCountMap = Map.of(pComment1.getId(), 1L, cComment1.getId(), 1L);
            given(likeRepository.getCommentLikeCountsByCommentIds(anyList()))
                    .willReturn(commentLikeCountMap);

            List<Like> commentLikes = List.of(like1, like2);
            given(likeRepository.findCommentLikesByCommentIdsAndMemberId(anyList(), anyLong()))
                    .willReturn(commentLikes);

            CommentsAtPost commentsAtPost = commentCoreService.getCommentsAtPost(post.getId(), 1L, pageable);

            assertThat(commentsAtPost.getComments().size()).isEqualTo(2);

            CommentsAtPost.ParentCommentInfo parentCommentInfo1 = commentsAtPost.getComments().get(0);
            assertThat(parentCommentInfo1.getId()).isEqualTo(pComment1.getId());
            assertThat(parentCommentInfo1.getLikeCount()).isEqualTo(1L);
            assertThat(parentCommentInfo1.getLikeId()).isEqualTo(like1.getId());
            assertThat(parentCommentInfo1.getMember().getId()).isEqualTo(member.getId());
            assertThat(parentCommentInfo1.getChild().size()).isEqualTo(1);
            assertThat(parentCommentInfo1.getChild().get(0).getId()).isEqualTo(cComment1.getId());
            assertThat(parentCommentInfo1.getChild().get(0).getLikeCount()).isEqualTo(1L);
            assertThat(parentCommentInfo1.getChild().get(0).getLikeId()).isEqualTo(like2.getId());
            assertThat(parentCommentInfo1.getChild().get(0).getParentId()).isEqualTo(pComment1.getId());

            CommentsAtPost.ParentCommentInfo parentCommentInfo2 = commentsAtPost.getComments().get(1);
            assertThat(parentCommentInfo2.getId()).isEqualTo(pComment2.getId());
            assertThat(parentCommentInfo2.getLikeCount()).isEqualTo(0L);
            assertThat(parentCommentInfo2.getLikeId()).isNull();
            assertThat(parentCommentInfo2.getMember().getId()).isEqualTo(member.getId());
            assertThat(parentCommentInfo2.getChild().size()).isEqualTo(1);
            assertThat(parentCommentInfo2.getChild().get(0).getId()).isEqualTo(cComment2.getId());
            assertThat(parentCommentInfo2.getChild().get(0).getLikeCount()).isEqualTo(0L);
            assertThat(parentCommentInfo2.getChild().get(0).getLikeId()).isNull();
            assertThat(parentCommentInfo2.getChild().get(0).getParentId()).isEqualTo(pComment2.getId());

            PaginationDto pageInfo = commentsAtPost.getPageInfo();
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.getTotalCount()).isEqualTo(2);
            assertThat(pageInfo.isHasNext()).isFalse();
        }

        @Test
        @DisplayName("글이 작성된 비공개 커뮤니티에 가입되지 않은 상태로 요청시 NotJoinedMemberException 발생한다.")
        void getCommentsAtPrivatePostWithoutAuthFail() {
            Community community = Community.builder()
                    .id(1L)
                    .build();
            Post post = Post.builder()
                    .id(1L)
                    .community(community)
                    .build();
            given(postQueryService.getPost(anyLong()))
                    .willReturn(post);
            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.empty());

            PageRequest pageable = PageRequest.of(0, 1);

            assertThatThrownBy(() ->
                    commentCoreService.getCommentsAtPost(post.getId(), 1L, pageable))
                    .isInstanceOf(NotJoinedMemberException.class);
        }

        @Test
        @DisplayName("삭제된 부모 댓글에 자식 댓글이 존재하는 경우 부모 댓글 content에 '삭제된 댓글입니다'가 들어있다.")
        void testDeletedParentCommentWhenExistChildComment() {
            User user = User.builder()
                    .id(1L)
                    .build();

            Community community = Community.builder()
                    .id(1L)
                    .build();

            Member member = Member.builder()
                    .id(1L)
                    .user(user)
                    .community(community)
                    .build();

            Post post = Post.builder()
                    .id(1L)
                    .community(community)
                    .commentCount(2)
                    .build();

            Comment pComment1 = Comment.builder()
                    .id(1L)
                    .post(post)
                    .member(member)
                    .parent(null)
                    .build();
            pComment1.deleteComment();
            Comment cComment1 = Comment.builder()
                    .id(3L)
                    .post(post)
                    .member(member)
                    .parent(pComment1)
                    .build();

            given(postQueryService.getPost(anyLong()))
                    .willReturn(post);
            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member));

            Pageable pageable = PageRequest.of(0, 2);
            List<Comment> parentComments = List.of(pComment1);
            Page<Comment> parentCommentPage = PageableExecutionUtils
                    .getPage(parentComments, pageable, () -> parentComments.size());
            given(commentRepository.findParentCommentsWithMemberByPostId(any(Pageable.class), anyLong()))
                    .willReturn(parentCommentPage);

            List<Comment> childComments = List.of(cComment1);
            given(commentRepository.findChildCommentsWithMemberByParentCommentIds(anyList()))
                    .willReturn(childComments);
            Map<Long, Long> commentLikeCountMap = Map.of();
            given(likeRepository.getCommentLikeCountsByCommentIds(anyList()))
                    .willReturn(commentLikeCountMap);

            List<Like> commentLikes = List.of();
            given(likeRepository.findCommentLikesByCommentIdsAndMemberId(anyList(), anyLong()))
                    .willReturn(commentLikes);

            CommentsAtPost commentsAtPost = commentCoreService.getCommentsAtPost(post.getId(), 1L, pageable);

            assertThat(commentsAtPost.getComments().size()).isEqualTo(1);

            CommentsAtPost.ParentCommentInfo parentCommentInfo1 = commentsAtPost.getComments().get(0);
            assertThat(parentCommentInfo1.getId()).isEqualTo(pComment1.getId());
            assertThat(parentCommentInfo1.getContent()).isEqualTo("삭제된 댓글입니다");
            assertThat(parentCommentInfo1.getChild().size()).isEqualTo(1);
            assertThat(parentCommentInfo1.getChild().get(0).getId()).isEqualTo(cComment1.getId());
            assertThat(parentCommentInfo1.getChild().get(0).getParentId()).isEqualTo(pComment1.getId());

            PaginationDto pageInfo = commentsAtPost.getPageInfo();
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.getTotalCount()).isEqualTo(1);
            assertThat(pageInfo.isHasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("유저가 작성한 댓글들 조회시")
    class GetUserCommentsTest {
        @Test
        @DisplayName("댓글 작성한 유저 == 세션 유저, 해당 유저가 작성한 댓글들을 페이지네이션해서 가져온다.")
        void commenterAndSessionUserEqualSuccess() {
            User user = User.builder()
                    .id(1L)
                    .build();

            Member member = Member.builder()
                    .id(1L)
                    .user(user)
                    .build();

            Comment pComment = Comment.builder()
                    .id(1L)
                    .member(member)
                    .parent(null)
                    .build();
            Comment cComment = Comment.builder()
                    .id(2L)
                    .member(member)
                    .parent(pComment)
                    .build();

            List<Long> findMemberIds = List.of(member.getId());
            given(memberRepository.findMemberIdsForQueryUserPostBySessionUserId(anyLong()))
                    .willReturn(findMemberIds);

            Pageable pageable = PageRequest.of(0, 2);
            List<Comment> comments = List.of(pComment, cComment);
            Page<Comment> userCommentPage = PageableExecutionUtils.getPage(comments, pageable, () -> comments.size());
            given(commentRepository.getUserCommentPageByMemberIds(anyList(), any(Pageable.class)))
                    .willReturn(userCommentPage);

            Page<Comment> userComments = commentCoreService.getUserComments(user.getId(), user.getId(), pageable);

            assertThat(userComments.getContent().size()).isEqualTo(2);
            assertThat(userComments.getContent().get(0).getId()).isEqualTo(pComment.getId());
            assertThat(userComments.getContent().get(1).getId()).isEqualTo(cComment.getId());
            assertThat(userComments.getNumber()).isEqualTo(0);
            assertThat(userComments.getTotalElements()).isEqualTo(2L);
            assertThat(userComments.hasNext()).isFalse();
        }

        @Test
        @DisplayName("댓글 작성한 유저 != 세션 유저, 동시에 가입되지 않은 비공개 커뮤니티의 글에 작성된 댓글은 가져오지 않는다.")
        void commenterAndSessionUserNotEqualSuccess() {
            User user1 = User.builder()
                    .id(1L)
                    .build();
            User user2 = User.builder()
                    .id(2L)
                    .build();

            Member member1 = Member.builder()
                    .id(1L)
                    .user(user1)
                    .build();
            Member member2 = Member.builder()
                    .id(2L)
                    .user(user2)
                    .build();

            Comment pComment = Comment.builder()
                    .id(1L)
                    .member(member1)
                    .parent(null)
                    .build();
            Comment cComment = Comment.builder()
                    .id(2L)
                    .member(member2)
                    .parent(pComment)
                    .build();
            given(userRepository.findUserById(anyLong()))
                    .willReturn(Optional.of(user2));

            List<Long> findMemberIds = List.of(member2.getId());
            given(memberRepository.findMemberIdsForQueryUserPostByUserIdAndSessionUserId(anyLong(), anyLong()))
                    .willReturn(findMemberIds);

            Pageable pageable = PageRequest.of(0, 2);
            List<Comment> comments = List.of(cComment);
            Page<Comment> userCommentPage = PageableExecutionUtils.getPage(comments, pageable, () -> comments.size());
            given(commentRepository.getUserCommentPageByMemberIds(anyList(), any(Pageable.class)))
                    .willReturn(userCommentPage);

            Page<Comment> userComments = commentCoreService.getUserComments(user2.getId(), user1.getId(), pageable);

            assertThat(userComments.getContent().size()).isEqualTo(1);
            assertThat(userComments.getContent().get(0).getId()).isEqualTo(cComment.getId());
            assertThat(userComments.getNumber()).isEqualTo(0);
            assertThat(userComments.getTotalElements()).isEqualTo(1L);
            assertThat(userComments.hasNext()).isFalse();
        }
    }
}