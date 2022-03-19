package boogi.apiserver.domain.notice.api;


import boogi.apiserver.domain.notice.application.NoticeQueryService;
import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.domain.notice.dto.NoticeDetailDto;
import boogi.apiserver.global.constant.HeaderConst;
import boogi.apiserver.global.constant.SessionInfoConst;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = NoticeApiController.class)
class NoticeApiControllerTest {

    @MockBean
    NoticeQueryService noticeQueryService;

    private MockMvc mvc;

    @Autowired
    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext ctx;

    @BeforeEach
    private void setup() {
        mvc =
                MockMvcBuilders.webAppContextSetup(ctx)
                        .addFilter(new CharacterEncodingFilter("UTF-8", true))
                        .alwaysDo(print())
                        .build();
    }

    @Test
    void 앱_공지_전체_조회() throws Exception {
        Notice notice = Notice.builder()
                .id(1L)
                .title("제목")
                .content("내용")
                .build();
        notice.setCreatedAt(LocalDateTime.now());
        NoticeDetailDto noticeDetailDto = NoticeDetailDto.of(notice);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        given(noticeQueryService.getAppNotice()).willReturn(List.of(noticeDetailDto));

        mvc.perform(
                        MockMvcRequestBuilders.get("/api/notices/list")
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTO_TOKEN")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.notices[0].id").isString())
                .andExpect(jsonPath("$.notices[0].title").isString())
                .andExpect(jsonPath("$.notices[0].createdAt").isString())
                .andExpect(jsonPath("$.notices[0].content").isString());
    }
}