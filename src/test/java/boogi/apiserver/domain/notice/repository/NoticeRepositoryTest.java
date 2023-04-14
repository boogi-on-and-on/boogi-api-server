package boogi.apiserver.domain.notice.repository;


import boogi.apiserver.builder.TestCommunity;
import boogi.apiserver.builder.TestMember;
import boogi.apiserver.builder.TestNotice;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.community.community.repository.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.repository.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.domain.user.repository.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.utils.RepositoryTest;
import boogi.apiserver.utils.TestTimeReflection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeRepositoryTest extends RepositoryTest {

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;


    @Test
    @DisplayName("최근 앱 공지사항 3개를 최신순으로 조회한다.")
    void getLatestAppNotice() {
        //given
        final Community community = TestCommunity.builder().build();
        communityRepository.save(community);

        List<Notice> appNotices = IntStream.range(0, 4)
                .mapToObj(i -> TestNotice.builder().build())
                .collect(Collectors.toList());
        appNotices.forEach(n -> TestTimeReflection.setCreatedAt(n, LocalDateTime.now()));
        Notice communityNotice = TestNotice.builder().community(community).build();
        noticeRepository.saveAll(appNotices);
        noticeRepository.save(communityNotice);

        cleanPersistenceContext();

        //when
        List<Notice> latestNotices = noticeRepository.getLatestAppNotice();

        //then
        assertThat(latestNotices).hasSize(3);
        List<Long> expectedAppNoticeIds = appNotices.subList(1, 4).stream().map(Notice::getId).collect(Collectors.toList());
        Collections.reverse(expectedAppNoticeIds);
        assertThat(latestNotices).extracting("id").containsExactlyElementsOf(expectedAppNoticeIds);
        assertThat(latestNotices).extracting("community").containsOnlyNulls();
    }

    @Test
    @DisplayName("앱 공지사항 전체를 최신순으로 조회한다.")
    void getAllAppNotices() {
        //given
        final Community community = TestCommunity.builder().build();
        communityRepository.save(community);

        List<Notice> appNotices = IntStream.range(0, 4)
                .mapToObj(i -> TestNotice.builder().build())
                .collect(Collectors.toList());
        appNotices.forEach(n -> TestTimeReflection.setCreatedAt(n, LocalDateTime.now()));
        Notice communityNotice = TestNotice.builder().community(community).build();
        noticeRepository.saveAll(appNotices);
        noticeRepository.save(communityNotice);

        cleanPersistenceContext();

        //when
        List<Notice> latestNotices = noticeRepository.getAllAppNotices();

        //then
        assertThat(latestNotices).hasSize(4);
        List<Long> expectedAppNoticeIds = appNotices.stream().map(Notice::getId).collect(Collectors.toList());
        Collections.reverse(expectedAppNoticeIds);
        assertThat(latestNotices).extracting("id").containsExactlyElementsOf(expectedAppNoticeIds);
        assertThat(latestNotices).extracting("community").containsOnlyNulls();
    }

    @Test
    @DisplayName("커뮤니티의 최근 공지사항을 최신순으로 3개 조회한다.")
    void getLatestCommunityNotice() {
        Community community = TestCommunity.builder().build();
        communityRepository.save(community);

        List<Notice> notices = IntStream.range(0, 4)
                .mapToObj(i -> TestNotice.builder()
                        .community(community)
                        .build())
                .collect(Collectors.toList());
        notices.forEach(n -> TestTimeReflection.setCreatedAt(n, LocalDateTime.now()));
        noticeRepository.saveAll(notices);

        cleanPersistenceContext();

        List<Notice> communityNotices = noticeRepository.getLatestNotice(community.getId());

        assertThat(communityNotices).hasSize(3);
        List<Long> expectedNoticeIds = notices.subList(1, 4).stream().map(Notice::getId).collect(Collectors.toList());
        Collections.reverse(expectedNoticeIds);
        assertThat(communityNotices).extracting("id").containsExactlyElementsOf(expectedNoticeIds);
        assertThat(communityNotices).extracting("community").extracting("id")
                .containsOnly(community.getId());
    }

    @Test
    @DisplayName("커뮤니티의 공지사항 전체를 최신순으로 조회한다.")
    void getAllCommunityNotices() {
        //given
        final User user = TestUser.builder().build();
        userRepository.save(user);

        final Member member = TestMember.builder().user(user).build();
        memberRepository.save(member);

        final Community community = TestCommunity.builder().build();
        communityRepository.save(community);

        final Notice notice1 = TestNotice.builder().build();
        final Notice notice2 = TestNotice.builder().build();
        final Notice notice3 = TestNotice.builder().build();
        final Notice notice4 = TestNotice.builder().build();
        final Notice notice5 = TestNotice.builder()
                .community(community)
                .member(member)
                .build();
        noticeRepository.saveAll(List.of(notice1, notice2, notice3, notice4, notice5));

        cleanPersistenceContext();

        //when
        List<Notice> notices = noticeRepository.getAllNotices(community.getId());

        assertThat(notices).hasSize(1);
        assertThat(notices).extracting("id").containsExactly(notice5.getId());
        assertThat(isLoaded(notices.get(0).getMember())).isTrue();
        assertThat(isLoaded(notices.get(0).getMember().getUser())).isTrue();
    }
}