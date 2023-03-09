package boogi.apiserver.domain.comment.application;

import boogi.apiserver.builder.*;
import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.dto.dto.UserCommentDto;
import boogi.apiserver.domain.comment.dto.request.CreateCommentRequest;
import boogi.apiserver.domain.comment.dto.response.CommentsAtPostResponse;
import boogi.apiserver.domain.comment.dto.response.UserCommentPageResponse;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.like.application.LikeCommandService;
import boogi.apiserver.domain.like.dao.LikeRepository;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.member.application.MemberValidationService;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.exception.NotAuthorizedMemberException;
import boogi.apiserver.domain.member.exception.NotJoinedMemberException;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.dto.PaginationDto;
import boogi.apiserver.global.util.PageableUtil;
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
import org.springframework.data.domain.Slice;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
class CommentCommandServiceTest {

    @InjectMocks
    CommentCommandService commentCommandService;

    @Mock
    CommentRepository commentRepository;

    @Mock
    PostRepository postRepository;

    @Mock
    MemberValidationService memberValidationService;

    @Mock
    CommentValidationService commentValidationService;

    @Mock
    LikeCommandService likeCommandService;

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
    class CreateCommentRequestTest {
        @Test
        @DisplayName("parentCommentId를 null로 주면 부모 댓글이 생성된다.")
        void createParentCommentSuccess() {
            final Community community = TestCommunity.builder().id(1L).build();

            final Member member = TestMember.builder().build();

            final Post post = TestPost.builder()
                    .id(2L)
                    .community(community)
                    .build();

            given(postRepository.findByPostId(anyLong()))
                    .willReturn(post);
            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member));

            CreateCommentRequest createCommentRequest = new CreateCommentRequest(post.getId(), null, "hello", List.of());

            Comment createdComment = commentCommandService.createComment(createCommentRequest, 3L);

            assertThat(createdComment.getContent()).isEqualTo(createCommentRequest.getContent());
            assertThat(createdComment.getParent()).isNull();
            assertThat(createdComment.getChild()).isFalse();
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

            given(postRepository.findByPostId(anyLong()))
                    .willReturn(post);

            final Member member = TestMember.builder()
                    .id(3L)
                    .community(community)
                    .build();

            final Comment parentComment = TestComment.builder()
                    .id(4L)
                    .member(member)
                    .post(post)
                    .content("부모댓글")
                    .build();

