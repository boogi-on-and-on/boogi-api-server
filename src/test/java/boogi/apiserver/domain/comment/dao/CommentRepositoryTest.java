package boogi.apiserver.domain.comment.dao;

import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager em;

    @Test
    void getUserCommentPage() {
        User user = User.builder()
                .build();
        userRepository.save(user);

        Member member = Member.builder()
                .user(user)
                .build();
        memberRepository.save(member);

        Comment comment1 = Comment.builder()
                .member(member)
                .build();
        comment1.setCreatedAt(LocalDateTime.now().minusHours(3));

        Comment comment2 = Comment.builder()
                .member(member)
                .build();
        comment2.setCreatedAt(LocalDateTime.now().minusHours(2));

        Comment comment3 = Comment.builder()
                .member(member)
                .build();
        comment3.setCreatedAt(LocalDateTime.now().minusHours(1));

        commentRepository.saveAll(List.of(comment1, comment2, comment3));

        em.flush();
        em.clear();

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
}
