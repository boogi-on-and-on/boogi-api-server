package boogi.apiserver.domain.user.controller;

import boogi.apiserver.domain.alarm.alarmconfig.application.AlarmConfigCommandService;
import boogi.apiserver.domain.alarm.alarmconfig.domain.AlarmConfig;
import boogi.apiserver.domain.alarm.alarmconfig.dto.request.AlarmConfigSettingRequest;
import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.dto.dto.JoinedCommunitiesDto;
import boogi.apiserver.domain.message.block.application.MessageBlockCommandService;
import boogi.apiserver.domain.message.block.application.MessageBlockQueryService;
import boogi.apiserver.domain.message.block.dto.dto.MessageBlockedUserDto;
import boogi.apiserver.domain.user.application.UserQueryService;
import boogi.apiserver.domain.user.dto.request.BlockMessageUsersRequest;
import boogi.apiserver.domain.user.dto.response.UserDetailInfoDto;
import boogi.apiserver.global.constant.HeaderConst;
import boogi.apiserver.global.constant.SessionInfoConst;
import boogi.apiserver.utils.controller.MockHttpSessionCreator;
import boogi.apiserver.utils.controller.TestControllerSetUp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = UserApiController.class)
class UserApiControllerTest extends TestControllerSetUp {


    @MockBean
    private UserQueryService userQueryService;

    @MockBean
    MessageBlockQueryService messageBlockQueryService;

    @MockBean
    MessageBlockCommandService messageBlockCommandService;

    @MockBean
    AlarmConfigCommandService alarmConfigCommandService;

    @MockBean
    CommunityQueryService communityQueryService;

    @Nested
    @DisplayName("토큰 유효성 테스트")
    class ValidateToken {

        @DisplayName("토큰이 유효한 경우")
        @Test
        void tokenIsValid() throws Exception {

            final ResultActions response = mvc.perform(RestDocumentationRequestBuilders
                    .post("/api/users/token/validation")
                    .session(MockHttpSessionCreator.dummySession())
                    .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN"));

            response.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isValid").value(true))
                    .andDo(document("users/post-token-validation",
                            requestHeaders(headerWithName(HeaderConst.AUTH_TOKEN)
                                    .description("유저 세션의 토큰")),

                            responseFields(
                                    fieldWithPath("isValid")
                                            .type(JsonFieldType.BOOLEAN)
                                            .description("유효한 토큰인 경우 true")
                            )
                    ));
        }

        @DisplayName("유효하지 않은 경우")
        @Test
        void tokenIsInvalid() throws Exception {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute(SessionInfoConst.USER_ID, 1L);

            mvc.perform(RestDocumentationRequestBuilders
                            .post("/api/users/token/validation")
                            .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                    ).andExpect(status().isOk())
                    .andExpect(jsonPath("$.isValid").value(false));
        }
    }

    @Test
    @DisplayName("유저 프로필 상세 조회")
    void userBasicInfo() throws Exception {
        // given
        UserDetailInfoDto dto = new UserDetailInfoDto(4L, null, "김선도", "#0001",
                "반갑습니다", "컴퓨터공학부");

        given(userQueryService.getUserDetailInfo(anyLong())).willReturn(dto);

        final ResultActions response = mvc.perform(RestDocumentationRequestBuilders
                .get("/api/users")
                .queryParam("userId", "4")
                .session(MockHttpSessionCreator.dummySession())
                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                .contentType(MediaType.APPLICATION_JSON)
        );

        response
                .andExpect(status().isOk())
                .andDo(document("users/get",
                        requestParameters(
                                parameterWithName("userId").description("유저의 ID")
                        ),
                        responseFields(
                                fieldWithPath("user")
                                        .type(JsonFieldType.OBJECT)
                                        .description("유저 정보"),

                                fieldWithPath("user.id")
                                        .type(JsonFieldType.NUMBER)
                                        .description("유저의 ID"),

                                fieldWithPath("user.profileImageUrl")
                                        .type(JsonFieldType.STRING)
                                        .description("프로필이미지 경로")
                                        .optional(),

                                fieldWithPath("user.name")
                                        .type(JsonFieldType.STRING)
                                        .description("유저의 이름"),

                                fieldWithPath("user.tagNum")
                                        .type(JsonFieldType.STRING)
                                        .description("태그번호"),

                                fieldWithPath("user.introduce")
                                        .type(JsonFieldType.STRING)
                                        .description("유저의 자기소개"),

                                fieldWithPath("user.department")
                                        .type(JsonFieldType.STRING)
                                        .description("유저의 학과"),

                                fieldWithPath("me")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("자신의 프로필을 조회하면 true")
                        )
                ));
    }

