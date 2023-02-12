package boogi.apiserver.domain.comment.dao;

import boogi.apiserver.annotations.CustomDataJpaTest;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.utils.PersistenceUtil;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CustomDataJpaTest
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
        User user = TestEmptyEntityGenerator.User();
        userRepository.save(user);

        final Member member = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(member, "user", user);

        memberRepository.save(member);

        final Post post = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(post, "member", member);
        postRepository.save(post);

        final Comment comment1 = TestEmptyEntityGenerator.Comment();
        ReflectionTestUtils.setField(comment1, "member", member);
        ReflectionTestUtils.setField(comment1, "post", post);
        ReflectionTestUtils.setField(comment1, "createdAt", LocalDateTime.now().minusHours(3));

        final Comment comment2 = TestEmptyEntityGenerator.Comment();
        ReflectionTestUtils.setField(comment2, "member", member);
        ReflectionTestUtils.setField(comment2, "post", post);
        ReflectionTestUtils.setField(comment2, "createdAt", LocalDateTime.now().minusHours(2));

        final Comment comment3 = TestEmptyEntityGenerator.Comment();
        ReflectionTestUtils.setField(comment3, "member", member);
        ReflectionTestUtils.setField(comment3, "post", post);
        ReflectionTestUtils.setField(comment3, "createdAt", LocalDateTime.now().minusHours(1));


        commentRepository.saveAll(List.of(comment1, comment2, comment3));

        persistenceUtil.cleanPersistenceContext();

        Slice<Comment> commentPage1 = commentRepository.getUserCommentPage(PageRequest.of(0, 2), user.getId());
        Slice<Comment> commentPage2 = commentRepository.getUserCommentPage(PageRequest.of(1, 2), user.getId());

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
        Slice<Comment> commentPage = commentRepository.getUserCommentPage(PageRequest.of(0, 3), 2L);

        assertThat(commentPage.getContent().size()).isEqualTo(0);
        assertThat(commentPage.hasNext()).isFalse();
    }

    @Test
    @DisplayName("fetch join으로 댓글에 member도 같이 가져온다.")
    void testFindCommentWithMemberByCommentId() {
        final Member member = TestEmptyEntityGenerator.Member();

        memberRepository.save(member);

        final Post post = TestEmptyEntityGenerator.Post();
        postRepository.save(post);

        final Comment comment1 = TestEmptyEntityGenerator.Comment();
        ReflectionTestUtils.setField(comment1, "member", member);
        ReflectionTestUtils.setField(comment1, "post", post);
        commentRepository.save(comment1);


        final Comment comment2 = TestEmptyEntityGenerator.Comment();
        ReflectionTestUtils.setField(comment2, "member", member);
        ReflectionTestUtils.setField(comment2, "post", post);
        ReflectionTestUtils.setField(comment2, "parent", comment1);
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
        final Member member = TestEmptyEntityGenerator.Member();
        memberRepository.save(member);

        final Post post = TestEmptyEntityGenerator.Post();
        postRepository.save(post);

        final Comment comment1 = TestEmptyEntityGenerator.Comment();
        ReflectionTestUtils.setField(comment1, "member", member);
        ReflectionTestUtils.setField(comment1, "post", post);
        ReflectionTestUtils.setField(comment1, "child", false);
        ReflectionTestUtils.setField(comment1, "createdAt", LocalDateTime.now().minusHours(2));
        commentRepository.save(comment1);

        final Comment comment2 = TestEmptyEntityGenerator.Comment();
        ReflectionTestUtils.setField(comment2, "member", member);
        ReflectionTestUtils.setField(comment2, "post", post);
        ReflectionTestUtils.setField(comment2, "parent", comment1);
        ReflectionTestUtils.setField(comment2, "child", true);
        ReflectionTestUtils.setField(comment2, "createdAt", LocalDateTime.now().minusHours(1));
        commentRepository.save(comment2);

        final Comment comment3 = TestEmptyEntityGenerator.Comment();
        ReflectionTestUtils.setField(comment3, "member", member);
        ReflectionTestUtils.setField(comment3, "post", post);
        ReflectionTestUtils.setField(comment3, "child", false);
        ReflectionTestUtils.setField(comment3, "createdAt", LocalDateTime.now());

        comment3.setCreatedAt(LocalDateTime.now());
        commentRepository.save(comment3);

        persistenceUtil.cleanPersistenceContext();

        Pageable pageable = PageRequest.of(0, 2);
        Slice<Comment> parentCommentsPage = commentRepository
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
        final Member member = TestEmptyEntityGenerator.Member();
        memberRepository.save(member);

        final Post post = TestEmptyEntityGenerator.Post();
        postRepository.save(post);


        final Comment comment1 = TestEmptyEntityGenerator.Comment();
        ReflectionTestUtils.setField(comment1, "member", member);
        ReflectionTestUtils.setField(comment1, "post", post);
        ReflectionTestUtils.setField(comment1, "child", false);
        ReflectionTestUtils.setField(comment1, "createdAt", LocalDateTime.now().minusHours(2));

        commentRepository.save(comment1);

        final Comment comment2 = TestEmptyEntityGenerator.Comment();
        ReflectionTestUtils.setField(comment2, "member", member);
        ReflectionTestUtils.setField(comment2, "post", post);
        ReflectionTestUtils.setField(comment2, "child", true);
        ReflectionTestUtils.setField(comment2, "parent", comment1);
        ReflectionTestUtils.setField(comment2, "createdAt", LocalDateTime.now().minusHours(1));

        commentRepository.save(comment2);

        final Comment comment3 = TestEmptyEntityGenerator.Comment();
        ReflectionTestUtils.setField(comment3, "member", member);
        ReflectionTestUtils.setField(comment3, "post", post);
        ReflectionTestUtils.setField(comment3, "child", false);
        ReflectionTestUtils.setField(comment3, "createdAt", LocalDateTime.now());

        commentRepository.save(comment3);

        final Comment comment4 = TestEmptyEntityGenerator.Comment();
        ReflectionTestUtils.setField(comment4, "member", member);
        ReflectionTestUtils.setField(comment4, "post", post);
        ReflectionTestUtils.setField(comment4, "child", true);
        ReflectionTestUtils.setField(comment4, "parent", comment1);
        ReflectionTestUtils.setField(comment4, "createdAt", LocalDateTime.now());

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
        final Comment comment1 = TestEmptyEntityGenerator.Comment();
        final Comment comment2 = TestEmptyEntityGenerator.Comment();

        comment2.deleteComment();
        commentRepository.saveAll(List.of(comment1, comment2));

        persistenceUtil.cleanPersistenceContext();

        List<Comment> comments = commentRepository.findAll();

        assertThat(comments.size()).isEqualTo(1);
        assertThat(comments.get(0).getId()).isEqualTo(comment1.getId());
    }

    @Test
    @DisplayName("멤버가 작성한 삭제되지 않은 댓글들을 최근순으로 페이지네이션해서 가져온다.")
    void testGetUserCommentPageByMemberIds() {
        final Member member1 = TestEmptyEntityGenerator.Member();
        memberRepository.save(member1);

        final Member member2 = TestEmptyEntityGenerator.Member();
        memberRepository.save(member2);

        final Post post = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(post, "commentCount", 5);
        postRepository.save(post);

        final Comment comment1 = TestEmptyEntityGenerator.Comment();
        ReflectionTestUtils.setField(comment1, "post", post);
        ReflectionTestUtils.setField(comment1, "child", false);
        ReflectionTestUtils.setField(comment1, "member", member1);
        ReflectionTestUtils.setField(comment1, "createdAt", LocalDateTime.now().minusHours(3));

        commentRepository.save(comment1);

        final Comment comment2 = TestEmptyEntityGenerator.Comment();
        ReflectionTestUtils.setField(comment2, "post", post);
        ReflectionTestUtils.setField(comment2, "member", member2);
        ReflectionTestUtils.setField(comment2, "parent", comment1);
        ReflectionTestUtils.setField(comment2, "child", true);
        ReflectionTestUtils.setField(comment2, "createdAt", LocalDateTime.now().minusHours(2));

        commentRepository.save(comment2);

        final Comment comment3 = TestEmptyEntityGenerator.Comment();
        ReflectionTestUtils.setField(comment3, "post", post);
        ReflectionTestUtils.setField(comment3, "member", member2);
        ReflectionTestUtils.setField(comment3, "child", false);
        ReflectionTestUtils.setField(comment3, "createdAt", LocalDateTime.now().minusHours(1));

        commentRepository.save(comment3);

        final Comment comment4 = TestEmptyEntityGenerator.Comment();
        ReflectionTestUtils.setField(comment4, "post", post);
        ReflectionTestUtils.setField(comment4, "member", member1);
        ReflectionTestUtils.setField(comment4, "parent", comment1);
        ReflectionTestUtils.setField(comment4, "child", true);
        ReflectionTestUtils.setField(comment4, "createdAt", LocalDateTime.now());

        commentRepository.save(comment4);

        final Comment comment5 = TestEmptyEntityGenerator.Comment();
        ReflectionTestUtils.setField(comment5, "post", post);
        ReflectionTestUtils.setField(comment5, "member", member1);
        ReflectionTestUtils.setField(comment5, "child", false);
        ReflectionTestUtils.setField(comment5, "createdAt", LocalDateTime.now());

        comment5.deleteComment();
        commentRepository.save(comment5);

        persistenceUtil.cleanPersistenceContext();

        List<Long> memberIds = List.of(member1.getId(), member2.getId());
        Pageable pageable = PageRequest.of(0, 4);
        Slice<Comment> commentPage = commentRepository.getUserCommentPageByMemberIds(memberIds, pageable);
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

        assertThat(findComment2.getId()).isEqualTo(comment3.getId());
        assertThat(findComment2.getMember().getId()).isEqualTo(member2.getId());
        assertThat(findComment2.getDeletedAt()).isNull();

        assertThat(findComment3.getId()).isEqualTo(comment2.getId());
        assertThat(findComment3.getMember().getId()).isEqualTo(member2.getId());
        assertThat(findComment3.getDeletedAt()).isNull();

        assertThat(findComment4.getId()).isEqualTo(comment1.getId());
        assertThat(findComment4.getMember().getId()).isEqualTo(member1.getId());
        assertThat(findComment4.getDeletedAt()).isNull();
    }
}
