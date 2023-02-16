package boogi.apiserver.domain.alarm.alarm.api;

import boogi.apiserver.domain.alarm.alarm.application.AlarmService;
import boogi.apiserver.domain.alarm.alarm.application.AlarmQueryService;
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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = AlarmApiController.class)
class AlarmApiControllerTest {

    @MockBean
    AlarmQueryService alarmQueryService;

    @MockBean
    AlarmService alarmService;

    MockMvc mvc;

    @Autowired
    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    WebApplicationContext ctx;

    @BeforeEach
    void setup() {
        mvc =
                MockMvcBuilders.webAppContextSetup(ctx)
                        .addFilter(new CharacterEncodingFilter("UTF-8", true))
                        .alwaysDo(print())
                        .build();
    }

    @Test
    void 알림_삭제() throws Exception {

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                MockMvcRequestBuilders.post("/api/alarms/1/delete")
                        .header(HeaderConst.AUTH_TOKEN, "TOKEN")
                        .session(session)
        ).andExpect(status().isOk());
    }
}