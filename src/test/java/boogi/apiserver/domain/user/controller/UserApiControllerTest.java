package boogi.apiserver.domain.user.controller;

import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.user.application.UserQueryService;
import boogi.apiserver.domain.user.dto.UserDetailInfoResponse;
import boogi.apiserver.domain.user.dto.UserJoinedCommunity;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = UserApiController.class)
class UserApiControllerTest {

    @MockBean
    private MemberQueryService memberQueryService;

    @MockBean
    private UserQueryService userQueryService;

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
    void 유저_프로필_개인정보_조회() throws Exception {
        // given
        UserDetailInfoResponse response = UserDetailInfoResponse.builder()
                .id("1")
                .username("김선도")
                .tagNum("#0001")
                .introduce("반갑습니다")
                .department("컴퓨터공학부")
                .build();

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        given(userQueryService.getUserDetailInfo(anyLong())).willReturn(response);

        // when, then
        mvc.perform(
                        MockMvcRequestBuilders.get("/api/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.username").value("김선도"))
                .andExpect(jsonPath("$.tagNum").value("#0001"))
                .andExpect(jsonPath("$.introduce").value("반갑습니다"))
                .andExpect(jsonPath("$.department").value("컴퓨터공학부"))
                .andExpect(jsonPath("$.profileImageUrl").doesNotExist());
    }

    @Test
    void 유저_가입한_커뮤니티_조회() throws Exception {
        //given
        UserJoinedCommunity dto1 = UserJoinedCommunity.builder()
                .id("1")
                .name("커뮤니티1")
                .build();

        UserJoinedCommunity dto2 = UserJoinedCommunity.builder()
                .id("2")
                .name("커뮤니티2")
                .build();

        given(memberQueryService.getJoinedMemberInfo(anyLong()))
                .willReturn(List.of(dto1, dto2));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        // when, then
        mvc.perform(
                        MockMvcRequestBuilders.get("/api/users/communities/joined")
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.communities.length()").value(2))
                .andExpect(jsonPath("$.communities[0].name").isString())
                .andExpect(jsonPath("$.communities[0].id").isString());
    }

}
