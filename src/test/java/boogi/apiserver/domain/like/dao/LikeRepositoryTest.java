package boogi.apiserver.domain.like.dao;

import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.utils.PersistenceUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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

    @BeforeAll
    void init() {
        persistenceUtil = new PersistenceUtil(em);
    }


    @Test
    @DisplayName("글에 한 모든 좋아요들을 PostId로 조회한다.")
    void testFindPostLikesByPostId() {
        Post post = Post.builder()
                .build();
        postRepository.save(post);

        Like like1 = Like.builder()
                .post(post)
                .build();
        Like like2 = Like.builder()
                .post(post)
                .build();
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
        Post post = Post.builder()
                .build();
        postRepository.save(post);

        Comment comment = Comment.builder()
                .build();
        commentRepository.save(comment);

        Like like1 = Like.builder()
                .post(post)
                .build();
        Like like2 = Like.builder()
                .comment(comment)
                .build();
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
        Post post = Post.builder()
                .build();
        postRepository.save(post);

        Comment comment = Comment.builder()
                .build();
        commentRepository.save(comment);

        Like like1 = Like.builder()
                .post(post)
                .build();
        Like like2 = Like.builder()
                .comment(comment)
                .build();
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
        Post post = Post.builder()
                .build();
        postRepository.save(post);

        Member member = Member.builder()
                .build();
        memberRepository.save(member);

        assertThat(likeRepository.existsLikeByPostIdAndMemberId(post.getId(), member.getId()))
                .isFalse();

        Like like = Like.builder()
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
        Comment comment = Comment.builder()
                .build();
        commentRepository.save(comment);

        Member member = Member.builder()
                .build();
        memberRepository.save(member);

        assertThat(likeRepository.existsLikeByCommentIdAndMemberId(comment.getId(), member.getId()))
                .isFalse();

        Like like = Like.builder()
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
        Member member = Member.builder()
                .build();
        memberRepository.save(member);

        Like like = Like.builder()
                .member(member)
                .build();
        likeRepository.save(like);

        persistenceUtil.cleanPersistenceContext();

        Like findLike = likeRepository.findLikeWithMemberById(like.getId()).orElse(null);
        if (findLike == null) {
            fail();
        }

        assertThat(findLike.getId()).isEqualTo(like.getId());
        assertThat(persistenceUtil.isLoaded(findLike.getMember())).isTrue();
    }

    @Test
    @DisplayName("해당 글에 한 좋아요를 PostId와 MemberId를 가지고 조회한다.")
    void testFindPostLikeByPostIdAndMemberId() {
        Post post = Post.builder()
                .build();
        postRepository.save(post);

        Member member = Member.builder()
                .build();
        memberRepository.save(member);

        Like like = Like.builder()
                .post(post)
                .member(member)
                .build();
        likeRepository.save(like);

        persistenceUtil.cleanPersistenceContext();

        Like findLike = likeRepository
                .findPostLikeByPostIdAndMemberId(post.getId(), member.getId())
                .orElse(null);
        if (findLike == null) {
            fail();
        }

        assertThat(findLike.getId()).isEqualTo(like.getId());
    }

    @Test
    @DisplayName("해당 댓글들에 한 좋아요들을 CommentId들과 MemberId를 가지고 조회한다.")
    void testFindCommentLikesByCommentIdsAndMemberId() {
        Member member = Member.builder()
                .build();
        memberRepository.save(member);

        Comment comment1 = Comment.builder()
                .build();
        Comment comment2 = Comment.builder()
                .build();
        List<Comment> comments = List.of(comment1, comment2);
        commentRepository.saveAll(comments);

        Like like1 = Like.builder()
                .comment(comment1)
                .member(member)
                .build();
        Like like2 = Like.builder()
                .comment(comment2)
                .member(member)
                .build();
        likeRepository.saveAll(List.of(like1, like2));

        persistenceUtil.cleanPersistenceContext();

        List<Long> commentIds = comments.stream()
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
        Post post = Post.builder()
                .build();
        postRepository.save(post);

        Member member = Member.builder()
                .build();
        memberRepository.save(member);

        Like like1 = Like.builder()
                .post(post)
                .member(member)
                .build();
        like1.setCreatedAt(LocalDateTime.now().minusHours(2));
        Like like2 = Like.builder()
                .post(post)
                .member(member)
                .build();
        like2.setCreatedAt(LocalDateTime.now().minusHours(1));
        Like like3 = Like.builder()
                .build();
        like3.setCreatedAt(LocalDateTime.now());
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
        Comment comment = Comment.builder()
                .build();
        commentRepository.save(comment);

        Member member = Member.builder()
                .build();
        memberRepository.save(member);

        Like like1 = Like.builder()
                .comment(comment)
                .member(member)
                .build();
        like1.setCreatedAt(LocalDateTime.now().minusHours(2));
        Like like2 = Like.builder()
                .comment(comment)
                .member(member)
                .build();
        like2.setCreatedAt(LocalDateTime.now().minusHours(1));
        Like like3 = Like.builder()
                .build();
        like3.setCreatedAt(LocalDateTime.now());
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
        Comment comment1 = Comment.builder()
                .build();
        Comment comment2 = Comment.builder()
                .build();
        List<Comment> comments = List.of(comment1, comment2);
        commentRepository.saveAll(comments);

        Like like1 = Like.builder()
                .comment(comment1)
                .build();
        Like like2 = Like.builder()
                .comment(comment1)
                .build();
        likeRepository.saveAll(List.of(like1, like2));

        persistenceUtil.cleanPersistenceContext();

        List<Long> commentIds = comments.stream()
                .map(c -> c.getId())
                .collect(Collectors.toList());

        Map<Long, Long> commentLikeCountMap = likeRepository
                .getCommentLikeCountsByCommentIds(commentIds);

        assertThat(commentLikeCountMap.size()).isEqualTo(1);
        assertThat(commentLikeCountMap.get(comment1.getId())).isEqualTo(2);
        assertThat(commentLikeCountMap.get(comment2.getId())).isNull();
    }
}