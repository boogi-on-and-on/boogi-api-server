package boogi.apiserver.domain.like.repository;

import boogi.apiserver.builder.TestComment;
import boogi.apiserver.builder.TestLike;
import boogi.apiserver.builder.TestMember;
import boogi.apiserver.builder.TestPost;
import boogi.apiserver.domain.comment.repository.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.like.exception.LikeNotFoundException;
import boogi.apiserver.domain.member.repository.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.post.post.repository.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.utils.RepositoryTest;
import boogi.apiserver.utils.TestTimeReflection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LikeRepositoryTest extends RepositoryTest {

    @Autowired
    LikeRepository likeRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    MemberRepository memberRepository;

    @Nested
    @DisplayName("ID로 좋아요 조회시")
    class FindLikeById {
        @Test
        @DisplayName("좋아요 조회에 성공한다.")
        void findLikeByIdSuccess() {
            Like like = TestLike.builder().build();
            likeRepository.save(like);

            cleanPersistenceContext();

            Like findLike = likeRepository.findLikeById(like.getId());

            assertThat(findLike).isNotNull();
            assertThat(findLike.getId()).isEqualTo(like.getId());
        }

        @Test
        @DisplayName("좋아요가 존재하지 않을시 LikeNotFoundException 발생")
        void notExistLikeFail() {
            assertThatThrownBy(() -> likeRepository.findLikeById(1L))
                    .isInstanceOf(LikeNotFoundException.class);
        }
    }

    @Test
    @DisplayName("게시글 ID로 해당 게시글 좋아요들을 모두 삭제한다.")
    void deleteAllPostLikeByPostId() {
        Post post = TestPost.builder().build();
        postRepository.save(post);

        List<Like> likes = IntStream.range(0, 5)
                .mapToObj(i -> TestLike.builder()
                        .post(post)
                        .build()
                ).collect(Collectors.toList());
        likeRepository.saveAll(likes);

        cleanPersistenceContext();

        likeRepository.deleteAllPostLikeByPostId(post.getId());

        assertThat(likeRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("댓글 ID로 해당 댓글 좋아요들을 모두 삭제한다.")
    void deleteAllCommentLikeByCommentId() {
        Comment comment = TestComment.builder().build();
        commentRepository.save(comment);

        List<Like> likes = IntStream.range(0, 5)
                .mapToObj(i -> TestLike.builder()
                        .comment(comment)
                        .build()
                ).collect(Collectors.toList());
        likeRepository.saveAll(likes);

        cleanPersistenceContext();

        likeRepository.deleteAllCommentLikeByCommentId(comment.getId());

        assertThat(likeRepository.findAll()).isEmpty();
    }

    @Nested
    @DisplayName("게시글과 멤버 ID로 해당 좋아요가 존재하는지 확인")
    class ExistsLikeByPostIdAndMemberId {
        @Test
        @DisplayName("좋아요가 존재할때 true를 반환한다.")
        void existLikeSuccess() {
            Post post = TestPost.builder().build();
            postRepository.save(post);

            Member member = TestMember.builder().build();
            memberRepository.save(member);

            Like like = TestLike.builder()
                    .post(post)
                    .member(member)
                    .build();
            likeRepository.save(like);

            cleanPersistenceContext();

            boolean isExist = likeRepository.existsLikeByPostIdAndMemberId(post.getId(), member.getId());

            assertThat(isExist).isTrue();
        }

        @Test
        @DisplayName("좋아요가 존재하지 않을때 false를 반환한다.")
        void notExistLikeSuccess() {
            boolean isExist = likeRepository.existsLikeByPostIdAndMemberId(1L, 1L);

            assertThat(isExist).isFalse();
        }
    }

    @Nested
    @DisplayName("댓글과 멤버 ID로 해당 좋아요가 존재하는지 확인")
    class ExistsLikeByCommentIdAndMemberId {
        @Test
        @DisplayName("좋아요가 존재할때 true를 반환한다.")
        void existLikeSuccess() {
            Comment comment = TestComment.builder().build();
            commentRepository.save(comment);

            Member member = TestMember.builder().build();
            memberRepository.save(member);

            Like like = TestLike.builder()
                    .comment(comment)
                    .member(member)
                    .build();
            likeRepository.save(like);

            cleanPersistenceContext();

            boolean isExist = likeRepository.existsLikeByCommentIdAndMemberId(comment.getId(), member.getId());

            assertThat(isExist).isTrue();
        }

        @Test
        @DisplayName("좋아요가 존재하지 않을때 false를 반환한다.")
        void notExistLikeSuccess() {
            boolean isExist = likeRepository.existsLikeByCommentIdAndMemberId(1L, 1L);

            assertThat(isExist).isFalse();
        }
    }

    @Test
    @DisplayName("해당 댓글들에 해당 멤버가 한 모든 좋아요들을 조회한다.")
    void findCommentLikesByCommentIdsAndMemberId() {
        final Member member = TestMember.builder().build();
        memberRepository.save(member);

        final Comment comment1 = TestComment.builder().build();
        final Comment comment2 = TestComment.builder().build();
        List<Comment> comments = List.of(comment1, comment2);
        commentRepository.saveAll(comments);

        final Like like1 = TestLike.builder()
                .comment(comment1)
                .member(member)
                .build();
        final Like like2 = TestLike.builder()
                .comment(comment2)
                .member(member)
                .build();
        likeRepository.saveAll(List.of(like1, like2));

        cleanPersistenceContext();

        List<Long> commentIds = comments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());

        List<Like> commentLikes = likeRepository.findCommentLikesByCommentIdsAndMemberId(commentIds, member.getId());

        assertThat(commentLikes).hasSize(2);
        assertThat(commentLikes).extracting("id").containsExactly(like1.getId(), like2.getId());
    }

    @Test
    @DisplayName("글에 한 좋아요들을 오래된 순으로 페이지네이션해서 fetch join으로 Member와 같이 조회한다.")
    void findPostLikePageWithMemberByPostId() {
        final Post post = TestPost.builder().build();
        postRepository.save(post);

        final Member member = TestMember.builder().build();
        memberRepository.save(member);

        final Like like1 = TestLike.builder()
                .post(post)
                .member(member)
                .build();
        TestTimeReflection.setCreatedAt(like1, LocalDateTime.now().minusHours(2));

        final Like like2 = TestLike.builder()
                .post(post)
                .member(member)
                .build();
        TestTimeReflection.setCreatedAt(like2, LocalDateTime.now().minusHours(1));

        final Like like3 = TestLike.builder().build();
        TestTimeReflection.setCreatedAt(like3, LocalDateTime.now());

        likeRepository.saveAll(List.of(like1, like2, like3));

        cleanPersistenceContext();

        Pageable pageable = PageRequest.of(0, 2);
        Slice<Like> postLikePage = likeRepository.findPostLikePageWithMemberByPostId(post.getId(), pageable);

        List<Like> postLikes = postLikePage.getContent();

        assertThat(postLikes).hasSize(2);
        assertThat(postLikes).extracting("id").containsExactly(like1.getId(), like2.getId());
        assertThat(postLikes).extracting("post").extracting("id").containsOnly(post.getId());
        assertThat(postLikes).extracting("member").extracting("id").containsOnly(member.getId());
        assertThat(isLoaded(postLikes.get(0).getMember())).isTrue();
        assertThat(isLoaded(postLikes.get(1).getMember())).isTrue();

        assertThat(postLikePage.getNumber()).isZero();
        assertThat(postLikePage.hasNext()).isFalse();
    }

    @Test
    @DisplayName("댓글에 한 좋아요들을 오래된 순으로 페이지네이션해서 fetch join으로 Member와 같이 조회한다.")
    void findCommentLikePageWithMemberByCommentId() {
        final Comment comment = TestComment.builder().build();
        commentRepository.save(comment);

        final Member member = TestMember.builder().build();
        memberRepository.save(member);

        final Like like1 = TestLike.builder()
                .comment(comment)
                .member(member)
                .build();
        TestTimeReflection.setCreatedAt(like1, LocalDateTime.now().minusHours(2));

        final Like like2 = TestLike.builder()
                .comment(comment)
                .member(member)
                .build();
        TestTimeReflection.setCreatedAt(like2, LocalDateTime.now().minusHours(2));

        final Like like3 = TestLike.builder().build();
        TestTimeReflection.setCreatedAt(like3, LocalDateTime.now());

        likeRepository.saveAll(List.of(like1, like2, like3));

        cleanPersistenceContext();

        Pageable pageable = PageRequest.of(0, 2);

        Slice<Like> commentLikePage = likeRepository.findCommentLikePageWithMemberByCommentId(comment.getId(), pageable);

        List<Like> commentLikes = commentLikePage.getContent();

        assertThat(commentLikes).hasSize(2);

        assertThat(commentLikes).extracting("id").containsExactly(like1.getId(), like2.getId());
        assertThat(commentLikes).extracting("comment").extracting("id").containsOnly(comment.getId());
        assertThat(commentLikes).extracting("member").extracting("id").containsOnly(member.getId());
        assertThat(isLoaded(commentLikes.get(0).getMember())).isTrue();
        assertThat(isLoaded(commentLikes.get(1).getMember())).isTrue();

        assertThat(commentLikePage.getNumber()).isZero();
        assertThat(commentLikePage.hasNext()).isFalse();
    }

    @Test
    @DisplayName("CommentId와 댓글에 한 좋아요 개수를 매핑한 Map을 좋아요 개수가 0인 경우를 제외하고 CommentId들로 조회한다.")
    void getCommentLikeCountsByCommentIds() {
        final Comment comment1 = TestComment.builder().build();
        final Comment comment2 = TestComment.builder().build();
        List<Comment> comments = List.of(comment1, comment2);
        commentRepository.saveAll(comments);

        final Like like1 = TestLike.builder().comment(comment1).build();
        final Like like2 = TestLike.builder().comment(comment1).build();
        likeRepository.saveAll(List.of(like1, like2));

        cleanPersistenceContext();

        List<Long> commentIds = comments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());

        Map<Long, Long> commentLikeCountMap = likeRepository.getCommentLikeCountsByCommentIds(commentIds);

        assertThat(commentLikeCountMap).hasSize(1);
        assertThat(commentLikeCountMap.get(comment1.getId())).isEqualTo(2);
        assertThat(commentLikeCountMap.containsKey(comment2.getId())).isFalse();
    }
}