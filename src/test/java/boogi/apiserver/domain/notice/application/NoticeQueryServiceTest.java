package boogi.apiserver.domain.notice.application;

import boogi.apiserver.domain.notice.dao.NoticeRepository;
import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.domain.notice.dto.NoticeDetailDto;
import boogi.apiserver.domain.notice.dto.NoticeDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class NoticeQueryServiceTest {

    @Mock
    NoticeRepository noticeRepository;

    @InjectMocks
    NoticeQueryService noticeQueryService;

    @Test
    void 앱_최근공지() {
        //given
        Notice notice = Notice.builder()
                .id(1L)
                .title("제목")
                .build();
        notice.setCreatedAt(LocalDateTime.now());

        given(noticeRepository.getLatestNotice())
                .willReturn(List.of(notice));

        //when
        List<NoticeDto> noticeDtos = noticeQueryService.getAppLatestNotice();

        //then
        NoticeDto dto = noticeDtos.get(0);
        assertThat(dto.getId()).isEqualTo("1");
        assertThat(dto.getTitle()).isEqualTo("제목");
        assertThat(dto.getCreatedAt()).isEqualTo(notice.getCreatedAt().toString());
    }

    @Test
    void 앱_공지_전체() {
        //given
        Notice notice = Notice.builder()
                .id(1L)
                .title("제목")
                .content("내용")
                .build();
        notice.setCreatedAt(LocalDateTime.now());

        given(noticeRepository.getAllNotices())
                .willReturn(List.of(notice));

        //when
        List<NoticeDetailDto> dtos = noticeQueryService.getAppNotice();

        //then
        NoticeDetailDto dto = dtos.get(0);

        assertThat(dto.getId()).isEqualTo("1");
        assertThat(dto.getTitle()).isEqualTo("제목");
        assertThat(dto.getContent()).isEqualTo("내용");
        assertThat(dto.getCreatedAt()).isEqualTo(notice.getCreatedAt().toString());
    }
}