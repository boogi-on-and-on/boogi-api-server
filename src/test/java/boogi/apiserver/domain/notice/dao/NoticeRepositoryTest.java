package boogi.apiserver.domain.notice.dao;


import boogi.apiserver.annotations.CustomDataJpaTest;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
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
        Community community = Community.builder().build();
        communityRepository.save(community);

        Notice notice1 = Notice.builder().build();
        Notice notice2 = Notice.builder().build();
        Notice notice3 = Notice.builder().build();
        Notice notice4 = Notice.builder().build();
        Notice notice5 = Notice.builder().community(community).build();
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
        Community community = Community.builder().build();
        communityRepository.save(community);

        Notice notice1 = Notice.builder().build();
        Notice notice2 = Notice.builder().build();
        Notice notice3 = Notice.builder().build();
        Notice notice4 = Notice.builder().build();
        Notice notice5 = Notice.builder().community(community).build();
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

        Member member = Member.builder().user(user).build();
        memberRepository.save(member);

        Community community = Community.builder().build();
        communityRepository.save(community);

        Notice notice1 = Notice.builder().build();
        Notice notice2 = Notice.builder().build();
        Notice notice3 = Notice.builder().build();
        Notice notice4 = Notice.builder().build();
        Notice notice5 = Notice.builder().member(member).community(community).build();
        noticeRepository.saveAll(List.of(notice1, notice2, notice3, notice4, notice5));

        //when
        List<Notice> notices = noticeRepository.getAllNotices(community.getId());

        assertThat(notices.size()).isEqualTo(1);

        Notice first = notices.get(0);
        assertThat(first).isEqualTo(notice5);
        assertThat(emf.getPersistenceUnitUtil().isLoaded(first.getMember().getUser())).isTrue();
    }
}