package boogi.apiserver.domain.notice.api;


import boogi.apiserver.builder.TestMember;
import boogi.apiserver.builder.TestNotice;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.application.MemberValidationService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.notice.application.NoticeQueryService;
import boogi.apiserver.domain.notice.application.NoticeCommandService;
import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.domain.notice.dto.dto.CommunityNoticeDetailDto;
import boogi.apiserver.domain.notice.dto.dto.NoticeDetailDto;
import boogi.apiserver.domain.notice.dto.request.NoticeCreateRequest;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.constant.HeaderConst;
import boogi.apiserver.global.constant.SessionInfoConst;
import boogi.apiserver.global.util.time.CustomDateTimeFormatter;
import boogi.apiserver.global.util.time.TimePattern;
import boogi.apiserver.utils.TestTimeReflection;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = NoticeApiController.class)
class NoticeApiControllerTest {

    @MockBean
    NoticeQueryService noticeQueryService;

    @MockBean
    MemberQueryService memberQueryService;

    @MockBean
    NoticeCommandService noticeCommandService;

    @MockBean
    MemberValidationService memberValidationService;

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

    @Nested
    @DisplayName("앱 공지사항 테스트")
    class AppNoticeTest {

        @Test
        @DisplayName("전체 공지 조회")
        void allAppNotice() throws Exception {
            final Notice notice = TestNotice.builder()
                    .id(1L)
                    .title("공지사항의 제목입니다.")
                    .content("공지사항의 내용입니다.")
                    .build();
            TestTimeReflection.setCreatedAt(notice, LocalDateTime.now());

            NoticeDetailDto noticeDetailDto = NoticeDetailDto.from(notice);

            MockHttpSession session = new MockHttpSession();
            session.setAttribute(SessionInfoConst.USER_ID, 1L);

            given(noticeQueryService.getAppNotice()).willReturn(List.of(noticeDetailDto));

            mvc.perform(
                            MockMvcRequestBuilders.get("/api/notices")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .session(session)
                                    .header(HeaderConst.AUTH_TOKEN, "AUTO_TOKEN")
                    ).andExpect(status().isOk())
                    .andExpect(jsonPath("$.notices[0].id").isNumber())
                    .andExpect(jsonPath("$.notices[0].title").isString())
                    .andExpect(jsonPath("$.notices[0].createdAt").value(CustomDateTimeFormatter.toString(notice.getCreatedAt(), TimePattern.BASIC_FORMAT)))
                    .andExpect(jsonPath("$.notices[0].content").isString());
        }

        @Test
        @DisplayName("공지사항 생성")
        void createNewAppNotice() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute(SessionInfoConst.USER_ID, 1L);

            NoticeCreateRequest request = new NoticeCreateRequest(1L, "내용", "제목");

            final Notice notice = TestNotice.builder().id(1L).build();

            given(noticeCommandService.createNotice(any(NoticeCreateRequest.class), anyLong()))
                    .willReturn(notice);

            mvc.perform(
                            MockMvcRequestBuilders.post("/api/notices")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(mapper.writeValueAsString(request))
                                    .session(session)
                                    .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                    ).andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L));
        }
    }

    @Nested
    @DisplayName("커뮤니티 공지사항 테스트")
    class CommunityNoticeTest {

        @Test
        @DisplayName("전체 공지 조회")
        void allCommunityNotice() throws Exception {
            final Member member = TestMember.builder().build();

            final Notice notice = TestNotice.builder()
                    .id(1L)
                    .member(member)
                    .title("공지사항의 제목입니다.")
                    .content("공지사항의 내용입니다.")
                    .build();
            TestTimeReflection.setCreatedAt(notice, LocalDateTime.now());

            final User user = TestUser.builder()
                    .id(3L)
                    .username("홍길동")
                    .tagNumber("#0001")
                    .build();

            CommunityNoticeDetailDto dto = CommunityNoticeDetailDto.of(notice, user);

            MockHttpSession session = new MockHttpSession();
            session.setAttribute(SessionInfoConst.USER_ID, 1L);

            given(noticeQueryService.getCommunityNotice(anyLong())).willReturn(List.of(dto));
            given(memberQueryService.hasAuth(any(), anyLong(), any()))
                    .willReturn(true);

            mvc.perform(
                            MockMvcRequestBuilders.get("/api/notices")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .session(session)
                                    .queryParam("communityId", "1")
                                    .header(HeaderConst.AUTH_TOKEN, "AUTO_TOKEN")
                    ).andExpect(status().isOk())
                    .andExpect(jsonPath("$.manager").value(true))
                    .andExpect(jsonPath("$.notices[0].id").value(1L))
                    .andExpect(jsonPath("$.notices[0].title").value("공지사항의 제목입니다."))
                    .andExpect(jsonPath("$.notices[0].content").value("공지사항의 내용입니다."))
                    .andExpect(jsonPath("$.notices[0].user.id").value(3L))
                    .andExpect(jsonPath("$.notices[0].user.tagNum").value("#0001"));
        }

    }
}