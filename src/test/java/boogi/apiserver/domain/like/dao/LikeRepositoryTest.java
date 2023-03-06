package boogi.apiserver.domain.like.dao;

import boogi.apiserver.annotations.CustomDataJpaTest;
import boogi.apiserver.builder.TestComment;
import boogi.apiserver.builder.TestLike;
import boogi.apiserver.builder.TestMember;
import boogi.apiserver.builder.TestPost;
import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.report.dao.ReportRepository;
import boogi.apiserver.utils.PersistenceUtil;
import boogi.apiserver.utils.TestTimeReflection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@CustomDataJpaTest
class LikeRepositoryTest {

    @Autowired
    LikeRepository likeRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    private EntityManager em;

    private PersistenceUtil persistenceUtil;
    @Autowired
    private ReportRepository reportRepository;

    @BeforeEach
    void init() {
        persistenceUtil = new PersistenceUtil(em);
    }


    @Test
    @DisplayName("글에 한 모든 좋아요들을 PostId로 조회한다.")
    void testFindPostLikesByPostId() {
        final Post post = TestPost.builder().build();
        postRepository.save(post);

        final Like like1 = TestLike.builder().post(post).build();
        final Like like2 = TestLike.builder().post(post).build();
        likeRepository.saveAll(List.of(like1, like2));

        persistenceUtil.cleanPersistenceContext();

        List<Like> postLikes = likeRepository.findPostLikesByPostId(post.getId());

        assertThat(postLikes.size()).isEqualTo(2);
        Like firstLike = postLikes.get(0);
        assertThat(firstLike.getId()).isEqualTo(like1.getId());
        assertThat(firstLike.getPost().getId()).isEqualTo(post.getId());

        Like secondLike = postLikes.get(1);
        assertThat(secondLike.getId()).isEqualTo(like2.getId());
        assertThat(secondLike.getPost().getId()).isEqualTo(post.getId());
    }

    @Test
    @DisplayName("PostId로 해당 글에 한 좋아요들을 모두 삭제한다.")
    void testDeleteAllPostLikeByPostId() {
        final Post post = TestPost.builder().build();
        postRepository.save(post);

        final Comment comment = TestComment.builder().build();
        commentRepository.save(comment);

        final Like like1 = TestLike.builder().post(post).build();
        final Like like2 = TestLike.builder().comment(comment).build();
        likeRepository.saveAll(List.of(like1, like2));

        persistenceUtil.cleanPersistenceContext();

        likeRepository.deleteAllPostLikeByPostId(post.getId());
        List<Like> likes = likeRepository.findAll();

        assertThat(likes.size()).isEqualTo(1);
        assertThat(likes.get(0).getId()).isEqualTo(like2.getId());
        assertThat(likes.get(0).getPost()).isNull();
    }

    @Test
    @DisplayName("CommentId로 해당 댓글에 한 좋아요들을 모두 삭제한다.")
    void testDeleteAllCommentLikeByCommentId() {
        final Post post = TestPost.builder().build();
        postRepository.save(post);

        final Comment comment = TestComment.builder().build();
        commentRepository.save(comment);

        final Like like1 = TestLike.builder().post(post).build();
        final Like like2 = TestLike.builder().comment(comment).build();
        likeRepository.saveAll(List.of(like1, like2));

        persistenceUtil.cleanPersistenceContext();

        likeRepository.deleteAllCommentLikeByCommentId(comment.getId());
        List<Like> likes = likeRepository.findAll();

        assertThat(likes.size()).isEqualTo(1);
        assertThat(likes.get(0).getId()).isEqualTo(like1.getId());
        assertThat(likes.get(0).getComment()).isNull();
    }

