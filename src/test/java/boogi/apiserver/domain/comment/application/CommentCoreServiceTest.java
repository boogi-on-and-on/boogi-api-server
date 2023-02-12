package boogi.apiserver.domain.comment.application;

import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.dto.response.CommentsAtPost;
import boogi.apiserver.domain.comment.dto.request.CreateComment;
import boogi.apiserver.domain.comment.dto.response.UserCommentDto;
import boogi.apiserver.domain.comment.dto.response.UserCommentPage;
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
import boogi.apiserver.global.util.PageableUtil;
import boogi.apiserver.global.webclient.push.SendPushNotification;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.test.util.ReflectionTestUtils;

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
            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);

            final Member member = TestEmptyEntityGenerator.Member();

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 2L);
            ReflectionTestUtils.setField(post, "community", community);
            ReflectionTestUtils.setField(post, "commentCount", 0);

            given(postQueryService.getPost(anyLong()))
                    .willReturn(post);
            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member));

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
            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 2L);
            ReflectionTestUtils.setField(post, "community", community);
            ReflectionTestUtils.setField(post, "commentCount", 1);

            given(postQueryService.getPost(anyLong()))
                    .willReturn(post);

            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "id", 3L);
            ReflectionTestUtils.setField(member, "community", community);


            final Comment parentComment = TestEmptyEntityGenerator.Comment();
            ReflectionTestUtils.setField(parentComment, "id", 4L);
            ReflectionTestUtils.setField(parentComment, "member", member);
            ReflectionTestUtils.setField(parentComment, "post", post);
            ReflectionTestUtils.setField(parentComment, "content", "부모댓글");

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
            final User user = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user, "id", 1L);

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 2L);

            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "id", 3L);
            ReflectionTestUtils.setField(member, "user", user);
            ReflectionTestUtils.setField(member, "community", community);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 4L);
            ReflectionTestUtils.setField(post, "commentCount", 1);

            final Comment comment = TestEmptyEntityGenerator.Comment();
            ReflectionTestUtils.setField(comment, "id", 5L);
            ReflectionTestUtils.setField(comment, "member", member);
            ReflectionTestUtils.setField(comment, "post", post);
            ReflectionTestUtils.setField(comment, "content", "댓글");

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
            final User user1 = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user1, "id", 1L);

            final User user2 = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user2, "id", 2L);

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 2L);

            final Member member1 = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member1, "id", 3L);
            ReflectionTestUtils.setField(member1, "user", user1);
            ReflectionTestUtils.setField(member1, "community", community);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 4L);
            ReflectionTestUtils.setField(post, "commentCount", 1);

            final Comment comment = TestEmptyEntityGenerator.Comment();
            ReflectionTestUtils.setField(comment, "id", 5L);
            ReflectionTestUtils.setField(comment, "member", member1);
            ReflectionTestUtils.setField(comment, "post", post);
            ReflectionTestUtils.setField(comment, "content", "댓글");

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
            final User user = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user, "id", 1L);

            final User user2 = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user2, "id", 2L);

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 2L);

            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "id", 3L);
            ReflectionTestUtils.setField(member, "user", user);
            ReflectionTestUtils.setField(member, "community", community);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 4L);
            ReflectionTestUtils.setField(post, "commentCount", 1);

            final Comment comment = TestEmptyEntityGenerator.Comment();
            ReflectionTestUtils.setField(comment, "id", 5L);
            ReflectionTestUtils.setField(comment, "member", member);
            ReflectionTestUtils.setField(comment, "post", post);
            ReflectionTestUtils.setField(comment, "content", "댓글");

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
            final User user = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user, "id", 1L);


            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);

            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "id", 1L);
            ReflectionTestUtils.setField(member, "user", user);
            ReflectionTestUtils.setField(member, "community", community);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 1L);
            ReflectionTestUtils.setField(post, "community", community);

            final Comment pComment1 = TestEmptyEntityGenerator.Comment();
            ReflectionTestUtils.setField(pComment1, "id", 1L);
            ReflectionTestUtils.setField(pComment1, "member", member);
            ReflectionTestUtils.setField(pComment1, "post", post);

            final Comment pComment2 = TestEmptyEntityGenerator.Comment();
            ReflectionTestUtils.setField(pComment2, "id", 2L);
            ReflectionTestUtils.setField(pComment2, "member", member);
            ReflectionTestUtils.setField(pComment2, "post", post);

            final Comment cComment1 = TestEmptyEntityGenerator.Comment();
            ReflectionTestUtils.setField(cComment1, "id", 3L);
            ReflectionTestUtils.setField(cComment1, "member", member);
            ReflectionTestUtils.setField(cComment1, "post", post);
            ReflectionTestUtils.setField(cComment1, "parent", pComment1);

            final Comment cComment2 = TestEmptyEntityGenerator.Comment();
            ReflectionTestUtils.setField(cComment2, "id", 4L);
            ReflectionTestUtils.setField(cComment2, "member", member);
            ReflectionTestUtils.setField(cComment2, "post", post);
            ReflectionTestUtils.setField(cComment2, "parent", pComment2);

            final Like like1 = TestEmptyEntityGenerator.Like();
            ReflectionTestUtils.setField(like1, "id", 1L);
            ReflectionTestUtils.setField(like1, "member", member);
            ReflectionTestUtils.setField(like1, "comment", pComment1);

            final Like like2 = TestEmptyEntityGenerator.Like();
            ReflectionTestUtils.setField(like2, "id", 2L);
            ReflectionTestUtils.setField(like2, "member", member);
            ReflectionTestUtils.setField(like2, "comment", cComment1);

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
            assertThat(pageInfo.isHasNext()).isFalse();
        }

        @Test
        @DisplayName("글이 작성된 비공개 커뮤니티에 가입되지 않은 상태로 요청시 NotJoinedMemberException 발생한다.")
        void getCommentsAtPrivatePostWithoutAuthFail() {
            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 1L);
            ReflectionTestUtils.setField(post, "community", community);

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
            final User user = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user, "id", 1L);


            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);

            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "id", 1L);
            ReflectionTestUtils.setField(member, "user", user);
            ReflectionTestUtils.setField(member, "community", community);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 1L);
            ReflectionTestUtils.setField(post, "community", community);
            ReflectionTestUtils.setField(post, "commentCount", 2);

            final Comment pComment1 = TestEmptyEntityGenerator.Comment();
            ReflectionTestUtils.setField(pComment1, "id", 1L);
            ReflectionTestUtils.setField(pComment1, "member", member);
            ReflectionTestUtils.setField(pComment1, "post", post);

            pComment1.deleteComment();

            final Comment cComment1 = TestEmptyEntityGenerator.Comment();
            ReflectionTestUtils.setField(cComment1, "id", 3L);
            ReflectionTestUtils.setField(cComment1, "member", member);
            ReflectionTestUtils.setField(cComment1, "post", post);
            ReflectionTestUtils.setField(cComment1, "parent", pComment1);

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
            assertThat(pageInfo.isHasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("유저가 작성한 댓글들 조회시")
    class GetUserCommentsTest {
        @Test
        @DisplayName("댓글 작성한 유저 == 세션 유저, 해당 유저가 작성한 댓글들을 페이지네이션해서 가져온다.")
        void commenterAndSessionUserEqualSuccess() {
            final User user = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user, "id", 1L);


            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "id", 2L);
            ReflectionTestUtils.setField(member, "user", user);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 3L);

            final Comment pComment = TestEmptyEntityGenerator.Comment();
            ReflectionTestUtils.setField(pComment, "id", 3L);
            ReflectionTestUtils.setField(pComment, "member", member);
            ReflectionTestUtils.setField(pComment, "post", post);

            final Comment cComment = TestEmptyEntityGenerator.Comment();
            ReflectionTestUtils.setField(cComment, "id", 4L);
            ReflectionTestUtils.setField(cComment, "member", member);
            ReflectionTestUtils.setField(cComment, "post", post);
            ReflectionTestUtils.setField(cComment, "parent", pComment);


            List<Long> findMemberIds = List.of(member.getId());
            given(memberRepository.findMemberIdsForQueryUserPostBySessionUserId(anyLong()))
                    .willReturn(findMemberIds);

            Pageable pageable = PageRequest.of(0, 2);
            List<Comment> comments = List.of(pComment, cComment);
            Slice<Comment> userCommentPage = PageableUtil.getSlice(comments, pageable);
            given(commentRepository.getUserCommentPageByMemberIds(anyList(), any(Pageable.class)))
                    .willReturn(userCommentPage);

            UserCommentPage userCommentDto = commentCoreService.getUserComments(user.getId(), user.getId(), pageable);
            PaginationDto pageInfo = userCommentDto.getPageInfo();
            List<UserCommentDto> commentsDto = userCommentDto.getComments();

            assertThat(commentsDto.size()).isEqualTo(2);
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.isHasNext()).isFalse();
        }

        @Test
        @DisplayName("댓글 작성한 유저 != 세션 유저, 동시에 가입되지 않은 비공개 커뮤니티의 글에 작성된 댓글은 가져오지 않는다.")
        void commenterAndSessionUserNotEqualSuccess() {
            final User user1 = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user1, "id", 1L);

            final User user2 = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user2, "id", 2L);

            final Member member1 = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member1, "id", 3L);
            ReflectionTestUtils.setField(member1, "user", user1);

            final Member member2 = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member2, "id", 4L);
            ReflectionTestUtils.setField(member2, "user", user2);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 7L);

            final Comment pComment = TestEmptyEntityGenerator.Comment();
            ReflectionTestUtils.setField(pComment, "id", 5L);
            ReflectionTestUtils.setField(pComment, "member", member1);
            ReflectionTestUtils.setField(pComment, "post", post);

            final Comment cComment = TestEmptyEntityGenerator.Comment();
            ReflectionTestUtils.setField(cComment, "id", 6L);
            ReflectionTestUtils.setField(cComment, "member", member2);
            ReflectionTestUtils.setField(cComment, "post", post);
            ReflectionTestUtils.setField(cComment, "parent", pComment);

            given(userRepository.findUserById(anyLong()))
                    .willReturn(Optional.of(user2));

            List<Long> findMemberIds = List.of(member2.getId());
            given(memberRepository.findMemberIdsForQueryUserPostByUserIdAndSessionUserId(anyLong(), anyLong()))
                    .willReturn(findMemberIds);

            Pageable pageable = PageRequest.of(0, 2);
            List<Comment> comments = List.of(cComment);
            Slice<Comment> userCommentPage = PageableUtil.getSlice(comments, pageable);
            given(commentRepository.getUserCommentPageByMemberIds(anyList(), any(Pageable.class)))
                    .willReturn(userCommentPage);


            UserCommentPage userCommentDto = commentCoreService.getUserComments(user2.getId(), user1.getId(), pageable);
            PaginationDto pageInfo = userCommentDto.getPageInfo();
            List<UserCommentDto> commentsDto = userCommentDto.getComments();

            assertThat(commentsDto.size()).isEqualTo(1);
            assertThat(commentsDto.get(0).getPostId()).isEqualTo(post.getId());
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.isHasNext()).isFalse();
        }
    }
}