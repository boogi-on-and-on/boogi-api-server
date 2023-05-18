package boogi.apiserver.domain.comment.application;

import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.dto.dto.UserCommentDto;
import boogi.apiserver.domain.comment.dto.response.CommentsAtPostResponse;
import boogi.apiserver.domain.comment.dto.response.UserCommentPageResponse;
import boogi.apiserver.domain.comment.repository.CommentRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.like.repository.LikeRepository;
import boogi.apiserver.domain.member.application.MemberQuery;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.repository.MemberRepository;
import boogi.apiserver.domain.member.vo.NullMember;
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
import java.util.Map;

import static boogi.apiserver.utils.fixture.CommentFixture.*;
import static boogi.apiserver.utils.fixture.CommunityFixture.POCS;
import static boogi.apiserver.utils.fixture.MemberFixture.SUNDO_POCS;
import static boogi.apiserver.utils.fixture.MemberFixture.YONGJIN_POCS;
import static boogi.apiserver.utils.fixture.PostFixture.POST1;
import static boogi.apiserver.utils.fixture.UserFixture.SUNDO;
import static boogi.apiserver.utils.fixture.UserFixture.YONGJIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class CommentQueryTest {

    @InjectMocks
    CommentQuery commentQuery;

    @Mock
    MemberQuery memberQuery;

    @Mock
    CommentRepository commentRepository;

    @Mock
    PostRepository postRepository;

    @Mock
    MemberRepository memberRepository;

    @Mock
    LikeRepository likeRepository;

    @Mock
    UserRepository userRepository;

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
    @DisplayName("글에 작성된 댓글들 조회시")
    class GetCommentsAtPostTest {

        @Test
        @DisplayName("부모 댓글 개수를 기준으로 페이지네이션해서 가져온다.")
        void getCommentsAtPostSuccess() {
            Comment pComment1 = COMMENT1.toComment(5L, post, member, null);
            Comment pComment2 = COMMENT2.toComment(6L, post, member, null);
            Comment cComment1 = COMMENT3.toComment(7L, post, member, pComment1);
            Comment cComment2 = COMMENT3.toComment(8L, post, member, pComment2);

            Like like1 = Like.ofComment(pComment1, member);
            ReflectionTestUtils.setField(like1, "id", 9L);
            Like like2 = Like.ofComment(cComment1, member);
            ReflectionTestUtils.setField(like2, "id", 10L);

            given(postRepository.findPostById(anyLong())).willReturn(post);
            given(memberQuery.getViewableMember(anyLong(), any(Community.class)))
                    .willReturn(member);

            PageRequest pageable = PageRequest.of(0, 2);
            List<Comment> parentComments = List.of(pComment1, pComment2);
            Slice<Comment> parentCommentPage = PageableUtil.getSlice(parentComments, pageable);
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

            CommentsAtPostResponse response = commentQuery.getCommentsAtPost(post.getId(), 1L, pageable);

            assertThat(response.getComments().size()).isEqualTo(2);

            CommentsAtPostResponse.ParentCommentInfo parentCommentInfo1 = response.getComments().get(0);
            assertThat(parentCommentInfo1.getId()).isEqualTo(5L);
            assertThat(parentCommentInfo1.getLikeCount()).isEqualTo(1L);
            assertThat(parentCommentInfo1.getLikeId()).isEqualTo(9L);
            assertThat(parentCommentInfo1.getMember().getId()).isEqualTo(3L);
            assertThat(parentCommentInfo1.getChild().size()).isEqualTo(1);
            assertThat(parentCommentInfo1.getChild().get(0).getId()).isEqualTo(7L);
            assertThat(parentCommentInfo1.getChild().get(0).getLikeCount()).isEqualTo(1L);
            assertThat(parentCommentInfo1.getChild().get(0).getLikeId()).isEqualTo(10L);
            assertThat(parentCommentInfo1.getChild().get(0).getParentId()).isEqualTo(5L);

            CommentsAtPostResponse.ParentCommentInfo parentCommentInfo2 = response.getComments().get(1);
            assertThat(parentCommentInfo2.getId()).isEqualTo(6L);
            assertThat(parentCommentInfo2.getLikeCount()).isEqualTo(0L);
            assertThat(parentCommentInfo2.getLikeId()).isNull();
            assertThat(parentCommentInfo2.getMember().getId()).isEqualTo(3L);
            assertThat(parentCommentInfo2.getChild().size()).isEqualTo(1);
            assertThat(parentCommentInfo2.getChild().get(0).getId()).isEqualTo(8L);
            assertThat(parentCommentInfo2.getChild().get(0).getLikeCount()).isEqualTo(0L);
            assertThat(parentCommentInfo2.getChild().get(0).getLikeId()).isNull();
            assertThat(parentCommentInfo2.getChild().get(0).getParentId()).isEqualTo(6L);

            PaginationDto pageInfo = response.getPageInfo();
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.isHasNext()).isFalse();
        }

        @Test
        @DisplayName("공개 커뮤니티에 비가입된 유저가 요청하면 가져온 모든 댓글의 likeId가 null이다.")
        void notJoinedMemberGetCommentsAtPostSuccess() {
            Comment pComment1 = COMMENT1.toComment(5L, post, member, null);
            Comment pComment2 = COMMENT1.toComment(6L, post, member, null);
            Comment cComment1 = COMMENT3.toComment(7L, post, member, pComment1);
            Comment cComment2 = COMMENT3.toComment(8L, post, member, pComment2);

            given(postRepository.findPostById(anyLong())).willReturn(post);
            given(memberQuery.getViewableMember(anyLong(), any(Community.class)))
                    .willReturn(new NullMember());

            PageRequest pageable = PageRequest.of(0, 2);
            List<Comment> parentComments = List.of(pComment1, pComment2);
            Slice<Comment> parentCommentPage = PageableUtil.getSlice(parentComments, pageable);
            given(commentRepository.findParentCommentsWithMemberByPostId(any(Pageable.class), anyLong()))
                    .willReturn(parentCommentPage);

            List<Comment> childComments = List.of(cComment1, cComment2);
            given(commentRepository.findChildCommentsWithMemberByParentCommentIds(anyList()))
                    .willReturn(childComments);

            Map<Long, Long> commentLikeCountMap = Map.of(pComment1.getId(), 1L, cComment1.getId(), 1L);
            given(likeRepository.getCommentLikeCountsByCommentIds(anyList()))
                    .willReturn(commentLikeCountMap);

            CommentsAtPostResponse response = commentQuery.getCommentsAtPost(post.getId(), 1L, pageable);

            assertThat(response.getComments().size()).isEqualTo(2);
            assertThat(response.getComments()).extracting("likeId").containsOnlyNulls();

            PaginationDto pageInfo = response.getPageInfo();
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.isHasNext()).isFalse();
        }

        @Test
        @DisplayName("삭제된 부모 댓글에 자식 댓글이 존재하는 경우 부모 댓글 content에 '삭제된 댓글입니다'가 들어있다.")
        void testDeletedParentCommentWhenExistChildComment() {
            Comment deletedComment = COMMENT4.toComment(5L, post, member, null);
            Comment comment = COMMENT1.toComment(6L, post, member, deletedComment);

            given(postRepository.findPostById(anyLong())).willReturn(post);
            given(memberQuery.getViewableMember(anyLong(), any(Community.class))).willReturn(member);

            Pageable pageable = PageRequest.of(0, 2);
            List<Comment> parentComments = List.of(deletedComment);
            Slice<Comment> parentCommentPage = PageableUtil.getSlice(parentComments, pageable);
            given(commentRepository.findParentCommentsWithMemberByPostId(any(Pageable.class), anyLong()))
                    .willReturn(parentCommentPage);

            List<Comment> childComments = List.of(comment);
            given(commentRepository.findChildCommentsWithMemberByParentCommentIds(anyList()))
                    .willReturn(childComments);
            Map<Long, Long> commentLikeCountMap = Map.of();
            given(likeRepository.getCommentLikeCountsByCommentIds(anyList()))
                    .willReturn(commentLikeCountMap);

            List<Like> commentLikes = List.of();
            given(likeRepository.findCommentLikesByCommentIdsAndMemberId(anyList(), anyLong()))
                    .willReturn(commentLikes);

            CommentsAtPostResponse commentsAtPostResponse =
                    commentQuery.getCommentsAtPost(post.getId(), 1L, pageable);

            assertThat(commentsAtPostResponse.getComments()).hasSize(1);

            CommentsAtPostResponse.ParentCommentInfo parentCommentInfo1 = commentsAtPostResponse.getComments().get(0);
            assertThat(parentCommentInfo1.getId()).isEqualTo(5L);
            assertThat(parentCommentInfo1.getContent()).isEqualTo("삭제된 댓글입니다");
            assertThat(parentCommentInfo1.getChild().size()).isEqualTo(1);
            assertThat(parentCommentInfo1.getChild().get(0).getId()).isEqualTo(6L);
            assertThat(parentCommentInfo1.getChild().get(0).getParentId()).isEqualTo(5L);

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
            Comment pComment = COMMENT1.toComment(5L, post, member, null);
            Comment cComment = COMMENT1.toComment(6L, post, member, pComment);

            List<Long> findMemberIds = List.of(member.getId());
            given(memberRepository.findMemberIdsForQueryUserPost(anyLong()))
                    .willReturn(findMemberIds);

            Pageable pageable = PageRequest.of(0, 2);
            List<Comment> comments = List.of(pComment, cComment);
            Slice<Comment> userCommentPage = PageableUtil.getSlice(comments, pageable);
            given(commentRepository.getUserCommentPageByMemberIds(anyList(), any(Pageable.class)))
                    .willReturn(userCommentPage);

            UserCommentPageResponse userCommentDto =
                    commentQuery.getUserComments(1L, 1L, pageable);

            List<UserCommentDto> commentsDto = userCommentDto.getComments();
            PaginationDto pageInfo = userCommentDto.getPageInfo();

            assertThat(commentsDto).hasSize(2);
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.isHasNext()).isFalse();
        }

        @Test
        @DisplayName("댓글 작성한 유저 != 세션 유저, 동시에 가입되지 않은 비공개 커뮤니티의 글에 작성된 댓글은 가져오지 않는다.")
        void commenterAndSessionUserNotEqualSuccess() {
            User user2 = YONGJIN.toUser(2L);
            Member member2 = YONGJIN_POCS.toMember(4L, user2, community);

            Comment pComment = COMMENT1.toComment(5L, post, member, null);
            Comment cComment = COMMENT3.toComment(6L, post, member2, pComment);

            given(userRepository.findUserById(anyLong())).willReturn(user2);

            List<Long> findMemberIds = List.of(member2.getId());
            given(memberRepository.findMemberIdsForQueryUserPost(anyLong(), anyLong()))
                    .willReturn(findMemberIds);

            Pageable pageable = PageRequest.of(0, 2);
            List<Comment> comments = List.of(cComment);
            Slice<Comment> userCommentPage = PageableUtil.getSlice(comments, pageable);
            given(commentRepository.getUserCommentPageByMemberIds(anyList(), any(Pageable.class)))
                    .willReturn(userCommentPage);

            UserCommentPageResponse userCommentDto =
                    commentQuery.getUserComments(2L, 1L, pageable);

            PaginationDto pageInfo = userCommentDto.getPageInfo();
            List<UserCommentDto> commentsDto = userCommentDto.getComments();

            assertThat(commentsDto).hasSize(1);
            assertThat(commentsDto.get(0).getPostId()).isEqualTo(post.getId());
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.isHasNext()).isFalse();
        }
    }
}