    @Test
    @DisplayName("PostId와 MemberId로 좋아요가 존재하는지 여부만 확인한다.")
    void testExistsLikeByPostIdAndMemberId() {
        final Post post = TestPost.builder().build();
        postRepository.save(post);

        final Member member = TestMember.builder().build();
        memberRepository.save(member);

        assertThat(likeRepository.existsLikeByPostIdAndMemberId(post.getId(), member.getId()))
                .isFalse();

        final Like like = TestLike.builder()
                .post(post)
                .member(member)
                .build();
        likeRepository.save(like);

        persistenceUtil.cleanPersistenceContext();

        assertThat(likeRepository.existsLikeByPostIdAndMemberId(post.getId(), member.getId()))
                .isTrue();
    }

    @Test
    @DisplayName("CommentId와 MemberId로 좋아요가 존재하는지 여부만 확인한다.")
    void testexistsLikeByCommentIdAndMemberId() {
        final Comment comment = TestComment.builder().build();
        commentRepository.save(comment);

        final Member member = TestMember.builder().build();
        memberRepository.save(member);

        assertThat(likeRepository.existsLikeByCommentIdAndMemberId(comment.getId(), member.getId()))
                .isFalse();

        final Like like = TestLike.builder()
                .comment(comment)
                .member(member)
                .build();
        likeRepository.save(like);

        persistenceUtil.cleanPersistenceContext();

        assertThat(likeRepository.existsLikeByCommentIdAndMemberId(comment.getId(), member.getId()))
                .isTrue();
    }

    @Test
    @DisplayName("LikeId로 Like를 fetch join으로 Member와 함께 조회한다.")
    void testFindLikeWithMemberById() {
        final Member member = TestMember.builder().build();
        memberRepository.save(member);

        final Like like = TestLike.builder().member(member).build();
        likeRepository.save(like);

        persistenceUtil.cleanPersistenceContext();

        Like findLike = likeRepository.findLikeWithMemberById(like.getId())
                .orElseGet(Assertions::fail);

        assertThat(findLike.getId()).isEqualTo(like.getId());
        assertThat(persistenceUtil.isLoaded(findLike.getMember())).isTrue();
    }

    @Test
    @DisplayName("해당 글에 한 좋아요를 PostId와 MemberId를 가지고 조회한다.")
    void testFindPostLikeByPostIdAndMemberId() {
        final Post post = TestPost.builder().build();
        postRepository.save(post);

        final Member member = TestMember.builder().build();
        memberRepository.save(member);

        final Like like = TestLike.builder()
                .post(post)
                .member(member)
                .build();
        likeRepository.save(like);

        persistenceUtil.cleanPersistenceContext();

        Like findLike = likeRepository.findPostLikeByPostIdAndMemberId(post.getId(), member.getId())
                .orElseGet(Assertions::fail);

        assertThat(findLike.getId()).isEqualTo(like.getId());
    }

    @Test
    @DisplayName("해당 댓글들에 한 좋아요들을 CommentId들과 MemberId를 가지고 조회한다.")
    void testFindCommentLikesByCommentIdsAndMemberId() {
        final Member member = TestMember.builder().build();
        memberRepository.save(member);

        final Comment comment1 = TestComment.builder().build();
        final Comment comment2 = TestComment.builder().build();
        commentRepository.saveAll(List.of(comment1, comment2));

        final Like like1 = TestLike.builder()
                .comment(comment1)
                .member(member)
                .build();
        final Like like2 = TestLike.builder()
                .comment(comment2)
                .member(member)
                .build();
        likeRepository.saveAll(List.of(like1, like2));

        persistenceUtil.cleanPersistenceContext();

        List<Long> commentIds = List.of(comment1, comment2).stream()
                .map(c -> c.getId())
                .collect(Collectors.toList());

        List<Like> commentLikes = likeRepository
                .findCommentLikesByCommentIdsAndMemberId(commentIds, member.getId());

        assertThat(commentLikes.size()).isEqualTo(2);
        assertThat(commentLikes.get(0).getId()).isEqualTo(like1.getId());
        assertThat(commentLikes.get(1).getId()).isEqualTo(like2.getId());
    }

