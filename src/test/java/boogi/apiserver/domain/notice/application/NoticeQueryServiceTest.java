package boogi.apiserver.domain.notice.application;

import boogi.apiserver.domain.notice.dao.NoticeRepository;
import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.domain.notice.dto.response.NoticeDetailDto;
import boogi.apiserver.domain.notice.dto.response.NoticeDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class NoticeQueryServiceTest {

    @Mock
    NoticeRepository noticeRepository;

    @InjectMocks
    NoticeQueryService noticeQueryService;


    @Nested
    @DisplayName("앱 공지사항 테스트")
    class AppNoticeTest {

        @Test
        @DisplayName("최근공지 조회")
        void lastCreatedNotice() {
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
            assertThat(dto.getId()).isEqualTo(1L);
            assertThat(dto.getTitle()).isEqualTo("제목");
            assertThat(dto.getCreatedAt()).isEqualTo(notice.getCreatedAt().toString());
        }

        @Test
        @DisplayName("전체 공지사항 조회")
        void allNotice() {
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

            assertThat(dto.getId()).isEqualTo(1L);
            assertThat(dto.getTitle()).isEqualTo("제목");
            assertThat(dto.getContent()).isEqualTo("내용");
            assertThat(dto.getCreatedAt()).isEqualTo(notice.getCreatedAt().toString());
        }

    }
}