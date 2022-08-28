package boogi.apiserver.domain.like.application;

import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.community.community.application.CommunityValidationService;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.like.dao.LikeRepository;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.like.dto.response.LikeMembersAtComment;
import boogi.apiserver.domain.like.dto.response.LikeMembersAtPost;
import boogi.apiserver.domain.like.exception.AlreadyDoLikeException;
import boogi.apiserver.domain.member.application.MemberValidationService;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class LikeCoreServiceTest {

    @InjectMocks
    LikeCoreService likeCoreService;

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
    private MemberValidationService memberValidationService;

    @Mock
    private CommunityValidationService communityValidationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommunityRepository communityRepository;

    @Nested
    @DisplayName("글에 좋아요할 시")
    class DoLikeAtPostTest {
        @Test
        @DisplayName("해당 글의 좋아요 수가 1 증가하고, 좋아요가 생성된다.")
        void doLikeAtPostSuccess() {
            Community community = Community.builder()
                    .id(1L)
                    .build();

            Post post = Post.builder()
                    .id(1L)
                    .community(community)
                    .likeCount(0)
                    .build();
            given(postRepository.findById(anyLong()))
                    .willReturn(Optional.of(post));

            Member member = Member.builder()
                    .id(1L)
                    .build();
            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member));

            given(likeValidationService.checkOnlyAlreadyDoPostLike(anyLong(), anyLong()))
                    .willReturn(false);

            Like newLike = likeCoreService.doLikeAtPost(post.getId(), 1L);

            assertThat(newLike.getPost().getId()).isEqualTo(post.getId());
            assertThat(newLike.getMember().getId()).isEqualTo(member.getId());
            assertThat(newLike.getPost().getLikeCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("해당 멤버가 해당 글에 이미 좋아요를 누른 경우 AlreadyDoLikeException 발생한다.")
        void alreadyDoLikeFail() {
            Community community = Community.builder()
                    .id(1L)
                    .build();

            Post post = Post.builder()
                    .id(1L)
                    .community(community)
                    .likeCount(0)
                    .build();
            given(postRepository.findById(anyLong()))
                    .willReturn(Optional.of(post));

            Member member = Member.builder()
                    .id(1L)
                    .build();

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member));

            given(likeValidationService.checkOnlyAlreadyDoPostLike(anyLong(), anyLong()))
                    .willReturn(true);

            assertThatThrownBy(() -> likeCoreService.doLikeAtPost(post.getId(), 1L))
                    .isInstanceOf(AlreadyDoLikeException.class);
        }
    }

    @Nested
    @DisplayName("댓글에 좋아요할 시")
    class DoLikeAtCommentTest {
        @Test
        @DisplayName("해당 댓글에 좋아요가 생성된다.")
        void doLikeAtCommentSuccess() {
            Community community = Community.builder()
                    .id(1L)
                    .build();

            Member member = Member.builder()
                    .id(1L)
                    .community(community)
                    .build();
            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member));

            Comment comment = Comment.builder()
                    .id(1L)
                    .member(member)
                    .build();
            given(commentRepository.findCommentWithMemberByCommentId(anyLong()))
                    .willReturn(Optional.of(comment));

            given(likeValidationService.checkOnlyAlreadyDoCommentLike(anyLong(), anyLong()))
                    .willReturn(false);

            Like newLike = likeCoreService.doLikeAtComment(comment.getId(), 1L);

            assertThat(newLike.getComment().getId()).isEqualTo(comment.getId());
            assertThat(newLike.getMember().getId()).isEqualTo(member.getId());
        }

        @Test
        @DisplayName("해당 멤버가 해당 댓글에 이미 좋아요를 누른 경우 AlreadyDoLikeException 발생한다.")
        void alreadyDoLikeFail() {
            Community community = Community.builder()
                    .id(1L)
                    .build();

            Member member = Member.builder()
                    .id(1L)
                    .community(community)
                    .build();

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member));

            Comment comment = Comment.builder()
                    .id(1L)
                    .member(member)
                    .build();
            given(commentRepository.findCommentWithMemberByCommentId(anyLong()))
                    .willReturn(Optional.of(comment));

            given(likeValidationService.checkOnlyAlreadyDoCommentLike(anyLong(), anyLong()))
                    .willReturn(true);

            assertThatThrownBy(() -> likeCoreService.doLikeAtComment(comment.getId(), 1L))
                    .isInstanceOf(AlreadyDoLikeException.class);
        }
    }

    @Nested
    @DisplayName("좋아요 취소할 시")
    class DoUnlikeTest {
        @Test
        @DisplayName("댓글 좋아요 취소는 좋아요가 삭제된다.")
        void doUnlikeAtCommentSuccess() {
            User user = User.builder()
                    .id(1L)
                    .build();

            Member member = Member.builder()
                    .id(1L)
                    .user(user)
                    .build();

            Comment comment = Comment.builder()
                    .id(1L)
                    .build();

            Like like = Like.builder()
                    .id(1L)
                    .comment(comment)
                    .member(member)
                    .build();
            given(likeRepository.findLikeWithMemberById(anyLong()))
                    .willReturn(Optional.of(like));

            likeCoreService.doUnlike(like.getId(), 1L);

            verify(likeRepository, times(1)).delete(any(Like.class));
        }

        @Test
        @DisplayName("글 좋아요 취소는 해당 글의 좋아요 수가 1 감소하고, 좋아요가 삭제된다.")
        void doUnlikeAtPostSuccess() {
            User user = User.builder()
                    .id(1L)
                    .build();

            Member member = Member.builder()
                    .id(1L)
                    .user(user)
                    .build();

            Post post = Post.builder()
                    .id(1L)
                    .member(member)
                    .likeCount(1)
                    .build();

            Like like = Like.builder()
                    .id(1L)
                    .post(post)
                    .member(member)
                    .build();
            given(likeRepository.findLikeWithMemberById(anyLong()))
                    .willReturn(Optional.of(like));

            likeCoreService.doUnlike(like.getId(), 1L);

            assertThat(post.getLikeCount()).isZero();
            verify(likeRepository, times(1)).delete(any(Like.class));
        }

        @Test
        @DisplayName("요청한 세션 유저와 좋아요 한 유저가 서로 다를 경우 NotAuthorizedMemberException 발생한다.")
        void requestedUserAndLikedUserNotSameFail() {
            User user = User.builder()
                    .id(1L)
                    .build();

            Member member = Member.builder()
                    .id(1L)
                    .user(user)
                    .build();

            Comment comment = Comment.builder()
                    .id(1L)
                    .build();

            Like like = Like.builder()
                    .id(1L)
                    .comment(comment)
                    .member(member)
                    .build();
            given(likeRepository.findLikeWithMemberById(anyLong()))
                    .willReturn(Optional.of(like));

            assertThatThrownBy(() -> likeCoreService.doUnlike(like.getId(), 2L))
                    .isInstanceOf(NotAuthorizedMemberException.class);
        }
    }

    @Nested
    @DisplayName("글에 좋아요한 유저들 조회시")
    class GetLikeMembersAtPostTest {
        @Test
        @DisplayName("글이 작성된 공개 커뮤니티에 가입되지 않은 유저가 요청할시 페이지네이션해서 가져온다.")
        void notJoinedUserRequestSuccess() {
            User user = User.builder()
                    .id(1L)
                    .build();

            Community community = Community.builder()
                    .id(1L)
                    .isPrivate(false)
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
            given(postQueryService.getPost(anyLong()))
                    .willReturn(post);

            Like like = Like.builder()
                    .id(1L)
                    .member(member)
                    .post(post)
                    .build();
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

            LikeMembersAtPost likeMembers = likeCoreService
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
            Community community = Community.builder()
                    .id(1L)
                    .isPrivate(true)
                    .build();

            Post post = Post.builder()
                    .id(1L)
                    .community(community)
                    .build();
            given(postQueryService.getPost(anyLong()))
                    .willReturn(post);

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.empty());

            Pageable pageable = PageRequest.of(0, 1);

            assertThatThrownBy(() -> likeCoreService
                    .getLikeMembersAtPost(post.getId(), 2L, pageable))
                    .isInstanceOf(NotJoinedMemberException.class);
        }
    }

    @Nested
    @DisplayName("댓글에 좋아요한 유저들 조회시")
    class GetLikeMembersAtCommentTest {
        @Test
        @DisplayName("댓글이 작성된 공개 커뮤니티에 가입되지 않은 유저가 요청할시 페이지네이션해서 가져온다.")
        void notJoinedUserRequestSuccess() {
            User user = User.builder()
                    .id(1L)
                    .build();

            Community community = Community.builder()
                    .id(1L)
                    .isPrivate(false)
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

            Comment comment = Comment.builder()
                    .id(1L)
                    .post(post)
                    .member(member)
                    .build();
            given(commentRepository.findById(anyLong()))
                    .willReturn(Optional.of(comment));

            Like like = Like.builder()
                    .id(1L)
                    .member(member)
                    .comment(comment)
                    .build();
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

            LikeMembersAtComment likeMembers = likeCoreService
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
            Community community = Community.builder()
                    .isPrivate(true)
                    .id(1L)
                    .build();

            Post post = Post.builder()
                    .id(1L)
                    .community(community)
                    .build();

            Comment comment = Comment.builder()
                    .id(1L)
                    .post(post)
                    .build();
            given(commentRepository.findById(anyLong()))
                    .willReturn(Optional.of(comment));

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.empty());

            Pageable pageable = PageRequest.of(0, 1);

            assertThatThrownBy(() -> likeCoreService
                    .getLikeMembersAtComment(comment.getId(), 2L, pageable))
                    .isInstanceOf(NotJoinedMemberException.class);
        }
    }
}