    @Test
    @DisplayName("글에 한 좋아요들을 오래된 순으로 페이지네이션해서 fetch join으로 Member와 같이 PostId로 조회한다.")
    void testFindPostLikePageWithMemberByPostId() {
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

        persistenceUtil.cleanPersistenceContext();

        Pageable pageable = PageRequest.of(0, 2);

        Slice<Like> postLikePage = likeRepository.findPostLikePageWithMemberByPostId(post.getId(), pageable);

        assertThat(postLikePage.getContent().size()).isEqualTo(2);

        Like firstLike = postLikePage.getContent().get(0);
        assertThat(firstLike.getId()).isEqualTo(like1.getId());
        assertThat(firstLike.getPost().getId()).isEqualTo(post.getId());
        assertThat(firstLike.getMember().getId()).isEqualTo(member.getId());
        assertThat(persistenceUtil.isLoaded(firstLike.getMember())).isTrue();

        Like secondLike = postLikePage.getContent().get(1);
        assertThat(secondLike.getId()).isEqualTo(like2.getId());
        assertThat(secondLike.getPost().getId()).isEqualTo(post.getId());
        assertThat(secondLike.getMember().getId()).isEqualTo(member.getId());
        assertThat(persistenceUtil.isLoaded(secondLike.getMember())).isTrue();

        assertThat(postLikePage.getNumber()).isEqualTo(0);
        assertThat(postLikePage.hasNext()).isFalse();
    }

    @Test
    @DisplayName("댓글에 한 좋아요들을 오래된 순으로 페이지네이션해서 fetch join으로 Member와 같이 CommentId로 조회한다.")
    void testFindCommentLikePageWithMemberByCommentId() {
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

        persistenceUtil.cleanPersistenceContext();

        Pageable pageable = PageRequest.of(0, 2);

        Slice<Like> commentLikePage = likeRepository.findCommentLikePageWithMemberByCommentId(comment.getId(), pageable);

        assertThat(commentLikePage.getContent().size()).isEqualTo(2);

        Like firstLike = commentLikePage.getContent().get(0);
        assertThat(firstLike.getId()).isEqualTo(like1.getId());
        assertThat(firstLike.getComment().getId()).isEqualTo(comment.getId());
        assertThat(firstLike.getMember().getId()).isEqualTo(member.getId());
        assertThat(persistenceUtil.isLoaded(firstLike.getMember())).isTrue();

        Like secondLike = commentLikePage.getContent().get(1);
        assertThat(secondLike.getId()).isEqualTo(like2.getId());
        assertThat(secondLike.getComment().getId()).isEqualTo(comment.getId());
        assertThat(secondLike.getMember().getId()).isEqualTo(member.getId());
        assertThat(persistenceUtil.isLoaded(secondLike.getMember())).isTrue();

        assertThat(commentLikePage.getNumber()).isEqualTo(0);
        assertThat(commentLikePage.hasNext()).isFalse();
    }

    @Test
    @DisplayName("CommentId와 댓글에 한 좋아요 개수를 매핑한 Map을 좋아요 개수가 0인 경우를 제외하고 CommentId들로 조회한다.")
    void testGetCommentLikeCountsByCommentIds() {
        final Comment comment1 = TestComment.builder().build();
        final Comment comment2 = TestComment.builder().build();
        commentRepository.saveAll(List.of(comment1, comment2));

        final Like like1 = TestLike.builder().comment(comment1).build();
        final Like like2 = TestLike.builder().comment(comment1).build();
        likeRepository.saveAll(List.of(like1, like2));

        persistenceUtil.cleanPersistenceContext();

        List<Long> commentIds = List.of(comment1, comment2).stream()
                .map(c -> c.getId())
                .collect(Collectors.toList());

        Map<Long, Long> commentLikeCountMap = likeRepository
                .getCommentLikeCountsByCommentIds(commentIds);

        assertThat(commentLikeCountMap.size()).isEqualTo(1);
        assertThat(commentLikeCountMap.get(comment1.getId())).isEqualTo(2);
        assertThat(commentLikeCountMap.get(comment2.getId())).isNull();
    }
}