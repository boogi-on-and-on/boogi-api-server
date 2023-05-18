package boogi.apiserver.domain.notice.application;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.repository.CommunityRepository;
import boogi.apiserver.domain.member.application.MemberQuery;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.domain.notice.dto.dto.NoticeDetailDto;
import boogi.apiserver.domain.notice.dto.dto.NoticeDto;
import boogi.apiserver.domain.notice.dto.response.NoticeDetailResponse;
import boogi.apiserver.domain.notice.repository.NoticeRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.utils.fixture.CommunityFixture;
import boogi.apiserver.utils.fixture.MemberFixture;
import boogi.apiserver.utils.fixture.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static boogi.apiserver.utils.fixture.NoticeFixture.NOTICE1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class NoticeQueryTest {
    @InjectMocks
    NoticeQuery noticeQuery;

    @Mock
    NoticeRepository noticeRepository;

    @Mock
    CommunityRepository communityRepository;

    @Mock
    MemberQuery memberQuery;

    private Notice notice;

    @BeforeEach
    public void init() {
        final Community community = CommunityFixture.POCS.toCommunity(1L, null);
        final User user = UserFixture.SUNDO.toUser(2L);
        final Member member = MemberFixture.SUNDO_POCS.toMember(3L, user, community);
        this.notice = NOTICE1.toNotice(4L, community, member);
    }

    @Test
    @DisplayName("앱 최근공지 조회")
    void lastCreatedNotice() {
        //given
        given(noticeRepository.getLatestAppNotice())
                .willReturn(List.of(notice));

        //when
        List<NoticeDto> noticeDtos = noticeQuery.getAppLatestNotice();

        //then
        NoticeDto dto = noticeDtos.get(0);
        assertThat(dto.getId()).isEqualTo(notice.getId());
        assertThat(dto.getTitle()).isEqualTo(NOTICE1.title);
        assertThat(dto.getCreatedAt()).isEqualTo(NOTICE1.createdAt.toString());
    }

    @Test
    @DisplayName("전체 앱 공지사항 조회")
    void allNotice() {
        //given
        given(noticeRepository.getAllAppNotices())
                .willReturn(List.of(notice));

        //when
        final NoticeDetailResponse response = noticeQuery.getAppNotice();

        //then
        assertThat(response.getManager()).isNull();

        final NoticeDetailDto dto = response.getNotices().get(0);
        assertThat(dto.getId()).isEqualTo(notice.getId());
        assertThat(dto.getTitle()).isEqualTo(NOTICE1.title);
        assertThat(dto.getContent()).isEqualTo(NOTICE1.content);
        assertThat(dto.getCreatedAt()).isEqualTo(NOTICE1.createdAt.toString());
    }

    @Test
    @DisplayName("커뮤니티 최근 공지사항 목록 조회")
    void latestCommunityNotice() {
        //given
        given(noticeRepository.getLatestNotice(anyLong()))
                .willReturn(List.of(notice));

        final Long communityId = notice.getCommunity().getId();
        //when
        final List<NoticeDto> dtos = noticeQuery.getCommunityLatestNotice(communityId);

        //then
        verify(communityRepository, times(1)).findCommunityById(anyLong());

        assertThat(dtos.size()).isEqualTo(1);

        final NoticeDto dto = dtos.get(0);
        assertThat(dto.getId()).isEqualTo(notice.getId());
        assertThat(dto.getTitle()).isEqualTo(NOTICE1.title);
        assertThat(dto.getCreatedAt()).isEqualTo(NOTICE1.createdAt.toString());
    }

    @Test
    @DisplayName("커뮤니티 전체 공지사항 목록 조회")
    void allCommunityNotice() {
        //given
        Long userId = notice.getMember().getUser().getId();
        Long communityId = notice.getCommunity().getId();

        given(memberQuery.getMember(any(), anyLong()))
                .willReturn(notice.getMember());

        given(noticeRepository.getAllNotices(anyLong()))
                .willReturn(List.of(notice));

        //when
        final NoticeDetailResponse response = noticeQuery.getCommunityNotice(userId, communityId);

        verify(communityRepository, times(1)).findCommunityById(anyLong());

        final List<? extends NoticeDetailDto> dtos = response.getNotices();
        assertThat(response.getManager()).isTrue();
        assertThat(dtos).hasSize(1);

        final NoticeDto dto = dtos.get(0);
        assertThat(dto.getId()).isEqualTo(notice.getId());
        assertThat(dto.getTitle()).isEqualTo(NOTICE1.title);
        assertThat(dto.getCreatedAt()).isEqualTo(NOTICE1.createdAt.toString());
    }
}