    @Test
    @DisplayName("유저가 가입한 커뮤니티 목록 조회")
    void userJoinedCommunities() throws Exception {
        final JoinedCommunitiesDto.PostInfo postInfo = new JoinedCommunitiesDto.PostInfo(
                1L, LocalDateTime.now(), List.of("태그1"), "글의 내용", "url", 1, 1
        );
        final JoinedCommunitiesDto.CommunityInfo communityInfo = new JoinedCommunitiesDto.CommunityInfo(1L, "커뮤니티 이름", postInfo);
        final JoinedCommunitiesDto dtos = new JoinedCommunitiesDto(List.of(communityInfo));

        given(communityQueryService.getJoinedCommunitiesWithLatestPost(any()))
                .willReturn(dtos);

        final ResultActions response = mvc.perform(RestDocumentationRequestBuilders
                .get("/api/users/communities/joined")
                .session(MockHttpSessionCreator.dummySession())
                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
        );

        response
                .andExpect(status().isOk())
                .andDo(document("users/get-communities-joined",
                        requestHeaders(
                                headerWithName(HeaderConst.AUTH_TOKEN)
                                        .description("유저 세션의 토큰")
                        ),

                        responseFields(
                                fieldWithPath("communities")
                                        .type(JsonFieldType.ARRAY)
                                        .description("커뮤니티 정보"),

                                fieldWithPath("communities[].id")
                                        .type(JsonFieldType.NUMBER)
                                        .description("커뮤니티 ID"),

                                fieldWithPath("communities[].name")
                                        .type(JsonFieldType.STRING)
                                        .description("커뮤니티 이름"),

                                fieldWithPath("communities[].post")
                                        .type(JsonFieldType.OBJECT)
                                        .description("커뮤니티의 최신 글 정보"),

                                fieldWithPath("communities[].post.id")
                                        .type(JsonFieldType.NUMBER)
                                        .description("게시글 ID"),

                                fieldWithPath("communities[].post.createdAt")
                                        .type(JsonFieldType.STRING)
                                        .description("게시글 생성일"),

                                fieldWithPath("communities[].post.hashtags")
                                        .type(JsonFieldType.ARRAY)
                                        .description("게시글 해시태그 목록")
                                        .optional(),

                                fieldWithPath("communities[].post.content")
                                        .type(JsonFieldType.STRING)
                                        .description("게시글의 내용"),

                                fieldWithPath("communities[].post.postMediaUrl")
                                        .type(JsonFieldType.STRING)
                                        .description("게시글의 경로")
                                        .optional(),

                                fieldWithPath("communities[].post.likeCount")
                                        .type(JsonFieldType.NUMBER)
                                        .description("게시글의 좋아요 개수"),

                                fieldWithPath("communities[].post.commentCount")
                                        .type(JsonFieldType.NUMBER)
                                        .description("게시글의 댓글 개수")
                        )
                ));
    }

    @Nested
    @DisplayName("유저 차단 테스트")
    class UserBlockTest {

