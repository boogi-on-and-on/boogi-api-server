package boogi.apiserver.domain.user.controller;

import boogi.apiserver.domain.alarm.alarmconfig.domain.AlarmConfig;
import boogi.apiserver.domain.alarm.alarmconfig.dto.request.AlarmConfigSettingRequest;
import boogi.apiserver.domain.community.community.dto.dto.JoinedCommunitiesDto;
import boogi.apiserver.domain.message.block.dto.dto.MessageBlockedUserDto;
import boogi.apiserver.domain.message.block.exception.NotBlockedUserException;
import boogi.apiserver.domain.user.dto.request.BlockMessageUsersRequest;
import boogi.apiserver.domain.user.dto.request.BlockedUserIdRequest;
import boogi.apiserver.domain.user.dto.response.UserDetailInfoDto;
import boogi.apiserver.domain.user.exception.UserNotFoundException;
import boogi.apiserver.global.constant.HeaderConst;
import boogi.apiserver.utils.controller.ControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserApiControllerTest extends ControllerTest {

    @Nested
    @DisplayName("토큰 유효성 테스트")
    class ValidateToken {

        @DisplayName("토큰이 유효한 경우")
        @Test
        void tokenIsValid() throws Exception {
            final ResultActions result = mvc.perform(
                    post("/api/users/token/validation")
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isValid").value(true))
                    .andDo(document("users/post-token-validation",
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
            mvc.perform(
                            post("/api/users/token/validation")
                                    .header(HeaderConst.AUTH_TOKEN, TOKEN)
                    ).andExpect(status().isOk())
                    .andExpect(jsonPath("$.isValid").value(false));
        }
    }

    @Nested
    @DisplayName("유저 프로필 상세 조회")
    class GetUserProfileInfo {
        @Test
        @DisplayName("유저 프로필 상세 조회에 성공한다.")
        void userProfileInfoSuccess() throws Exception {
            // given
            UserDetailInfoDto userDto = new UserDetailInfoDto(4L, null, "김선도", "#0001",
                    "반갑습니다", "컴퓨터공학부");

            given(userQuery.getUserDetailInfo(anyLong())).willReturn(userDto);

            final ResultActions result = mvc.perform(
                    get("/api/users")
                            .queryParam("userId", "4")
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            result
                    .andExpect(status().isOk())
                    .andDo(document("users/get",
                            requestParameters(
                                    parameterWithName("userId").description("유저의 ID")
                            ),
                            responseFields(
                                    fieldWithPath("user").type(JsonFieldType.OBJECT)
                                            .description("유저 정보"),
                                    fieldWithPath("user.id").type(JsonFieldType.NUMBER)
                                            .description("유저의 ID"),
                                    fieldWithPath("user.profileImageUrl").type(JsonFieldType.STRING)
                                            .description("프로필이미지 경로").optional(),
                                    fieldWithPath("user.name").type(JsonFieldType.STRING)
                                            .description("유저의 이름"),
                                    fieldWithPath("user.tagNum").type(JsonFieldType.STRING)
                                            .description("태그번호"),
                                    fieldWithPath("user.introduce").type(JsonFieldType.STRING)
                                            .description("유저의 자기소개"),
                                    fieldWithPath("user.department").type(JsonFieldType.STRING)
                                            .description("유저의 학과"),
                                    fieldWithPath("me").type(JsonFieldType.BOOLEAN)
                                            .description("자신의 프로필을 조회하면 true")
                            )
                    ));
        }

        @Test
        @DisplayName("존재하지 않는 유저 ID로 요청할 경우 UserNotFoundException 발생")
        void notExistUserFail() throws Exception {
            doThrow(new UserNotFoundException())
                    .when(userQuery).getUserDetailInfo(anyLong());

            final ResultActions result = mvc.perform(
                    get("/api/users")
                            .queryParam("userId", "9999")
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("users/get-UserNotFoundException"));
        }
    }

    @Test
    @DisplayName("유저가 가입한 커뮤니티 목록 조회")
    void getUserJoinedCommunitiesSuccess() throws Exception {
        final JoinedCommunitiesDto.PostInfo postInfo = new JoinedCommunitiesDto.PostInfo(1L, LocalDateTime.now(),
                List.of("해시태그"), "글의 내용", "url", 1, 1);
        final JoinedCommunitiesDto.CommunityInfo communityInfo = new JoinedCommunitiesDto.CommunityInfo(1L,
                "커뮤니티 이름", postInfo);
        final JoinedCommunitiesDto communityDto = new JoinedCommunitiesDto(List.of(communityInfo));

        given(communityQuery.getJoinedCommunitiesWithLatestPost(any()))
                .willReturn(communityDto);

        final ResultActions result = mvc.perform(
                get("/api/users/communities/joined")
                        .session(dummySession)
                        .header(HeaderConst.AUTH_TOKEN, TOKEN)
        );

        result
                .andExpect(status().isOk())
                .andDo(document("users/get-communities-joined",
                        responseFields(
                                fieldWithPath("communities").type(JsonFieldType.ARRAY)
                                        .description("커뮤니티 정보"),
                                fieldWithPath("communities[].id").type(JsonFieldType.NUMBER)
                                        .description("커뮤니티 ID"),
                                fieldWithPath("communities[].name").type(JsonFieldType.STRING)
                                        .description("커뮤니티 이름"),
                                fieldWithPath("communities[].post").type(JsonFieldType.OBJECT)
                                        .description("커뮤니티의 최신 글 정보"),
                                fieldWithPath("communities[].post.id").type(JsonFieldType.NUMBER)
                                        .description("게시글 ID"),
                                fieldWithPath("communities[].post.createdAt").type(JsonFieldType.STRING)
                                        .description("게시글 생성일"),
                                fieldWithPath("communities[].post.hashtags").type(JsonFieldType.ARRAY)
                                        .description("게시글 해시태그 목록").optional(),
                                fieldWithPath("communities[].post.content").type(JsonFieldType.STRING)
                                        .description("게시글의 내용"),
                                fieldWithPath("communities[].post.postMediaUrl").type(JsonFieldType.STRING)
                                        .description("게시글의 경로").optional(),
                                fieldWithPath("communities[].post.likeCount").type(JsonFieldType.NUMBER)
                                        .description("게시글의 좋아요 개수"),
                                fieldWithPath("communities[].post.commentCount").type(JsonFieldType.NUMBER)
                                        .description("게시글의 댓글 개수")
                        )
                ));
    }

    @Test
    @DisplayName("차단한 유저 목록 조회")
    void getBlockedUsersSuccess() throws Exception {
        MessageBlockedUserDto blockedUserDto = new MessageBlockedUserDto(1L, "가나다#0001");

        given(messageBlockQuery.getBlockedUsers(anyLong()))
                .willReturn(List.of(blockedUserDto));

        final ResultActions result = mvc.perform(
                get("/api/users/messages/blocked")
                        .session(dummySession)
                        .header(HeaderConst.AUTH_TOKEN, TOKEN)
        );

        result
                .andExpect(status().isOk())
                .andDo(document("users/get-messages-blocked",
                        responseFields(
                                fieldWithPath("blocked").type(JsonFieldType.ARRAY)
                                        .description("차단된 유저 목록"),
                                fieldWithPath("blocked[].userId").type(JsonFieldType.NUMBER)
                                        .description("유저의 ID"),
                                fieldWithPath("blocked[].nameTag").type(JsonFieldType.STRING)
                                        .description("유저의 태그번호")
                        )
                ));
    }

    @Nested
    @DisplayName("유저 쪽지 차단 해제")
    class UnblockUserTest {
        @Test
        @DisplayName("유저 차단 해제에 성공한다.")
        void unblockUserSuccess() throws Exception {
            final long UNBLOCK_USER_ID = 2L;
            BlockedUserIdRequest request = new BlockedUserIdRequest(UNBLOCK_USER_ID);

            final ResultActions result = mvc.perform(
                    post("/api/users/messages/unblock")
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .content(mapper.writeValueAsString(request))
            );

            result
                    .andExpect(status().isOk())
                    .andDo(document("users/post-messages-unblock",
                            requestFields(
                                    fieldWithPath("blockedUserId").type(JsonFieldType.NUMBER)
                                            .description("차단 해제할 유저 ID")
                            )
                    ));
        }

        @Test
        @DisplayName("해당 유저에 대해 쪽지 차단되어 있지 않는 경우 NotBlockedUserException 발생")
        void notBlockedUserFail() throws Exception {
            BlockedUserIdRequest request = new BlockedUserIdRequest(1L);

            doThrow(new NotBlockedUserException())
                    .when(messageBlockCommand).unblockUser(anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    post("/api/users/messages/unblock")
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .content(mapper.writeValueAsString(request))
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("users/post-messages-unblock-NotBlockedUserException"));
        }
    }

    @Nested
    @DisplayName("유저 메시지 차단")
    class BlockUsers {
        @Test
        @DisplayName("유저 메시지 차단에 성공한다.")
        void blockUsersSuccess() throws Exception {
            BlockMessageUsersRequest request = new BlockMessageUsersRequest(List.of(1L));

            ResultActions result = mvc.perform(
                    post("/api/users/messages/block")
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .content(mapper.writeValueAsString(request))
            );

            verify(messageBlockCommand, times(1)).blockUsers(anyLong(), anyList());

            result
                    .andExpect(status().isOk())
                    .andDo(document("users/post-messages-block",
                            requestFields(
                                    fieldWithPath("blockUserIds").type(JsonFieldType.ARRAY)
                                            .description("메시지 차단할 유저 ID들")
                                            .attributes(key("constraint").value("최소 1개 이상의 ID 입력"))
                            )
                    ));
        }
    }

    @Test
    @DisplayName("유저 알림설정 정보 조회에 성공한다.")
    void getAlarmConfigDetailSuccess() throws Exception {
        final AlarmConfig config = AlarmConfig.builder()
                .notice(true)
                .joinRequest(true)
                .comment(false)
                .mention(true)
                .build();

        given(alarmConfigCommand.findOrElseCreateAlarmConfig(anyLong()))
                .willReturn(config);

        final ResultActions response = mvc.perform(
                get("/api/users/config/notifications")
                        .session(dummySession)
                        .header(HeaderConst.AUTH_TOKEN, TOKEN)
        );

        response.andExpect(status().isOk())
                .andDo(document("users/get-config-notifications",
                        responseFields(
                                fieldWithPath("alarmInfo").type(JsonFieldType.OBJECT)
                                        .description("알람정보"),
                                fieldWithPath("alarmInfo.personal").type(JsonFieldType.OBJECT)
                                        .description("개인 알람 정보"),
                                fieldWithPath("alarmInfo.personal.message").type(JsonFieldType.BOOLEAN)
                                        .description("메시지 알람"),
                                fieldWithPath("alarmInfo.community").type(JsonFieldType.OBJECT)
                                        .description("커뮤니티 알람 정보"),
                                fieldWithPath("alarmInfo.community.notice").type(JsonFieldType.BOOLEAN)
                                        .description("커뮤니티 공지사항 알람"),
                                fieldWithPath("alarmInfo.community.join").type(JsonFieldType.BOOLEAN)
                                        .description("커뮤니티 가입 알람"),
                                fieldWithPath("alarmInfo.post").type(JsonFieldType.OBJECT)
                                        .description("게시글 알람 정보"),
                                fieldWithPath("alarmInfo.post.comment").type(JsonFieldType.BOOLEAN)
                                        .description("게시글 댓글 알람"),
                                fieldWithPath("alarmInfo.post.mention").type(JsonFieldType.BOOLEAN)
                                        .description("게시글 맨션 알람")
                        )
                ));
    }

    @Test
    @DisplayName("알람 정보 변경에 성공한다.")
    void configureAlarmSuccess() throws Exception {
        final AlarmConfig config = AlarmConfig.builder()
                .notice(true)
                .joinRequest(true)
                .comment(false)
                .mention(true)
                .build();

        final AlarmConfigSettingRequest request =
                new AlarmConfigSettingRequest(true, true, true, true, true);

        given(alarmConfigCommand.configureAlarm(anyLong(), any(AlarmConfigSettingRequest.class)))
                .willReturn(config);

        final ResultActions result = mvc.perform(
                post("/api/users/config/notifications")
                        .session(dummySession)
                        .header(HeaderConst.AUTH_TOKEN, TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        );

        result.andExpect(status().isOk())
                .andDo(document("users/post-config-notifications",
                        requestFields(
                                fieldWithPath("message").type(JsonFieldType.BOOLEAN)
                                        .description("true -> 메시지 알람 ON"),
                                fieldWithPath("notice").type(JsonFieldType.BOOLEAN)
                                        .description("true -> 공지 알람 ON"),
                                fieldWithPath("join").type(JsonFieldType.BOOLEAN)
                                        .description("true -> 커뮤니티 가입 알람 ON"),
                                fieldWithPath("comment").type(JsonFieldType.BOOLEAN)
                                        .description("true -> 댓글 알람 ON"),
                                fieldWithPath("mention").type(JsonFieldType.BOOLEAN)
                                        .description("true -> 맨션 알람 ON")
                        ),
                        responseFields(
                                fieldWithPath("alarmInfo").type(JsonFieldType.OBJECT)
                                        .description("알람정보"),
                                fieldWithPath("alarmInfo.personal").type(JsonFieldType.OBJECT)
                                        .description("개인 알람 정보"),
                                fieldWithPath("alarmInfo.personal.message").type(JsonFieldType.BOOLEAN)
                                        .description("메시지 알람"),
                                fieldWithPath("alarmInfo.community").type(JsonFieldType.OBJECT)
                                        .description("커뮤니티 알람 정보"),
                                fieldWithPath("alarmInfo.community.notice").type(JsonFieldType.BOOLEAN)
                                        .description("커뮤니티 공지사항 알람"),
                                fieldWithPath("alarmInfo.community.join").type(JsonFieldType.BOOLEAN)
                                        .description("커뮤니티 가입 알람"),
                                fieldWithPath("alarmInfo.post").type(JsonFieldType.OBJECT)
                                        .description("게시글 알람 정보"),
                                fieldWithPath("alarmInfo.post.comment").type(JsonFieldType.BOOLEAN)
                                        .description("게시글 댓글 알람"),
                                fieldWithPath("alarmInfo.post.mention").type(JsonFieldType.BOOLEAN)
                                        .description("게시글 맨션 알람")
                        )
                ));
    }
}