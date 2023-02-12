package boogi.apiserver.domain.notice.dao;


import boogi.apiserver.annotations.CustomDataJpaTest;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CustomDataJpaTest
class NoticeRepositoryTest {

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private EntityManagerFactory emf;



    @Test
    @DisplayName("최근 공지사항 5개 조회")
    void getLatestNotice() {
        //given
        final Community community = TestEmptyEntityGenerator.Community();
        communityRepository.save(community);

        final Notice notice1 = TestEmptyEntityGenerator.Notice();
        final Notice notice2 = TestEmptyEntityGenerator.Notice();
        final Notice notice3 = TestEmptyEntityGenerator.Notice();
        final Notice notice4 = TestEmptyEntityGenerator.Notice();

        final Notice notice5 = TestEmptyEntityGenerator.Notice();
        ReflectionTestUtils.setField(notice5, "community", community);

        noticeRepository.saveAll(List.of(notice1, notice2, notice3, notice4, notice5));

        //when
        List<Notice> latestNotices = noticeRepository.getLatestNotice();

        //then
        assertThat(latestNotices.size()).isEqualTo(3);
        assertThat(latestNotices.stream().anyMatch(n -> n.getCommunity() == community)).isFalse();
    }

    @Test
    @DisplayName("전체 공지사항 조회")
    void getAllNotices() {
        //given
        final Community community = TestEmptyEntityGenerator.Community();
        communityRepository.save(community);

        final Notice notice1 = TestEmptyEntityGenerator.Notice();
        final Notice notice2 = TestEmptyEntityGenerator.Notice();
        final Notice notice3 = TestEmptyEntityGenerator.Notice();
        final Notice notice4 = TestEmptyEntityGenerator.Notice();

        final Notice notice5 = TestEmptyEntityGenerator.Notice();
        ReflectionTestUtils.setField(notice5, "community", community);

        noticeRepository.saveAll(List.of(notice1, notice2, notice3, notice4, notice5));

        //when
        List<Notice> latestNotices = noticeRepository.getAllNotices();

        //then
        assertThat(latestNotices.size()).isEqualTo(4);
        assertThat(latestNotices.stream().anyMatch(n -> n.getCommunity() == community)).isFalse();
    }

    @Test
    @DisplayName("커뮤니티의 전체 공지사항 조회")
    void getAllNotices_community() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        final Member member = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(member, "user", user);
        memberRepository.save(member);

        final Community community = TestEmptyEntityGenerator.Community();
        communityRepository.save(community);

        final Notice notice1 = TestEmptyEntityGenerator.Notice();
        final Notice notice2 = TestEmptyEntityGenerator.Notice();
        final Notice notice3 = TestEmptyEntityGenerator.Notice();
        final Notice notice4 = TestEmptyEntityGenerator.Notice();

        final Notice notice5 = TestEmptyEntityGenerator.Notice();
        ReflectionTestUtils.setField(notice5, "community", community);
        ReflectionTestUtils.setField(notice5, "member", member);

        noticeRepository.saveAll(List.of(notice1, notice2, notice3, notice4, notice5));

        //when
        List<Notice> notices = noticeRepository.getAllNotices(community.getId());

        assertThat(notices.size()).isEqualTo(1);

        Notice first = notices.get(0);
        assertThat(first).isEqualTo(notice5);
        assertThat(emf.getPersistenceUnitUtil().isLoaded(first.getMember().getUser())).isTrue();
    }
}