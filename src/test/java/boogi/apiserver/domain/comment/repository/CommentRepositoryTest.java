package boogi.apiserver.domain.comment.repository;

import boogi.apiserver.builder.TestComment;
import boogi.apiserver.builder.TestMember;
import boogi.apiserver.builder.TestPost;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.exception.CommentNotFoundException;
import boogi.apiserver.domain.member.repository.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.post.post.repository.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.user.repository.UserRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommentRepositoryTest extends RepositoryTest {

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PostRepository postRepository;

    @Nested
    @DisplayName("ID로 댓글 조회시")
    class FindCommentById {
        @Test
        @DisplayName("댓글 조회에 성공한다.")
        void findCommentByIdSuccess() {
            Comment comment = TestComment.builder().build();
            commentRepository.save(comment);

            cleanPersistenceContext();

            Comment findComment = commentRepository.findCommentById(comment.getId());

            assertThat(findComment).isNotNull();
        }

        @Test
        @DisplayName("댓글이 존재하지 않을시 CommentNotFoundException 발생")
        void notExistCommentFail() {
            assertThatThrownBy(() -> commentRepository.findCommentById(1l))
                    .isInstanceOf(CommentNotFoundException.class);
        }
    }

    @Test
    @DisplayName("해당 게시글에 달린 부모 댓글만 페이지네이션해서 조회한다.")
    void findParentCommentsWithMemberByPostId() {
        final String COMMENT_CONTENT = "댓글 내용";

        Post post = TestPost.builder().build();
        postRepository.save(post);

        Member member = TestMember.builder().build();
        memberRepository.save(member);

        List<Comment> comments = IntStream.range(0, 30).mapToObj(i -> {
                    Comment comment = TestComment.builder()
                            .content(COMMENT_CONTENT + i)
                            .post(post)
                            .member(member)
                            .parent(null)
                            .child(false)
                            .build();
                    TestTimeReflection.setCreatedAt(comment, LocalDateTime.now().plusDays(i));
                    return comment;
                }
        ).collect(Collectors.toList());
        commentRepository.saveAll(comments);

        cleanPersistenceContext();

        Pageable pageable = PageRequest.of(0, 20);

        Slice<Comment> parentCommentPage =
                commentRepository.findParentCommentsWithMemberByPostId(pageable, post.getId());

        String[] expectedCommentContents = IntStream.range(0, 20)
                .mapToObj(i -> COMMENT_CONTENT + i)
                .toArray(String[]::new);

        List<Comment> parentComments = parentCommentPage.getContent();
        assertThat(parentComments).hasSize(20);

        assertThat(parentComments).extracting("content").containsExactly(expectedCommentContents);
        assertThat(parentComments).extracting("parent").containsOnlyNulls();
        assertThat(parentComments).extracting("child").containsOnly(false);
        assertThat(isLoaded(parentComments.get(0))).isTrue();

        assertThat(parentCommentPage.hasNext()).isTrue();
    }

    @Test
    @DisplayName("부모 댓글 ID들로 해당 자식 댓글들을 모두 조회한다.")
    void findChildCommentsWithMemberByParentCommentIds() {
        final String COMMENT_CONTENT = "댓글 내용";

        Member member = TestMember.builder().build();
        memberRepository.save(member);

        List<Comment> parentComments = IntStream.range(0, 2)
                .mapToObj(i -> TestComment.builder()
                        .content(COMMENT_CONTENT + i)
                        .parent(null)
                        .child(false)
                        .build()
                ).collect(Collectors.toList());
        commentRepository.saveAll(parentComments);

        Stream<Comment> childCommentStream1 = IntStream.range(2, 8)
                .mapToObj(i -> TestComment.builder()
                        .content(COMMENT_CONTENT + i)
                        .member(member)
                        .parent(parentComments.get(0))
                        .build()
                );
        Stream<Comment> childCommentStream2 = IntStream.range(8, 15)
                .mapToObj(i -> TestComment.builder()
                        .content(COMMENT_CONTENT + i)
                        .member(member)
                        .parent(parentComments.get(1))
                        .build()
                );
        List<Comment> childComments = Stream.concat(childCommentStream1, childCommentStream2)
                .collect(Collectors.toList());
        commentRepository.saveAll(childComments);

        cleanPersistenceContext();

        List<Long> parentCommentIds = parentComments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());

        List<Comment> findChildComments =
                commentRepository.findChildCommentsWithMemberByParentCommentIds(parentCommentIds);

        String[] expectedChildContents = IntStream.range(2, 15)
                .mapToObj(i -> COMMENT_CONTENT + i)
                .toArray(String[]::new);

        assertThat(findChildComments).hasSize(13);
        assertThat(findChildComments).extracting("content").containsExactly(expectedChildContents);
        assertThat(findChildComments).extracting("parent").extracting("id")
                .containsOnly(parentCommentIds.get(0), parentCommentIds.get(1));
        assertThat(isLoaded(findChildComments.get(0).getMember())).isTrue();
    }

    @Test
    @DisplayName("멤버의 ID들로 작성한 댓글들을 페이지네이션해서 조회한다.")
    void getUserCommentPageByMemberIds() {
        final String COMMENT_CONTENT = "댓글 내용";

        final Member member1 = TestMember.builder().build();
        final Member member2 = TestMember.builder().build();
        memberRepository.saveAll(List.of(member1, member2));

        List<Comment> member1Comments = IntStream.range(0, 10).mapToObj(i -> {
                    Comment comment = TestComment.builder()
                            .content(COMMENT_CONTENT + i)
                            .member(member1)
                            .build();
                    TestTimeReflection.setCreatedAt(comment, LocalDateTime.now().plusDays(i));
                    return comment;
                }
        ).collect(Collectors.toList());

        List<Comment> member2Comments = IntStream.range(10, 20).mapToObj(i -> {
                    Comment comment = TestComment.builder()
                            .content(COMMENT_CONTENT + i)
                            .member(member2)
                            .build();
                    TestTimeReflection.setCreatedAt(comment, LocalDateTime.now().plusDays(i));
                    return comment;
                }
        ).collect(Collectors.toList());

        commentRepository.saveAll(member1Comments);
        commentRepository.saveAll(member2Comments);

        cleanPersistenceContext();

        List<Long> memberIds = List.of(member1.getId(), member2.getId());
        Pageable pageable = PageRequest.of(0, 15);
        Slice<Comment> userCommentPage = commentRepository.getUserCommentPageByMemberIds(memberIds, pageable);

        List<Comment> userComments = userCommentPage.getContent();
        List<String> commentContents = IntStream.range(5, 20)
                .mapToObj(i -> COMMENT_CONTENT + i)
                .collect(Collectors.toList());
        Collections.reverse(commentContents);

        assertThat(userComments).hasSize(15);
        assertThat(userComments).extracting("content").isEqualTo(commentContents);
        assertThat(userComments).extracting("member").extracting("id")
                .containsOnly(member1.getId(), member2.getId());

        assertThat(userCommentPage.hasNext()).isTrue();
    }
}