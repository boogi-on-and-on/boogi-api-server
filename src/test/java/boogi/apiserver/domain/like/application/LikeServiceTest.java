package boogi.apiserver.domain.like.application;

import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.like.dao.LikeRepository;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.like.dto.response.LikeMembersAtCommentResponse;
import boogi.apiserver.domain.like.dto.response.LikeMembersAtPost;
import boogi.apiserver.domain.like.exception.AlreadyDoLikeException;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.exception.NotAuthorizedMemberException;
import boogi.apiserver.domain.member.exception.NotJoinedMemberException;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.dto.PaginationDto;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @InjectMocks
    LikeService likeService;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PostQueryService postQueryService;

    @Mock
    private LikeValidationService likeValidationService;

    @Mock
    private UserRepository userRepository;

    @Nested
    @DisplayName("글에 좋아요할 시")
    class DoLikeAtPostTest {
        @Test
        @DisplayName("해당 글의 좋아요 수가 1 증가하고, 좋아요가 생성된다.")
        void doLikeAtPostSuccess() {
            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 1L);
            ReflectionTestUtils.setField(post, "community", community);
            ReflectionTestUtils.setField(post, "likeCount", 0);

            given(postRepository.findById(anyLong()))
                    .willReturn(Optional.of(post));

            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "id", 1L);

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member));

            given(likeValidationService.checkOnlyAlreadyDoPostLike(anyLong(), anyLong()))
                    .willReturn(false);

            Like newLike = likeService.doLikeAtPost(post.getId(), 1L);

            assertThat(newLike.getPost().getId()).isEqualTo(post.getId());
            assertThat(newLike.getMember().getId()).isEqualTo(member.getId());
            assertThat(newLike.getPost().getLikeCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("해당 멤버가 해당 글에 이미 좋아요를 누른 경우 AlreadyDoLikeException 발생한다.")
        void alreadyDoLikeFail() {
            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 1L);
            ReflectionTestUtils.setField(post, "community", community);

            given(postRepository.findById(anyLong()))
                    .willReturn(Optional.of(post));

            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "id", 1L);

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member));

            given(likeValidationService.checkOnlyAlreadyDoPostLike(anyLong(), anyLong()))
                    .willReturn(true);

            assertThatThrownBy(() -> likeService.doLikeAtPost(post.getId(), 1L))
                    .isInstanceOf(AlreadyDoLikeException.class);
        }
    }

    @Nested
    @DisplayName("댓글에 좋아요할 시")
    class DoLikeAtCommentTest {
        @Test
        @DisplayName("해당 댓글에 좋아요가 생성된다.")
        void doLikeAtCommentSuccess() {
            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);

            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "id", 1L);
            ReflectionTestUtils.setField(member, "community", community);

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member));

            final Comment comment = TestEmptyEntityGenerator.Comment();
            ReflectionTestUtils.setField(comment, "id", 1L);
            ReflectionTestUtils.setField(comment, "member", member);

            given(commentRepository.findCommentWithMemberByCommentId(anyLong()))
                    .willReturn(Optional.of(comment));

            given(likeValidationService.checkOnlyAlreadyDoCommentLike(anyLong(), anyLong()))
                    .willReturn(false);

            Like newLike = likeService.doLikeAtComment(comment.getId(), 1L);

            assertThat(newLike.getComment().getId()).isEqualTo(comment.getId());
            assertThat(newLike.getMember().getId()).isEqualTo(member.getId());
        }

        @Test
        @DisplayName("해당 멤버가 해당 댓글에 이미 좋아요를 누른 경우 AlreadyDoLikeException 발생한다.")
        void alreadyDoLikeFail() {
            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);

            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "id", 1L);
            ReflectionTestUtils.setField(member, "community", community);

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member));

            final Comment comment = TestEmptyEntityGenerator.Comment();
            ReflectionTestUtils.setField(comment, "id", 1L);
            ReflectionTestUtils.setField(comment, "member", member);

            given(commentRepository.findCommentWithMemberByCommentId(anyLong()))
                    .willReturn(Optional.of(comment));

            given(likeValidationService.checkOnlyAlreadyDoCommentLike(anyLong(), anyLong()))
                    .willReturn(true);

            assertThatThrownBy(() -> likeService.doLikeAtComment(comment.getId(), 1L))
                    .isInstanceOf(AlreadyDoLikeException.class);
        }
    }

    @Nested
    @DisplayName("좋아요 취소할 시")
    class DoUnlikeTest {
        @Test
        @DisplayName("댓글 좋아요 취소는 좋아요가 삭제된다.")
        void doUnlikeAtCommentSuccess() {
            final User user = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user, "id", 1L);

            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "id", 1L);
            ReflectionTestUtils.setField(member, "user", user);

            final Comment comment = TestEmptyEntityGenerator.Comment();
            ReflectionTestUtils.setField(comment, "id", 1L);

            final Like like = TestEmptyEntityGenerator.Like();
            ReflectionTestUtils.setField(like, "id", 1L);
            ReflectionTestUtils.setField(like, "comment", comment);
            ReflectionTestUtils.setField(like, "member", member);

            given(likeRepository.findLikeWithMemberById(anyLong()))
                    .willReturn(Optional.of(like));

            likeService.doUnlike(like.getId(), 1L);

            verify(likeRepository, times(1)).delete(any(Like.class));
        }

        @Test
        @DisplayName("글 좋아요 취소는 해당 글의 좋아요 수가 1 감소하고, 좋아요가 삭제된다.")
        void doUnlikeAtPostSuccess() {
            final User user = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user, "id", 1L);

            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "id", 1L);
            ReflectionTestUtils.setField(member, "user", user);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 1L);
            ReflectionTestUtils.setField(post, "member", member);
            ReflectionTestUtils.setField(post, "likeCount", 1);

            final Like like = TestEmptyEntityGenerator.Like();
            ReflectionTestUtils.setField(like, "id", 1L);
            ReflectionTestUtils.setField(like, "post", post);
            ReflectionTestUtils.setField(like, "member", member);

            given(likeRepository.findLikeWithMemberById(anyLong()))
                    .willReturn(Optional.of(like));

            likeService.doUnlike(like.getId(), 1L);

            assertThat(post.getLikeCount()).isZero();
            verify(likeRepository, times(1)).delete(any(Like.class));
        }

        @Test
        @DisplayName("요청한 세션 유저와 좋아요 한 유저가 서로 다를 경우 NotAuthorizedMemberException 발생한다.")
        void requestedUserAndLikedUserNotSameFail() {
            final User user = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user, "id", 1L);

            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "id", 1L);
            ReflectionTestUtils.setField(member, "user", user);

            final Comment comment = TestEmptyEntityGenerator.Comment();
            ReflectionTestUtils.setField(comment, "id", 1L);

            final Like like = TestEmptyEntityGenerator.Like();
            ReflectionTestUtils.setField(like, "id", 1L);
            ReflectionTestUtils.setField(like, "comment", comment);
            ReflectionTestUtils.setField(like, "member", member);

            given(likeRepository.findLikeWithMemberById(anyLong()))
                    .willReturn(Optional.of(like));

            assertThatThrownBy(() -> likeService.doUnlike(like.getId(), 2L))
                    .isInstanceOf(NotAuthorizedMemberException.class);
        }
    }

    @Nested
    @DisplayName("글에 좋아요한 유저들 조회시")
    class GetLikeMembersAtPostTest {
        @Test
        @DisplayName("글이 작성된 공개 커뮤니티에 가입되지 않은 유저가 요청할시 페이지네이션해서 가져온다.")
        void notJoinedUserRequestSuccess() {
            final User user = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user, "id", 1L);

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);
            ReflectionTestUtils.setField(community, "isPrivate", false);

            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "id", 1L);
            ReflectionTestUtils.setField(member, "user", user);
            ReflectionTestUtils.setField(member, "community", community);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 1L);
            ReflectionTestUtils.setField(post, "community", community);

            given(postRepository.findByPostId(anyLong()))
                    .willReturn(post);

            final Like like = TestEmptyEntityGenerator.Like();
            ReflectionTestUtils.setField(like, "id", 1L);
            ReflectionTestUtils.setField(like, "post", post);
            ReflectionTestUtils.setField(like, "member", member);

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.empty());

            Pageable pageable = PageRequest.of(0, 1);
            List<Like> likes = List.of(like);
            Page<Like> likePage = PageableExecutionUtils.getPage(likes, pageable, () -> likes.size());
            given(likeRepository.findPostLikePageWithMemberByPostId(anyLong(), any(Pageable.class)))
                    .willReturn(likePage);

            List<User> users = List.of(user);
            given(userRepository.findUsersByIds(anyList()))
                    .willReturn(users);

            LikeMembersAtPost likeMembers = likeService
                    .getLikeMembersAtPost(post.getId(), 2L, pageable);

            assertThat(likeMembers.getMembers().size()).isEqualTo(1);
            assertThat(likeMembers.getMembers().get(0).getId()).isEqualTo(user.getId());

            PaginationDto pageInfo = likeMembers.getPageInfo();
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.isHasNext()).isFalse();
        }

        @Test
        @DisplayName("비공개 커뮤니티에 비가입상태로 요청했을때 NotJoinedException 발생한다.")
        void notJoinedMemberInPrivateCommunityRequestFail() {
            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);
            ReflectionTestUtils.setField(community, "isPrivate", true);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 1L);
            ReflectionTestUtils.setField(post, "community", community);

            given(postRepository.findByPostId(anyLong()))
                    .willReturn(post);

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.empty());

            Pageable pageable = PageRequest.of(0, 1);

            assertThatThrownBy(() -> likeService
                    .getLikeMembersAtPost(post.getId(), 2L, pageable))
                    .isInstanceOf(NotJoinedMemberException.class);
        }
    }

    @Nested
    @DisplayName("댓글에 좋아요한 유저들 조회시")
    class GetLikeMembersAtCommentResponseTest {
        @Test
        @DisplayName("댓글이 작성된 공개 커뮤니티에 가입되지 않은 유저가 요청할시 페이지네이션해서 가져온다.")
        void notJoinedUserRequestSuccess() {
            final User user = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user, "id", 1L);

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);
            ReflectionTestUtils.setField(community, "isPrivate", false);

            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "id", 1L);
            ReflectionTestUtils.setField(member, "user", user);
            ReflectionTestUtils.setField(member, "community", community);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 1L);
            ReflectionTestUtils.setField(post, "community", community);

            final Comment comment = TestEmptyEntityGenerator.Comment();
            ReflectionTestUtils.setField(comment, "id", 1L);
            ReflectionTestUtils.setField(comment, "post", post);
            ReflectionTestUtils.setField(comment, "member", member);

            given(commentRepository.findById(anyLong()))
                    .willReturn(Optional.of(comment));


            final Like like = TestEmptyEntityGenerator.Like();
            ReflectionTestUtils.setField(like, "id", 1L);
            ReflectionTestUtils.setField(like, "comment", comment);
            ReflectionTestUtils.setField(like, "member", member);

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.empty());

            Pageable pageable = PageRequest.of(0, 1);
            List<Like> likes = List.of(like);
            Page<Like> likePage = PageableExecutionUtils.getPage(likes, pageable, () -> likes.size());
            given(likeRepository.findCommentLikePageWithMemberByCommentId(anyLong(), any(Pageable.class)))
                    .willReturn(likePage);

            List<User> users = List.of(user);
            given(userRepository.findUsersByIds(anyList()))
                    .willReturn(users);

            LikeMembersAtCommentResponse likeMembers = likeService
                    .getLikeMembersAtComment(comment.getId(), 2L, pageable);

            assertThat(likeMembers.getMembers().size()).isEqualTo(1);
            assertThat(likeMembers.getMembers().get(0).getId()).isEqualTo(user.getId());

            PaginationDto pageInfo = likeMembers.getPageInfo();
            assertThat(pageInfo.getNextPage()).isEqualTo(1);
            assertThat(pageInfo.isHasNext()).isFalse();
        }

        @Test
        @DisplayName("비공개 커뮤니티에 비가입상태로 요청했을때 NotJoinedException 발생한다.")
        void notJoinedMemberInPrivateCommunityRequestFail() {
            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);
            ReflectionTestUtils.setField(community, "isPrivate", true);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 1L);
            ReflectionTestUtils.setField(post, "community", community);

            final Comment comment = TestEmptyEntityGenerator.Comment();
            ReflectionTestUtils.setField(comment, "id", 1L);
            ReflectionTestUtils.setField(comment, "post", post);

            given(commentRepository.findById(anyLong()))
                    .willReturn(Optional.of(comment));

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.empty());

            Pageable pageable = PageRequest.of(0, 1);

            assertThatThrownBy(() -> likeService
                    .getLikeMembersAtComment(comment.getId(), 2L, pageable))
                    .isInstanceOf(NotJoinedMemberException.class);
        }
    }
}