        @Test
        @DisplayName("차단한 유저 목록 조회")
        void blockedUsers() throws Exception {
            MessageBlockedUserDto dto = new MessageBlockedUserDto(1L, "가나다#0001");

            given(messageBlockQueryService.getBlockedMembers(anyLong()))
                    .willReturn(List.of(dto));

            final ResultActions response = mvc.perform(
                    RestDocumentationRequestBuilders.get("/api/users/messages/blocked")
                            .session(MockHttpSessionCreator.dummySession())
                            .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
            );

            response
                    .andExpect(status().isOk())
                    .andDo(document("users/get-messages-blocked",
                            requestHeaders(
                                    headerWithName(HeaderConst.AUTH_TOKEN)
                                            .description("유저 세션의 토큰")),

                            responseFields(
                                    fieldWithPath("blocked")
                                            .type(JsonFieldType.ARRAY)
                                            .description("차단된 유저 목록"),

                                    fieldWithPath("blocked[].userId")
                                            .type(JsonFieldType.NUMBER)
                                            .description("유저의 ID"),

                                    fieldWithPath("blocked[].nameTag")
                                            .type(JsonFieldType.STRING)
                                            .description("유저의 태그번호")
                            )
                    ));
        }

        @Test
        @DisplayName("유저 차단 해제")
        void unblockUser() throws Exception {
            final ResultActions response = mvc.perform(RestDocumentationRequestBuilders
                    .post("/api/users/messages/unblock")
                    .contentType(MediaType.APPLICATION_JSON)
                    .session(MockHttpSessionCreator.dummySession())
                    .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                    .content(mapper.writeValueAsString(Map.of("blockedUserId", 2L)))
            );

            response
                    .andExpect(status().isOk())
                    .andDo(document("users/post-messages-unblock",
                            requestHeaders(
                                    headerWithName(HeaderConst.AUTH_TOKEN)
                                            .description("유저 세션의 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("blockedUserId")
                                            .type(JsonFieldType.NUMBER)
                                            .description("차단 해제할 유저 ID")
                            )
                    ));
        }

        @Test
        @DisplayName("차단할 유저를 입력하지 않아서 실패")
        void failBlockingUser() throws Exception {
            BlockMessageUsersRequest request = new BlockMessageUsersRequest(List.of());

            mvc.perform(RestDocumentationRequestBuilders
                            .post("/api/users/messages/block")
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(MockHttpSessionCreator.dummySession())
                            .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                            .content(mapper.writeValueAsString(request))
                    ).andExpect(status().is4xxClientError())
                    .andExpect(jsonPath("$.message").value("메시지 차단할 유저를 1명이상 선택해주세요"));
        }
    }


    @Nested
    @DisplayName("유저 알림 테스트")
    class UserAlarm {
        @Test
        @DisplayName("유저 알림설정 정보 조회")
        void alarmConfigDetail() throws Exception {
            final AlarmConfig config = AlarmConfig.builder()
                    .notice(true)
                    .joinRequest(true)
                    .comment(false)
                    .mention(true)
                    .build();

            given(alarmConfigCommandService.findOrElseCreateAlarmConfig(anyLong()))
                    .willReturn(config);

            final ResultActions response = mvc.perform(RestDocumentationRequestBuilders
                    .get("/api/users/config/notifications")
                    .session(MockHttpSessionCreator.dummySession())
                    .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
            );

            response.andExpect(status().isOk())
                    .andDo(document("users/get-config-notifications",
                            requestHeaders(
                                    headerWithName(HeaderConst.AUTH_TOKEN)
                                            .description("유저 세션의 토큰")
                            ),

                            responseFields(
                                    fieldWithPath("alarmInfo")
                                            .type(JsonFieldType.OBJECT)
                                            .description("알람정보"),

                                    fieldWithPath("alarmInfo.personal")
                                            .type(JsonFieldType.OBJECT)
                                            .description("개인 알람 정보"),

                                    fieldWithPath("alarmInfo.personal.message")
                                            .type(JsonFieldType.BOOLEAN)
                                            .description("메시지 알람"),

                                    fieldWithPath("alarmInfo.community")
                                            .type(JsonFieldType.OBJECT)
                                            .description("커뮤니티 알람 정보"),

                                    fieldWithPath("alarmInfo.community.notice")
                                            .type(JsonFieldType.BOOLEAN)
                                            .description("커뮤니티 공지사항 알람"),

                                    fieldWithPath("alarmInfo.community.join")
                                            .type(JsonFieldType.BOOLEAN)
                                            .description("커뮤니티 가입 알람"),

                                    fieldWithPath("alarmInfo.post")
                                            .type(JsonFieldType.OBJECT)
                                            .description("게시글 알람 정보"),

                                    fieldWithPath("alarmInfo.post.comment")
                                            .type(JsonFieldType.BOOLEAN)
                                            .description("게시글 댓글 알람"),

                                    fieldWithPath("alarmInfo.post.mention")
                                            .type(JsonFieldType.BOOLEAN)
                                            .description("게시글 맨션 알람")
                            )
                    ));
        }


