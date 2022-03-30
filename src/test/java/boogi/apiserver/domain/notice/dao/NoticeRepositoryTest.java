package boogi.apiserver.domain.notice.dao;


import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.notice.domain.Notice;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class NoticeRepositoryTest {

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private EntityManager em;


    @Test
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

}