package boogi.apiserver.domain.member.api;


import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import boogi.apiserver.global.constant.HeaderConst;
import boogi.apiserver.global.constant.SessionInfoConst;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = MemberApiController.class)
class MemberApiControllerTest {

    @MockBean
    MemberQueryService memberQueryService;

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

    @Nested
    @DisplayName("맨션 멤버 검색 테스트")
    class MentionMemberTest {
        @Test
        @DisplayName("communityId가 없으면 client error")
        void communityIdIsEssential() throws Exception {

            MockHttpSession session = new MockHttpSession();
            session.setAttribute(SessionInfoConst.USER_ID, 1L);

            mvc.perform(
                            MockMvcRequestBuilders.get("/api/members/search/mention")
                                    .session(session)
                                    .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                    )
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("name은 없어도 성공")
        void nameIsOptional() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute(SessionInfoConst.USER_ID, 1L);

            UserBasicProfileDto dto = new UserBasicProfileDto(1L, null, "태그", "이름");

            PageImpl<UserBasicProfileDto> slice = new PageImpl(List.of(dto), Pageable.ofSize(1), 1);
            given(memberQueryService.getMentionSearchMembers(any(), anyLong(), any()))
                    .willReturn(slice);

            mvc.perform(
                            MockMvcRequestBuilders.get("/api/members/search/mention")
                                    .queryParam("communityId", "1")
                                    .queryParam("page", "0")
                                    .queryParam("size", "3")
                                    .session(session)
                                    .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.users[0].id").value(1L))
                    .andExpect(jsonPath("$.users[0].profileImageUrl").doesNotExist())
                    .andExpect(jsonPath("$.users[0].name").value("이름"))
                    .andExpect(jsonPath("$.users[0].tagNum").value("태그"));
        }
    }
}