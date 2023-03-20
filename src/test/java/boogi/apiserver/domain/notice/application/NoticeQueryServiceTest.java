package boogi.apiserver.domain.notice.application;

import boogi.apiserver.builder.TestMember;
import boogi.apiserver.builder.TestNotice;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.notice.dao.NoticeRepository;
import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.domain.notice.dto.dto.NoticeDetailDto;
import boogi.apiserver.domain.notice.dto.dto.NoticeDto;
import boogi.apiserver.domain.notice.dto.response.NoticeDetailResponse;
import boogi.apiserver.utils.TestTimeReflection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class NoticeQueryServiceTest {

    @Mock
    NoticeRepository noticeRepository;

    @Mock
    MemberQueryService memberQueryService;

    @InjectMocks
    NoticeQueryService noticeQueryService;


    @Test
    @DisplayName("앱 최근공지 조회")
    void lastCreatedNotice() {
        //given
        final Notice notice = TestNotice.builder()
                .id(1L)
                .title("공지사항의 제목입니다.")
                .build();
        TestTimeReflection.setCreatedAt(notice, LocalDateTime.now());

        given(noticeRepository.getLatestAppNotice())
                .willReturn(List.of(notice));

        //when
        List<NoticeDto> noticeDtos = noticeQueryService.getAppLatestNotice();

        //then
        NoticeDto dto = noticeDtos.get(0);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTitle()).isEqualTo("공지사항의 제목입니다.");
        assertThat(dto.getCreatedAt()).isEqualTo(notice.getCreatedAt().toString());
    }

    @Test
    @DisplayName("전체 앱 공지사항 조회")
    void allNotice() {
        //given
        final Notice notice = TestNotice.builder()
                .id(1L)
                .title("공지사항의 제목입니다.")
                .content("공지사항의 내용입니다.")
                .build();
        TestTimeReflection.setCreatedAt(notice, LocalDateTime.now());

        given(noticeRepository.getAllNotices())
                .willReturn(List.of(notice));

        //when
        final NoticeDetailResponse response = noticeQueryService.getAppNotice();

        //then

        assertThat(response.getManager()).isNull();

        final NoticeDetailDto dto = response.getNotices().get(0);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTitle()).isEqualTo("공지사항의 제목입니다.");
        assertThat(dto.getContent()).isEqualTo("공지사항의 내용입니다.");
        assertThat(dto.getCreatedAt()).isEqualTo(notice.getCreatedAt().toString());
    }

    @Test
    @DisplayName("커뮤니티 최근 공지사항 목록 조회")
    void latestCommunityNotice() {
        final Notice notice = TestNotice.builder()
                .id(1L)
                .title("공지사항 제목입니다.")
                .build();
        TestTimeReflection.setCreatedAt(notice, LocalDateTime.now());

        given(noticeRepository.getLatestNotice(anyLong()))
                .willReturn(List.of(notice));

        final List<NoticeDto> dtos = noticeQueryService.getCommunityLatestNotice(1L);

        assertThat(dtos.size()).isEqualTo(1);

        final NoticeDto dto = dtos.get(0);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTitle()).isEqualTo("공지사항 제목입니다.");
        assertThat(dto.getCreatedAt()).isEqualTo(notice.getCreatedAt().toString());
    }

    @Test
    @DisplayName("커뮤니티 전체 공지사항 목록 조회")
    void allCommunityNotice() {
        final Member member = TestMember.builder()
                .user(TestUser.builder().build())
                .memberType(MemberType.MANAGER)
                .build();

        given(memberQueryService.getMember(any(), anyLong()))
                .willReturn(member);

        final Notice notice = TestNotice.builder()
                .id(1L)
                .member(member)
                .title("공지사항 제목입니다.")
                .build();
        TestTimeReflection.setCreatedAt(notice, LocalDateTime.now());

        given(noticeRepository.getAllNotices(anyLong()))
                .willReturn(List.of(notice));

        final NoticeDetailResponse response = noticeQueryService.getCommunityNotice(1L, 1L);

        final List<? extends NoticeDetailDto> dtos = response.getNotices();
        assertThat(response.getManager()).isTrue();
        assertThat(dtos.size()).isEqualTo(1);

        final NoticeDto dto = dtos.get(0);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTitle()).isEqualTo("공지사항 제목입니다.");
        assertThat(dto.getCreatedAt()).isEqualTo(notice.getCreatedAt().toString());
    }
}