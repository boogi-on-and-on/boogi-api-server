package boogi.apiserver.domain.member.controller;


import boogi.apiserver.domain.community.community.dto.request.DelegateMemberRequest;
import boogi.apiserver.domain.member.application.MemberCommandService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.exception.*;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import boogi.apiserver.global.constant.HeaderConst;
import boogi.apiserver.global.util.PageableUtil;
import boogi.apiserver.utils.controller.TestControllerSetUp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = MemberApiController.class)
class MemberApiControllerTest extends TestControllerSetUp {

    @MockBean
    MemberQueryService memberQueryService;

    @MockBean
    MemberCommandService memberCommandService;

    @Test
    @DisplayName("맨션할 멤버 검색 후 페이지네이션해서 조회한다.")
    void searchMentionMemberSuccess() throws Exception {
        UserBasicProfileDto userDto = new UserBasicProfileDto(1L, null, "태그", "유저");
        Slice<UserBasicProfileDto> userPage = PageableUtil.getSlice(List.of(userDto), PageRequest.of(0, 1));

        given(memberQueryService.getMentionSearchMembers(any(), anyLong(), any()))
                .willReturn(userPage);

        final ResultActions result = mvc.perform(
                get("/api/members/search/mention")
                        .queryParam("communityId", "1")
                        .queryParam("page", "0")
                        .queryParam("size", "1")
                        .session(dummySession)
                        .header(HeaderConst.AUTH_TOKEN, TOKEN)
        );

        result
                .andExpect(status().isOk())
                .andDo(document("members/get-search-mention",
                        requestParameters(
                                parameterWithName("communityId").description("커뮤니티 ID"),
                                parameterWithName("name").description("검색할 이름").optional(),
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").description("페이지 사이즈")
                        ),
                        responseFields(
                                //todo: UserBasicProfile 공통화하기
                                fieldWithPath("users[].id").type(JsonFieldType.NUMBER)
                                        .description("유저의 ID"),
                                fieldWithPath("users[].profileImageUrl").type(JsonFieldType.STRING)
                                        .description("프로필이미지 경로").optional(),
                                fieldWithPath("users[].name").type(JsonFieldType.STRING)
                                        .description("유저의 이름"),
                                fieldWithPath("users[].tagNum").type(JsonFieldType.STRING)
                                        .description("태그번호"),

                                //todo: PagnationInfo extract하기
                                fieldWithPath("pageInfo").type(JsonFieldType.OBJECT)
                                        .description("페이지네이션 정보"),
                                fieldWithPath("pageInfo.nextPage").type(JsonFieldType.NUMBER)
                                        .description("다음 페이지 번호"),
                                fieldWithPath("pageInfo.hasNext").type(JsonFieldType.BOOLEAN)
                                        .description("true -> 다음 페이지가 있는 경우")
                        )
                ));
    }