            given(commentRepository.findById(anyLong()))
                    .willReturn(Optional.of(parentComment));

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member));

            CreateCommentRequest createCommentRequest = new CreateCommentRequest(post.getId(), parentComment.getId(),
                    "자식댓글", List.of());

            Comment createdComment = commentCommandService.createComment(createCommentRequest, 5L);

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

            given(commentRepository.findCommentWithMemberByCommentId(eq(comment.getId())))
                    .willReturn(Optional.of(comment));

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

            final Community community = TestCommunity.builder().id(2L).build();

            final Member member1 = TestMember.builder()
                    .id(3L)
                    .user(user1)
                    .community(community)
                    .build();

            final Post post = TestPost.builder()
                    .id(4L)
                    .commentCount(1)
                    .build();

            final Comment comment = TestComment.builder()
                    .id(5L)
                    .member(member1)
                    .post(post)
                    .content("댓글")
                    .build();

            given(commentRepository.findCommentWithMemberByCommentId(eq(comment.getId())))
                    .willReturn(Optional.of(comment));

            given(memberValidationService.hasAuth(eq(user2.getId()), eq(community.getId()), eq(MemberType.SUB_MANAGER)))
                    .willReturn(true);

            commentCommandService.deleteComment(comment.getId(), user2.getId());

            assertThat(comment.getDeletedAt()).isNotNull();
            assertThat(comment.getPost().getCommentCount()).isZero();

            verify(likeCommandService, times(1)).removeAllCommentLikes(comment.getId());
        }

        @Test
        @DisplayName("권한이 없는 멤버가 삭제요청시 NotAuthorizedMemberException 발생")
        void notAuthorizedMemberDeleteCommentFail() {
            final User user = TestUser.builder().id(1L).build();
            final User user2 = TestUser.builder().id(2L).build();

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

            given(commentRepository.findCommentWithMemberByCommentId(eq(comment.getId())))
                    .willReturn(Optional.of(comment));

            assertThatThrownBy(() -> commentCommandService.deleteComment(comment.getId(), user2.getId()))
                    .isInstanceOf(NotAuthorizedMemberException.class);
        }
    }

    @Nested
    @DisplayName("글에 작성된 댓글들 조회시")
    class GetCommentsAtPostTest {
        @Test
        @DisplayName("부모 댓글 개수를 기준으로 페이지네이션해서 가져온다.")
        void getCommentsAtPostSuccess() {
            final User user = TestUser.builder().id(1L).build();

            final Community community = TestCommunity.builder().id(1L).build();

            final Member member = TestMember.builder()
                    .id(1L)
                    .user(user)
                    .community(community)
                    .build();

            final Post post = TestPost.builder()
                    .id(1L)
                    .community(community)
                    .build();

            final Comment pComment1 = TestComment.builder()
                    .id(1L)
                    .member(member)
                    .post(post)
                    .build();

            final Comment pComment2 = TestComment.builder()
                    .id(2L)
                    .member(member)
                    .post(post)
                    .build();

            final Comment cComment1 = TestComment.builder()
                    .id(3L)
                    .member(member)
                    .post(post)
                    .parent(pComment1)
                    .build();

            final Comment cComment2 = TestComment.builder()
                    .id(4L)
                    .member(member)
                    .post(post)
                    .parent(pComment2)
                    .build();

            final Like like1 = TestLike.builder()
                    .id(1L)
                    .member(member)
                    .comment(pComment1)
                    .build();

            final Like like2 = TestLike.builder()
                    .id(2L)
                    .member(member)
                    .comment(cComment1)
                    .build();

            given(postRepository.findByPostId(anyLong()))
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

            CommentsAtPostResponse commentsAtPostResponse = commentCommandService.getCommentsAtPost(post.getId(), 1L, pageable);

            assertThat(commentsAtPostResponse.getComments().size()).isEqualTo(2);

            CommentsAtPostResponse.ParentCommentInfo parentCommentInfo1 = commentsAtPostResponse.getComments().get(0);
            assertThat(parentCommentInfo1.getId()).isEqualTo(pComment1.getId());
            assertThat(parentCommentInfo1.getLikeCount()).isEqualTo(1L);
            assertThat(parentCommentInfo1.getLikeId()).isEqualTo(like1.getId());
            assertThat(parentCommentInfo1.getMember().getId()).isEqualTo(member.getId());
            assertThat(parentCommentInfo1.getChild().size()).isEqualTo(1);
            assertThat(parentCommentInfo1.getChild().get(0).getId()).isEqualTo(cComment1.getId());
            assertThat(parentCommentInfo1.getChild().get(0).getLikeCount()).isEqualTo(1L);
            assertThat(parentCommentInfo1.getChild().get(0).getLikeId()).isEqualTo(like2.getId());
            assertThat(parentCommentInfo1.getChild().get(0).getParentId()).isEqualTo(pComment1.getId());

            CommentsAtPostResponse.ParentCommentInfo parentCommentInfo2 = commentsAtPostResponse.getComments().get(1);
            assertThat(parentCommentInfo2.getId()).isEqualTo(pComment2.getId());
            assertThat(parentCommentInfo2.getLikeCount()).isEqualTo(0L);
            assertThat(parentCommentInfo2.getLikeId()).isNull();
            assertThat(parentCommentInfo2.getMember().getId()).isEqualTo(member.getId());
            assertThat(parentCommentInfo2.getChild().size()).isEqualTo(1);
            assertThat(parentCommentInfo2.getChild().get(0).getId()).isEqualTo(cComment2.getId());
            assertThat(parentCommentInfo2.getChild().get(0).getLikeCount()).isEqualTo(0L);
            assertThat(parentCommentInfo2.getChild().get(0).getLikeId()).isNull();
            assertThat(parentCommentInfo2.getChild().get(0).getParentId()).isEqualTo(pComment2.getId());

            PaginationDto pageInfo = commentsAtPostResponse.getPageInfo();
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.isHasNext()).isFalse();
        }

        @Test
        @DisplayName("비공개 커뮤니티에 가입되지 않은 상태로 해당 커뮤니티에 작성된 글에 달린 댓글들을 요청시 NotJoinedMemberException 발생한다.")
        void getCommentsAtPrivatePostWithoutAuthFail() {
            final Community community = TestCommunity.builder()
                    .id(1L)
                    .isPrivate(true)
                    .build();

            final Post post = TestPost.builder()
                    .id(1L)
                    .community(community)
                    .build();

            given(postRepository.findByPostId(anyLong()))
                    .willReturn(post);
            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.empty());

            PageRequest pageable = PageRequest.of(0, 1);

            assertThatThrownBy(() ->
                    commentCommandService.getCommentsAtPost(post.getId(), 1L, pageable))
                    .isInstanceOf(NotJoinedMemberException.class);
        }

        @Test
        @DisplayName("삭제된 부모 댓글에 자식 댓글이 존재하는 경우 부모 댓글 content에 '삭제된 댓글입니다'가 들어있다.")
        void testDeletedParentCommentWhenExistChildComment() {
            final User user = TestUser.builder().id(1L).build();

            final Community community = TestCommunity.builder().id(1L).build();

            final Member member = TestMember.builder()
                    .id(1L)
                    .user(user)
                    .community(community)
                    .build();

            final Post post = TestPost.builder()
                    .id(1L)
                    .community(community)
                    .commentCount(2)
                    .build();

            final Comment pComment1 = TestComment.builder()
                    .id(1L)
                    .member(member)
                    .post(post)
                    .build();

            pComment1.deleteComment();

            final Comment cComment1 = TestComment.builder()
                    .id(3L)
                    .member(member)
                    .post(post)
                    .parent(pComment1)
                    .build();

            given(postRepository.findByPostId(anyLong()))
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

            CommentsAtPostResponse commentsAtPostResponse = commentCommandService.getCommentsAtPost(post.getId(), 1L, pageable);

            assertThat(commentsAtPostResponse.getComments().size()).isEqualTo(1);

            CommentsAtPostResponse.ParentCommentInfo parentCommentInfo1 = commentsAtPostResponse.getComments().get(0);
            assertThat(parentCommentInfo1.getId()).isEqualTo(pComment1.getId());
            assertThat(parentCommentInfo1.getContent()).isEqualTo("삭제된 댓글입니다");
            assertThat(parentCommentInfo1.getChild().size()).isEqualTo(1);
            assertThat(parentCommentInfo1.getChild().get(0).getId()).isEqualTo(cComment1.getId());
            assertThat(parentCommentInfo1.getChild().get(0).getParentId()).isEqualTo(pComment1.getId());

            PaginationDto pageInfo = commentsAtPostResponse.getPageInfo();
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.isHasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("유저가 작성한 댓글들 조회시")
    class GetUserCommentsTest {
        @Test
        @DisplayName("댓글 작성한 유저 == 세션 유저, 해당 유저가 작성한 댓글들을 페이지네이션해서 가져온다.")
        void commenterAndSessionUserEqualSuccess() {
            final User user = TestUser.builder().id(1L).build();

            final Member member = TestMember.builder()
                    .id(2L)
                    .user(user)
                    .build();

            final Post post = TestPost.builder().id(3L).build();

            final Comment pComment = TestComment.builder()
                    .id(3L)
                    .member(member)
                    .post(post)
                    .build();

            final Comment cComment = TestComment.builder()
                    .id(4L)
                    .member(member)
                    .post(post)
                    .parent(pComment)
                    .build();

            List<Long> findMemberIds = List.of(member.getId());
            given(memberRepository.findMemberIdsForQueryUserPost(anyLong()))
                    .willReturn(findMemberIds);

            Pageable pageable = PageRequest.of(0, 2);
            List<Comment> comments = List.of(pComment, cComment);
            Slice<Comment> userCommentPage = PageableUtil.getSlice(comments, pageable);
            given(commentRepository.getUserCommentPageByMemberIds(anyList(), any(Pageable.class)))
                    .willReturn(userCommentPage);

            UserCommentPageResponse userCommentDto = commentCommandService.getUserComments(user.getId(), user.getId(), pageable);
            PaginationDto pageInfo = userCommentDto.getPageInfo();
            List<UserCommentDto> commentsDto = userCommentDto.getComments();

            assertThat(commentsDto.size()).isEqualTo(2);
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.isHasNext()).isFalse();
        }

        @Test
        @DisplayName("댓글 작성한 유저 != 세션 유저, 동시에 가입되지 않은 비공개 커뮤니티의 글에 작성된 댓글은 가져오지 않는다.")
        void commenterAndSessionUserNotEqualSuccess() {
            final User user1 = TestUser.builder().id(1L).build();
            final User user2 = TestUser.builder().id(2L).build();

            final Member member1 = TestMember.builder()
                    .id(3L)
                    .user(user1)
                    .build();
            final Member member2 = TestMember.builder()
                    .id(4L)
                    .user(user2)
                    .build();

            final Post post = TestPost.builder().id(7L).build();

            final Comment pComment = TestComment.builder()
                    .id(5L)
                    .member(member1)
                    .post(post)
                    .build();

            final Comment cComment = TestComment.builder()
                    .id(6L)
                    .member(member2)
                    .post(post)
                    .parent(pComment)
                    .build();

            given(userRepository.findUserById(anyLong()))
                    .willReturn(Optional.of(user2));

            List<Long> findMemberIds = List.of(member2.getId());
            given(memberRepository.findMemberIdsForQueryUserPost(anyLong(), anyLong()))
                    .willReturn(findMemberIds);

            Pageable pageable = PageRequest.of(0, 2);
            List<Comment> comments = List.of(cComment);
            Slice<Comment> userCommentPage = PageableUtil.getSlice(comments, pageable);
            given(commentRepository.getUserCommentPageByMemberIds(anyList(), any(Pageable.class)))
                    .willReturn(userCommentPage);


            UserCommentPageResponse userCommentDto = commentCommandService.getUserComments(user2.getId(), user1.getId(), pageable);
            PaginationDto pageInfo = userCommentDto.getPageInfo();
            List<UserCommentDto> commentsDto = userCommentDto.getComments();

            assertThat(commentsDto.size()).isEqualTo(1);
            assertThat(commentsDto.get(0).getPostId()).isEqualTo(post.getId());
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.isHasNext()).isFalse();
        }
    }
}