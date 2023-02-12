package boogi.apiserver.domain.user.controller;

import boogi.apiserver.domain.alarm.alarmconfig.application.AlarmConfigCoreService;
import boogi.apiserver.domain.alarm.alarmconfig.domain.AlarmConfig;
import boogi.apiserver.domain.community.community.application.CommunityCoreService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.message.block.application.MessageBlockCoreService;
import boogi.apiserver.domain.message.block.application.MessageBlockQueryService;
import boogi.apiserver.domain.message.block.dto.response.MessageBlockedUserDto;
import boogi.apiserver.domain.user.application.UserQueryService;
import boogi.apiserver.domain.user.dto.request.BlockMessageUsersRequest;
import boogi.apiserver.domain.user.dto.response.UserDetailInfoResponse;
import boogi.apiserver.domain.user.dto.response.UserJoinedCommunity;
import boogi.apiserver.global.constant.HeaderConst;
import boogi.apiserver.global.constant.SessionInfoConst;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.List;
import java.util.Map;

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

    @MockBean
    MessageBlockQueryService messageBlockQueryService;

    @MockBean
    MessageBlockCoreService messageBlockCoreService;

    @MockBean
    private AlarmConfigCoreService alarmConfigCoreService;

    @MockBean
    private CommunityCoreService communityCoreService;

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
    @DisplayName("토큰 유효성 테스트")
    class ValidateToken {

        @DisplayName("토큰이 유효한 경우")
        @Test
        void tokenIsValid() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute(SessionInfoConst.USER_ID, 1L);

            mvc.perform(
                            MockMvcRequestBuilders.post("/api/users/token/validation")
                                    .session(session)
                                    .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                    ).andExpect(status().isOk())
                    .andExpect(jsonPath("$.isValid").value(true));
        }

        @DisplayName("유효하지 않은 경우")
        @Test
        void tokenIsInvalid() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute(SessionInfoConst.USER_ID, 1L);

            mvc.perform(
                            MockMvcRequestBuilders.post("/api/users/token/validation")
                                    .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                    ).andExpect(status().isOk())
                    .andExpect(jsonPath("$.isValid").value(false));
        }
    }

    @Test
    @DisplayName("유저 프로필 개인정보 조회")
    void userBasicInfo() throws Exception {
        // given
        UserDetailInfoResponse response = UserDetailInfoResponse.builder()
                .id(4L)
                .name("김선도")
                .tagNum("#0001")
                .introduce("반갑습니다")
                .department("컴퓨터공학부")
                .build();

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        given(userQueryService.getUserDetailInfo(anyLong())).willReturn(response);

        // when, then
        mvc.perform(
                        MockMvcRequestBuilders.get("/api/users")
                                .queryParam("userId", "4")
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value("4"))
                .andExpect(jsonPath("$.user.name").value("김선도"))
                .andExpect(jsonPath("$.user.tagNum").value("#0001"))
                .andExpect(jsonPath("$.user.introduce").value("반갑습니다"))
                .andExpect(jsonPath("$.user.department").value("컴퓨터공학부"))
                .andExpect(jsonPath("$.user.profileImageUrl").doesNotExist());
    }

    @Test
    @Disabled
    void 유저_가입한_커뮤니티_조회() throws Exception {
        //given
        UserJoinedCommunity dto1 = UserJoinedCommunity.builder()
                .id(1L)
                .name("커뮤니티1")
                .build();

        UserJoinedCommunity dto2 = UserJoinedCommunity.builder()
                .id(2L)
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

//    @Test
//    void 유저가_가입한_커뮤니티_최신글_조회() throws Exception {
//        LatestPostOfUserJoinedCommunity post = LatestPostOfUserJoinedCommunity.builder()
//                .id(1L)
//                .name("커뮤니티1")
//                .post(LatestPostOfUserJoinedCommunity.PostDto.builder()
//                        .id(2L)
//                        .content("글")
//                        .likeCount(111)
//                        .commentCount(222)
//                        .createdAt(LocalDateTime.now().toString())
//                        .hashtags(List.of("해시테그"))
//                        .build())
//                .build();
//
//        given(postQueryService.getPostsOfUserJoinedCommunity(anyLong()))
//                .willReturn(List.of(post));
//
//        MockHttpSession session = new MockHttpSession();
//        session.setAttribute(SessionInfoConst.USER_ID, 1L);
//
//        // when, then
//        mvc.perform(
//                        MockMvcRequestBuilders.get("/api/users/communities/joined")
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .session(session)
//                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.communities[0].id").value("1"))
//                .andExpect(jsonPath("$.communities[0].name").value("커뮤니티1"))
//                .andExpect(jsonPath("$.communities[0].post.id").value("2"))
//                .andExpect(jsonPath("$.communities[0].post.content").value("글"))
//                .andExpect(jsonPath("$.communities[0].post.likeCount").value(111))
//                .andExpect(jsonPath("$.communities[0].post.commentCount").value(222))
//                .andExpect(jsonPath("$.communities[0].post.hashtags").isArray());
//
//    }

    @Nested
    @DisplayName("유저 차단 테스트")
    class UserBlockTest {

        @Test
        @DisplayName("차단한 유저 목록 조회")
        void blockUserList() throws Exception {
            MessageBlockedUserDto dto = MessageBlockedUserDto.builder()
                    .userId(1L)
                    .nameTag("가나다#0001")
                    .build();

            given(messageBlockQueryService.getBlockedMembers(anyLong()))
                    .willReturn(List.of(dto));

            MockHttpSession session = new MockHttpSession();
            session.setAttribute(SessionInfoConst.USER_ID, 1L);

            mvc.perform(
                            MockMvcRequestBuilders.get("/api/users/messages/blocked")
                                    .session(session)
                                    .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.blocked[0].userId").value(1L))
                    .andExpect(jsonPath("$.blocked[0].nameTag").value("가나다#0001"));
        }

        @Test
        @DisplayName("유저 차단 해제")
        void unblockUser() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute(SessionInfoConst.USER_ID, 1L);

            mvc.perform(
                            MockMvcRequestBuilders.post("/api/users/messages/unblock")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .session(session)
                                    .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                                    .content(mapper.writeValueAsString(Map.of("blockedUserId", 1L)))
                    )
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("유저 차단 실패")
        void failBlockingUser() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute(SessionInfoConst.USER_ID, 1L);

            BlockMessageUsersRequest request = BlockMessageUsersRequest.builder()
                    .blockUserIds(List.of())
                    .build();

            mvc.perform(
                            MockMvcRequestBuilders.post("/api/users/messages/block")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .session(session)
                                    .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                                    .content(mapper.writeValueAsString(request))
                    ).andExpect(status().is4xxClientError())
                    .andExpect(jsonPath("$.message").value("메시지 차단할 유저를 1명이상 선택해주세요"));
        }

    }

    @Test
    @DisplayName("유저 알림설정 정보 조회")
    void userAlarmList() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        final AlarmConfig config = TestEmptyEntityGenerator.AlarmConfig();
        ReflectionTestUtils.setField(config, "notice", true);
        ReflectionTestUtils.setField(config, "joinRequest", true);
        ReflectionTestUtils.setField(config, "comment", false);
        ReflectionTestUtils.setField(config, "mention", true);


        given(alarmConfigCoreService.findOrElseCreateAlarmConfig(anyLong()))
                .willReturn(config);

        mvc.perform(
                        MockMvcRequestBuilders.get("/api/users/config/notifications")
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.alarmInfo.personal.message").value(false))
                .andExpect(jsonPath("$.alarmInfo.community.notice").value(true))
                .andExpect(jsonPath("$.alarmInfo.community.join").value(true))
                .andExpect(jsonPath("$.alarmInfo.post.comment").value(false))
                .andExpect(jsonPath("$.alarmInfo.post.mention").value(true));

    }
}