        @Test
        @DisplayName("알람 정보 변경")
        void configureAlarm() throws Exception {
            final AlarmConfig config = AlarmConfig.builder()
                    .notice(true)
                    .joinRequest(true)
                    .comment(false)
                    .mention(true)
                    .build();

            given(alarmConfigCommandService.findOrElseCreateAlarmConfig(anyLong()))
                    .willReturn(config);

            final AlarmConfigSettingRequest request = new AlarmConfigSettingRequest(true, true, true, true, true);
            final ResultActions response = mvc.perform(RestDocumentationRequestBuilders
                    .get("/api/users/config/notifications")
                    .session(MockHttpSessionCreator.dummySession())
                    .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request))
            );

            response.andExpect(status().isOk())
                    .andDo(document("users/get-config-notifications",
                            requestHeaders(
                                    headerWithName(HeaderConst.AUTH_TOKEN)
                                            .description("유저 세션의 토큰")
                            ),

                            requestFields(
                                    fieldWithPath("message")
                                            .type(JsonFieldType.BOOLEAN)
                                            .description("true -> 메시지 알람 ON"),

                                    fieldWithPath("notice")
                                            .type(JsonFieldType.BOOLEAN)
                                            .description("true -> 공지 알람 ON"),

                                    fieldWithPath("join")
                                            .type(JsonFieldType.BOOLEAN)
                                            .description("true -> 커뮤니티 가입 알람 ON"),

                                    fieldWithPath("comment")
                                            .type(JsonFieldType.BOOLEAN)
                                            .description("true -> 댓글 알람 ON"),

                                    fieldWithPath("mention")
                                            .type(JsonFieldType.BOOLEAN)
                                            .description("true -> 맨션 알람 ON")
                            ),

                            responseFields(
                                    fieldWithPath("alarmInfo")
                                            .type(JsonFieldType.OBJECT)
                                            .description("알람정보"),

                                    fieldWithPath("alarmInfo.personal")
                                            .type(JsonFieldType.OBJECT)
                                            .description("개인 알람 정보"),

                                    fieldWithPath("alarmInfo.personal.message")
                                            .type(JsonFieldType.BOOLEAN)
                                            .description("메시지 알람"),

                                    fieldWithPath("alarmInfo.community")
                                            .type(JsonFieldType.OBJECT)
                                            .description("커뮤니티 알람 정보"),

                                    fieldWithPath("alarmInfo.community.notice")
                                            .type(JsonFieldType.BOOLEAN)
                                            .description("커뮤니티 공지사항 알람"),

                                    fieldWithPath("alarmInfo.community.join")
                                            .type(JsonFieldType.BOOLEAN)
                                            .description("커뮤니티 가입 알람"),

                                    fieldWithPath("alarmInfo.post")
                                            .type(JsonFieldType.OBJECT)
                                            .description("게시글 알람 정보"),

                                    fieldWithPath("alarmInfo.post.comment")
                                            .type(JsonFieldType.BOOLEAN)
                                            .description("게시글 댓글 알람"),

                                    fieldWithPath("alarmInfo.post.mention")
                                            .type(JsonFieldType.BOOLEAN)
                                            .description("게시글 맨션 알람")
                            )
                    ));
        }
    }
}
