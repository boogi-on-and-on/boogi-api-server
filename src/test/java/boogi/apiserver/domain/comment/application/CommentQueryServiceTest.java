package boogi.apiserver.domain.comment.application;

import boogi.apiserver.builder.*;
import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.dto.dto.UserCommentDto;
import boogi.apiserver.domain.comment.dto.response.CommentsAtPostResponse;
import boogi.apiserver.domain.comment.dto.response.UserCommentPageResponse;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.like.dao.LikeRepository;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.vo.NullMember;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.dto.PaginationDto;
import boogi.apiserver.global.util.PageableUtil;
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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class CommentQueryServiceTest {

    @InjectMocks
    CommentQueryService commentQueryService;

    @Mock
    MemberQueryService memberQueryService;

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


    @Nested
    @DisplayName("글에 작성된 댓글들 조회시")
    class GetCommentsAtPostTest {

        @Test
        @DisplayName("부모 댓글 개수를 기준으로 페이지네이션해서 가져온다.")
        void getCommentsAtPostSuccess() {
            final User user = TestUser.builder().id(1L).build();

            final Community community = TestCommunity.builder().id(2L).build();

            final Member member = TestMember.builder()
                    .id(3L)
                    .user(user)
                    .community(community)
                    .build();

            final Post post = TestPost.builder()
                    .id(4L)
                    .community(community)
                    .build();

            final Comment pComment1 = TestComment.builder()
                    .id(5L)
                    .member(member)
                    .post(post)
                    .build();
            final Comment pComment2 = TestComment.builder()
                    .id(6L)
                    .member(member)
                    .post(post)
                    .build();
            final Comment cComment1 = TestComment.builder()
                    .id(7L)
                    .member(member)
                    .post(post)
                    .parent(pComment1)
                    .build();
            final Comment cComment2 = TestComment.builder()
                    .id(8L)
                    .member(member)
                    .post(post)
                    .parent(pComment2)
                    .build();

            final Like like1 = TestLike.builder()
                    .id(9L)
                    .member(member)
                    .comment(pComment1)
                    .build();
            final Like like2 = TestLike.builder()
                    .id(10L)
                    .member(member)
                    .comment(cComment1)
                    .build();

            given(postRepository.findPostById(anyLong()))
                    .willReturn(post);
            given(memberQueryService.getViewableMember(anyLong(), any(Community.class)))
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

            CommentsAtPostResponse response = commentQueryService.getCommentsAtPost(post.getId(), 1L, pageable);

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
            final User user = TestUser.builder().id(1L).build();

            final Community community = TestCommunity.builder().id(2L).build();

            final Member member = TestMember.builder()
                    .id(3L)
                    .user(user)
                    .community(community)
                    .build();

            final Post post = TestPost.builder()
                    .id(4L)
                    .community(community)
                    .build();

            final Comment pComment1 = TestComment.builder()
                    .id(5L)
                    .member(member)
                    .post(post)
                    .build();
            final Comment pComment2 = TestComment.builder()
                    .id(6L)
                    .member(member)
                    .post(post)
                    .build();
            final Comment cComment1 = TestComment.builder()
                    .id(7L)
                    .member(member)
                    .post(post)
                    .parent(pComment1)
                    .build();
            final Comment cComment2 = TestComment.builder()
                    .id(8L)
                    .member(member)
                    .post(post)
                    .parent(pComment2)
                    .build();

            given(postRepository.findPostById(anyLong()))
                    .willReturn(post);
            given(memberQueryService.getViewableMember(anyLong(), any(Community.class)))
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

            CommentsAtPostResponse response = commentQueryService.getCommentsAtPost(post.getId(), 1L, pageable);

            assertThat(response.getComments().size()).isEqualTo(2);
            assertThat(response.getComments()).extracting("likeId").containsOnlyNulls();

            PaginationDto pageInfo = response.getPageInfo();
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.isHasNext()).isFalse();
        }

        @Test
        @DisplayName("삭제된 부모 댓글에 자식 댓글이 존재하는 경우 부모 댓글 content에 '삭제된 댓글입니다'가 들어있다.")
        void testDeletedParentCommentWhenExistChildComment() {
            final User user = TestUser.builder().id(1L).build();

            final Community community = TestCommunity.builder().id(2L).build();

            final Member member = TestMember.builder()
                    .id(3L)
                    .user(user)
                    .community(community)
                    .build();

            final Post post = TestPost.builder()
                    .id(4L)
                    .community(community)
                    .commentCount(2)
                    .build();

            final Comment pComment1 = TestComment.builder()
                    .id(5L)
                    .member(member)
                    .post(post)
                    .build();
            pComment1.deleteComment();

            final Comment cComment1 = TestComment.builder()
                    .id(6L)
                    .member(member)
                    .post(post)
                    .parent(pComment1)
                    .build();

            given(postRepository.findPostById(anyLong()))
                    .willReturn(post);
            given(memberQueryService.getViewableMember(anyLong(), any(Community.class)))
                    .willReturn(member);

            Pageable pageable = PageRequest.of(0, 2);
            List<Comment> parentComments = List.of(pComment1);
            Slice<Comment> parentCommentPage = PageableUtil.getSlice(parentComments, pageable);
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

            CommentsAtPostResponse commentsAtPostResponse =
                    commentQueryService.getCommentsAtPost(post.getId(), 1L, pageable);

            assertThat(commentsAtPostResponse.getComments().size()).isEqualTo(1);

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

            UserCommentPageResponse userCommentDto =
                    commentQueryService.getUserComments(1L, 1L, pageable);

            List<UserCommentDto> commentsDto = userCommentDto.getComments();
            PaginationDto pageInfo = userCommentDto.getPageInfo();

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
                    .willReturn(user2);

            List<Long> findMemberIds = List.of(member2.getId());
            given(memberRepository.findMemberIdsForQueryUserPost(anyLong(), anyLong()))
                    .willReturn(findMemberIds);

            Pageable pageable = PageRequest.of(0, 2);
            List<Comment> comments = List.of(cComment);
            Slice<Comment> userCommentPage = PageableUtil.getSlice(comments, pageable);
            given(commentRepository.getUserCommentPageByMemberIds(anyList(), any(Pageable.class)))
                    .willReturn(userCommentPage);

            UserCommentPageResponse userCommentDto =
                    commentQueryService.getUserComments(2L, 1L, pageable);

            PaginationDto pageInfo = userCommentDto.getPageInfo();
            List<UserCommentDto> commentsDto = userCommentDto.getComments();

            assertThat(commentsDto.size()).isEqualTo(1);
            assertThat(commentsDto.get(0).getPostId()).isEqualTo(post.getId());
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.isHasNext()).isFalse();
        }
    }
}