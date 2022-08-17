package boogi.apiserver.domain.comment.dao;

import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.utils.PersistenceUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    private EntityManager em;

    private PersistenceUtil persistenceUtil;

    @BeforeAll
    void init() {
        persistenceUtil = new PersistenceUtil(em);
    }

    @Test
    void getUserCommentPage() {
        User user = User.builder()
                .build();
        userRepository.save(user);

        Member member = Member.builder()
                .user(user)
                .build();
        memberRepository.save(member);

        Post post = Post.builder()
                .member(member)
                .build();
        postRepository.save(post);

        Comment comment1 = Comment.builder()
                .member(member)
                .post(post)
                .build();
        comment1.setCreatedAt(LocalDateTime.now().minusHours(3));

        Comment comment2 = Comment.builder()
                .member(member)
                .post(post)
                .build();
        comment2.setCreatedAt(LocalDateTime.now().minusHours(2));

        Comment comment3 = Comment.builder()
                .member(member)
                .post(post)
                .build();
        comment3.setCreatedAt(LocalDateTime.now().minusHours(1));

        commentRepository.saveAll(List.of(comment1, comment2, comment3));

        persistenceUtil.cleanPersistenceContext();

        Page<Comment> commentPage1 = commentRepository.getUserCommentPage(PageRequest.of(0, 2), user.getId());
        Page<Comment> commentPage2 = commentRepository.getUserCommentPage(PageRequest.of(1, 2), user.getId());

        List<Comment> comments1 = commentPage1.getContent(); //first page
        List<Comment> comments2 = commentPage2.getContent(); //second page

        Comment first = comments1.get(0);
        Comment second = comments1.get(1);

        assertThat(first.getId()).isEqualTo(comment3.getId());
        assertThat(second.getId()).isEqualTo(comment2.getId());

        assertThat(first.getCreatedAt()).isAfter(second.getCreatedAt());

        assertThat(comments1.size()).isEqualTo(2);
        assertThat(commentPage1.hasNext()).isTrue();

        assertThat(comments2.size()).isEqualTo(1);
        assertThat(commentPage2.hasNext()).isFalse();
    }

    @Test
    void getUserCommentPage_멤버아이디_없을때() {
        Page<Comment> commentPage = commentRepository.getUserCommentPage(PageRequest.of(0, 3), 2L);

        assertThat(commentPage.getTotalElements()).isEqualTo(0);
        assertThat(commentPage.hasNext()).isFalse();
    }

    @Test
    @DisplayName("fetch join으로 댓글에 member도 같이 가져온다.")
    void testFindCommentWithMemberByCommentId() {
        Member member = Member.builder()
                .build();
        memberRepository.save(member);

        Post post = Post.builder()
                .build();
        postRepository.save(post);

        Comment comment1 = Comment.builder()
                .post(post)
                .member(member)
                .build();
        commentRepository.save(comment1);

        Comment comment2 = Comment.builder()
                .post(post)
                .member(member)
                .parent(comment1)
                .build();
        commentRepository.save(comment2);

        persistenceUtil.cleanPersistenceContext();

        Comment findComment2 = commentRepository
                .findCommentWithMemberByCommentId(comment2.getId()).orElse(null);
        if (findComment2 == null) {
            Assertions.fail();
        }
        assertThat(persistenceUtil.isLoaded(findComment2.getMember())).isTrue();
        assertThat(persistenceUtil.isLoaded(findComment2.getPost())).isFalse();
        assertThat(persistenceUtil.isLoaded(findComment2.getParent())).isFalse();

        Comment findComment1 = commentRepository
                .findCommentWithMemberByCommentId(comment1.getId()).orElse(null);
        if (findComment1 == null) {
            Assertions.fail();
        }
        assertThat(persistenceUtil.isLoaded(findComment1.getMember())).isTrue();
        assertThat(persistenceUtil.isLoaded(findComment1.getPost())).isFalse();
        assertThat(findComment1.getParent()).isNull();
    }

    @Test
    @DisplayName("한 post에 달린 부모 댓글만 페이지네이션하고 오래된 순으로 member와 같이 가져온다.")
    void testFindParentCommentsWithMemberByPostId() {
        Member member = Member.builder()
                .build();
        memberRepository.save(member);

        Post post = Post.builder()
                .build();
        postRepository.save(post);

        Comment comment1 = Comment.builder()
                .post(post)
                .member(member)
                .child(false)
                .build();
        comment1.setCreatedAt(LocalDateTime.now().minusHours(2));
        commentRepository.save(comment1);

        Comment comment2 = Comment.builder()
                .post(post)
                .member(member)
                .parent(comment1)
                .child(true)
                .build();
        comment2.setCreatedAt(LocalDateTime.now().minusHours(1));
        commentRepository.save(comment2);

        Comment comment3 = Comment.builder()
                .post(post)
                .member(member)
                .child(false)
                .build();
        comment3.setCreatedAt(LocalDateTime.now());
        commentRepository.save(comment3);

        persistenceUtil.cleanPersistenceContext();

        Pageable pageable = PageRequest.of(0, 2);
        Page<Comment> parentCommentsPage = commentRepository
                .findParentCommentsWithMemberByPostId(pageable, post.getId());
        List<Comment> parentComments = parentCommentsPage.getContent();

        assertThat(parentComments.size()).isEqualTo(2);

        Comment firstComment = parentComments.get(0);
        Comment secondComment = parentComments.get(1);

        assertThat(firstComment.getId()).isEqualTo(comment1.getId());
        assertThat(firstComment.getParent()).isNull();
        assertThat(persistenceUtil.isLoaded(firstComment.getMember())).isTrue();
        assertThat(persistenceUtil.isLoaded(firstComment.getPost())).isFalse();

        assertThat(secondComment.getId()).isEqualTo(comment3.getId());
        assertThat(secondComment.getParent()).isNull();
        assertThat(persistenceUtil.isLoaded(secondComment.getMember())).isTrue();
        assertThat(persistenceUtil.isLoaded(secondComment.getPost())).isFalse();
    }

    @Test
    @DisplayName("부모 댓글들에 달린 자식 댓글들을 오래된 순으로 member와 같이 가져온다.")
    void testFindChildCommentsWithMemberByParentCommentIds() {
        Member member = Member.builder()
                .build();
        memberRepository.save(member);

        Post post = Post.builder()
                .build();
        postRepository.save(post);

        Comment comment1 = Comment.builder()
                .post(post)
                .member(member)
                .child(false)
                .build();
        comment1.setCreatedAt(LocalDateTime.now().minusHours(2));
        commentRepository.save(comment1);

        Comment comment2 = Comment.builder()
                .post(post)
                .member(member)
                .parent(comment1)
                .child(true)
                .build();
        comment2.setCreatedAt(LocalDateTime.now().minusHours(1));
        commentRepository.save(comment2);

        Comment comment3 = Comment.builder()
                .post(post)
                .member(member)
                .child(false)
                .build();
        comment3.setCreatedAt(LocalDateTime.now());
        commentRepository.save(comment3);

        Comment comment4 = Comment.builder()
                .post(post)
                .member(member)
                .parent(comment1)
                .child(true)
                .build();
        comment4.setCreatedAt(LocalDateTime.now());
        commentRepository.save(comment4);

        persistenceUtil.cleanPersistenceContext();

        List<Long> parentCommentIds = List.of(comment1.getId(), comment3.getId());
        List<Comment> childComments = commentRepository
                .findChildCommentsWithMemberByParentCommentIds(parentCommentIds);

        assertThat(childComments.size()).isEqualTo(2);

        Comment firstComment = childComments.get(0);
        Comment secondComment = childComments.get(1);

        assertThat(firstComment.getId()).isEqualTo(comment2.getId());
        assertThat(firstComment.getParent().getId()).isEqualTo(comment1.getId());
        assertThat(persistenceUtil.isLoaded(firstComment.getParent())).isFalse();
        assertThat(persistenceUtil.isLoaded(firstComment.getMember())).isTrue();
        assertThat(persistenceUtil.isLoaded(firstComment.getPost())).isFalse();

        assertThat(secondComment.getId()).isEqualTo(comment4.getId());
        assertThat(secondComment.getParent().getId()).isEqualTo(comment1.getId());
        assertThat(persistenceUtil.isLoaded(secondComment.getParent())).isFalse();
        assertThat(persistenceUtil.isLoaded(secondComment.getMember())).isTrue();
        assertThat(persistenceUtil.isLoaded(secondComment.getPost())).isFalse();
    }

    @Test
    @DisplayName("삭제되지 않은 댓글의 경우만 가져온다.")
    void testFindCommentById() {
        Comment comment1 = Comment.builder()
                .build();
        commentRepository.save(comment1);

        Comment comment2 = Comment.builder()
                .build();
//        comment2.setCanceledAt(LocalDateTime.now());
        commentRepository.save(comment2);

        Comment comment3 = Comment.builder()
                .build();
        comment3.deleteComment();
        commentRepository.save(comment3);

        persistenceUtil.cleanPersistenceContext();

        Comment findComment1 = commentRepository
                .findCommentById(comment1.getId()).orElse(null);
        if (findComment1 == null) {
            Assertions.fail();
        }
        assertThat(findComment1.getId()).isEqualTo(comment1.getId());
        assertThat(findComment1.getDeletedAt()).isNull();
//        assertThat(findComment1.getCanceledAt()).isNull();

        if (commentRepository.findCommentById(comment2.getId()).isPresent()
                || commentRepository.findCommentById(comment3.getId()).isPresent()) {
            Assertions.fail();
        }
    }

    @Test
    @DisplayName("멤버가 작성한 삭제되지 않은 댓글들을 최근순으로 페이지네이션해서 가져온다.")
    void testGetUserCommentPageByMemberIds() {
        Member member1 = Member.builder()
                .build();
        memberRepository.save(member1);

        Member member2 = Member.builder()
                .build();
        memberRepository.save(member2);

        Post post = Post.builder()
                .commentCount(5)
                .build();
        postRepository.save(post);

        Comment comment1 = Comment.builder()
                .post(post)
                .member(member1)
                .child(false)
                .build();
        comment1.setCreatedAt(LocalDateTime.now().minusHours(3));
        commentRepository.save(comment1);

        Comment comment2 = Comment.builder()
                .post(post)
                .member(member2)
                .parent(comment1)
                .child(true)
                .build();
        comment2.setCreatedAt(LocalDateTime.now().minusHours(2));
        commentRepository.save(comment2);

        Comment comment3 = Comment.builder()
                .post(post)
                .member(member2)
                .child(false)
                .build();
        comment3.setCreatedAt(LocalDateTime.now().minusHours(1));
        commentRepository.save(comment3);

        Comment comment4 = Comment.builder()
                .post(post)
                .member(member1)
                .parent(comment1)
                .child(true)
                .build();
        comment4.setCreatedAt(LocalDateTime.now());
        commentRepository.save(comment4);

        Comment comment5 = Comment.builder()
                .post(post)
                .member(member1)
                .child(false)
                .build();
        comment5.setCreatedAt(LocalDateTime.now());
        comment5.deleteComment();
        commentRepository.save(comment5);

        persistenceUtil.cleanPersistenceContext();

        List<Long> memberIds = List.of(member1.getId(), member2.getId());
        Pageable pageable = PageRequest.of(0, 4);
        Page<Comment> commentPage = commentRepository.getUserCommentPageByMemberIds(memberIds, pageable);
        List<Comment> comments = commentPage.getContent();

        assertThat(comments.size()).isEqualTo(4);
        assertThat(post.getCommentCount()).isEqualTo(4);

        Comment findComment1 = comments.get(0);
        Comment findComment2 = comments.get(1);
        Comment findComment3 = comments.get(2);
        Comment findComment4 = comments.get(3);

        assertThat(findComment1.getId()).isEqualTo(comment4.getId());
        assertThat(findComment1.getMember().getId()).isEqualTo(member1.getId());
        assertThat(findComment1.getDeletedAt()).isNull();
//        assertThat(findComment1.getCanceledAt()).isNull();

        assertThat(findComment2.getId()).isEqualTo(comment3.getId());
        assertThat(findComment2.getMember().getId()).isEqualTo(member2.getId());
        assertThat(findComment2.getDeletedAt()).isNull();
//        assertThat(findComment2.getCanceledAt()).isNull();

        assertThat(findComment3.getId()).isEqualTo(comment2.getId());
        assertThat(findComment3.getMember().getId()).isEqualTo(member2.getId());
        assertThat(findComment3.getDeletedAt()).isNull();
//        assertThat(findComment3.getCanceledAt()).isNull();

        assertThat(findComment4.getId()).isEqualTo(comment1.getId());
        assertThat(findComment4.getMember().getId()).isEqualTo(member1.getId());
        assertThat(findComment4.getDeletedAt()).isNull();
//        assertThat(findComment4.getCanceledAt()).isNull();
    }
}