    @Nested
    @DisplayName("멤버 차단")
    class BanMember {
        @Test
        @DisplayName("멤버 차단에 성공한다.")
        void banMemberSuccess() throws Exception {
            final ResultActions result = mvc.perform(
                    post("/api/members/{memberId}/ban", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            verify(memberCommandService, times(1)).banMember(anyLong(), anyLong());

            result
                    .andExpect(status().isOk())
                    .andDo(document("members/post-memberId-ban",
                            pathParameters(
                                    parameterWithName("memberId").description("멤버 ID")
                            )
                    ));
        }

        @Test
        @DisplayName("존재하지 않는 멤버 ID로 요청한 경우 MemberNotFoundException 발생")
        void notExistMemberFail() throws Exception {
            doThrow(new MemberNotFoundException())
                    .when(memberCommandService).banMember(anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    post("/api/members/{memberId}/ban", 9999L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("members/post-memberId-ban-MemberNotFoundException"));
        }

        @Test
        @DisplayName("해당 커뮤니티에 가입되지 않는 경우 NotJoinedMemberException 발생")
        void notMemberFail() throws Exception {
            doThrow(new NotJoinedMemberException())
                    .when(memberCommandService).banMember(anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    post("/api/members/{memberId}/ban", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("members/post-memberId-ban-NotJoinedMemberException"));
        }

        @Test
        @DisplayName("해당 커뮤니티의 관리자가 아닌 경우 NotOperatorException 발생")
        void notOperatorFail() throws Exception {
            doThrow(new NotOperatorException())
                    .when(memberCommandService).banMember(anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    post("/api/members/{memberId}/ban", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("members/post-memberId-ban-NotOperatorException"));
        }
    }

    @Nested
    @DisplayName("멤버 차단 해제")
    class ReleaseMember {
        @Test
        @DisplayName("멤버 차단 해제")
        void releaseMember() throws Exception {
            final ResultActions result = mvc.perform(
                    post("/api/members/{memberId}/release", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            verify(memberCommandService, times(1)).releaseMember(anyLong(), anyLong());

            result
                    .andExpect(status().isOk())
                    .andDo(document("members/post-memberId-release",
                            pathParameters(
                                    parameterWithName("memberId").description("멤버 ID")
                            )
                    ));
        }

        @Test
        @DisplayName("존재하지 않는 멤버 ID로 요청한 경우 MemberNotFoundException 발생")
        void notExistMemberFail() throws Exception {
            doThrow(new MemberNotFoundException())
                    .when(memberCommandService).releaseMember(anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    post("/api/members/{memberId}/release", 9999L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("members/post-memberId-release-MemberNotFoundException"));
        }

        @Test
        @DisplayName("해당 커뮤니티에 가입되지 않는 경우 NotJoinedMemberException 발생")
        void notMemberFail() throws Exception {
            doThrow(new NotJoinedMemberException())
                    .when(memberCommandService).releaseMember(anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    post("/api/members/{memberId}/release", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("members/post-memberId-release-NotJoinedMemberException"));
        }

        @Test
        @DisplayName("해당 커뮤니티의 매니저가 아닌 경우 NotManagerException 발생")
        void notManagerFail() throws Exception {
            doThrow(new NotManagerException())
                    .when(memberCommandService).releaseMember(anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    post("/api/members/{memberId}/release", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("members/post-memberId-release-NotManagerException"));
        }

        @Test
        @DisplayName("차단 해제할 멤버가 차단된 멤버가 아닌 경우 NotBannedMemberException 발생")
        void notBannedMemberFail() throws Exception {
            doThrow(new NotBannedMemberException())
                    .when(memberCommandService).releaseMember(anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    post("/api/members/{memberId}/release", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("members/post-memberId-release-NotBannedMemberException"));
        }
    }

    @Nested
    @DisplayName("멤버 권한 위임")
    class DelegateMember {
        @Test
        @DisplayName("멤버 권한 위임에 성공한다.")
        void delegateMemberSuccess() throws Exception {
            final DelegateMemberRequest request = new DelegateMemberRequest(MemberType.NORMAL);

            final ResultActions result = mvc.perform(
                    post("/api/members/{memberId}/delegate", 1L)
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            verify(memberCommandService, times(1))
                    .delegateMember(anyLong(), anyLong(), any(MemberType.class));

            result
                    .andExpect(status().isOk())
                    .andDo(document("members/post-memberId-delegate",
                            pathParameters(
                                    parameterWithName("memberId").description("멤버 ID")
                            ),
                            requestFields(
                                    fieldWithPath("type").type(JsonFieldType.STRING)
                                            .description("멤버 타입")
                                            .attributes(key("constraint").value("NORMAL, SUB_MANAGER, MANAGER 중 하나"))
                            )
                    ));
        }

        @Test
        @DisplayName("존재하지 않는 멤버 ID로 요청한 경우 MemberNotFoundException 발생")
        void notExistMemberFail() throws Exception {
            final DelegateMemberRequest request = new DelegateMemberRequest(MemberType.NORMAL);

            doThrow(new MemberNotFoundException())
                    .when(memberCommandService).delegateMember(anyLong(), anyLong(), any(MemberType.class));

            final ResultActions result = mvc.perform(
                    post("/api/members/{memberId}/delegate", 9999L)
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("members/post-memberId-delegate-MemberNotFoundException"));
        }

        @Test
        @DisplayName("해당 커뮤니티에 가입되지 않는 경우 NotJoinedMemberException 발생")
        void notMemberFail() throws Exception {
            final DelegateMemberRequest request = new DelegateMemberRequest(MemberType.NORMAL);

            doThrow(new NotJoinedMemberException())
                    .when(memberCommandService).delegateMember(anyLong(), anyLong(), any(MemberType.class));

            final ResultActions result = mvc.perform(
                    post("/api/members/{memberId}/delegate", 1L)
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("members/post-memberId-delegate-NotJoinedMemberException"));
        }

        @Test
        @DisplayName("해당 커뮤니티의 매니저가 아닌 경우 NotManagerException 발생")
        void notManagerFail() throws Exception {
            final DelegateMemberRequest request = new DelegateMemberRequest(MemberType.NORMAL);

            doThrow(new NotManagerException())
                    .when(memberCommandService).delegateMember(anyLong(), anyLong(), any(MemberType.class));

            final ResultActions result = mvc.perform(
                    post("/api/members/{memberId}/delegate", 1L)
                            .content(mapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("members/post-memberId-delegate-NotManagerException"));
        }
    }
}