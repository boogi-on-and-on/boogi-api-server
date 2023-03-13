package boogi.apiserver.domain.member.api;


import boogi.apiserver.domain.community.community.dto.request.DelegateMemberRequest;
import boogi.apiserver.domain.member.application.MemberCommandService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import boogi.apiserver.global.constant.HeaderConst;
import boogi.apiserver.utils.controller.MockHttpSessionCreator;
import boogi.apiserver.utils.controller.TestControllerSetUp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = MemberApiController.class)
class MemberApiControllerTest extends TestControllerSetUp {

    @MockBean
    MemberQueryService memberQueryService;

    @MockBean
    MemberCommandService memberCommandService;

    @Test
    @DisplayName("맨션할 멤버 검색하기")
    void searchMentionMember() throws Exception {
        UserBasicProfileDto dto = new UserBasicProfileDto(1L, null, "태그", "이름");

        PageImpl<UserBasicProfileDto> slice = new PageImpl(List.of(dto), Pageable.ofSize(1), 1);
        given(memberQueryService.getMentionSearchMembers(any(), anyLong(), any()))
                .willReturn(slice);

        final ResultActions response = mvc.perform(
                MockMvcRequestBuilders.get("/api/members/search/mention")
                        .queryParam("communityId", "1")
                        .queryParam("page", "0")
                        .queryParam("size", "3")
                        .session(MockHttpSessionCreator.dummySession())
                        .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
        );

        response
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
                                fieldWithPath("users[].id")
                                        .type(JsonFieldType.NUMBER)
                                        .description("유저의 ID"),

                                fieldWithPath("users[].profileImageUrl")
                                        .type(JsonFieldType.STRING)
                                        .description("프로필이미지 경로")
                                        .optional(),

                                fieldWithPath("users[].name")
                                        .type(JsonFieldType.STRING)
                                        .description("유저의 이름"),

                                fieldWithPath("users[].tagNum")
                                        .type(JsonFieldType.STRING)
                                        .description("태그번호"),

                                //todo: PagnationInfo extract하기
                                fieldWithPath("pageInfo")
                                        .type(JsonFieldType.OBJECT)
                                        .description("페이지네이션 정보"),

                                fieldWithPath("pageInfo.nextPage")
                                        .type(JsonFieldType.NUMBER)
                                        .description("다음 페이지 번호"),

                                fieldWithPath("pageInfo.hasNext")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("true -> 다음 페이지가 있는 경우")
                        )
                ));

    }

    @Test
    @DisplayName("멤버 차단")
    void banMember() throws Exception {
        final ResultActions response = mvc.perform(RestDocumentationRequestBuilders
                .post("/api/members/{memberId}/ban", 2L)
                .session(MockHttpSessionCreator.dummySession())
                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
        );

        then(memberCommandService).should(times(1))
                .banMember(1L, 2L);

        response
                .andExpect(status().isOk())
                .andDo(document("members/post-memberId-ban",
                        pathParameters(
                                parameterWithName("memberId").description("멤버 ID")
                        )
                ));
    }

    @Test
    @DisplayName("멤버 차단 해제")
    void releaseMember() throws Exception {
        final ResultActions response = mvc.perform(RestDocumentationRequestBuilders
                .post("/api/members/{memberId}/release", 2L)
                .session(MockHttpSessionCreator.dummySession())
                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
        );

        then(memberCommandService).should(times(1))
                .releaseMember(1L, 2L);

        response
                .andExpect(status().isOk())
                .andDo(document("members/post-memberId-release",
                        pathParameters(
                                parameterWithName("memberId").description("멤버 ID")
                        )
                ));
    }

    @Test
    @DisplayName("멤버 권한 위임")
    void delegateMember() throws Exception {
        final DelegateMemberRequest request = new DelegateMemberRequest(MemberType.NORMAL);

        final ResultActions response = mvc.perform(RestDocumentationRequestBuilders
                .post("/api/members/{memberId}/delegate", 2L)
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .session(MockHttpSessionCreator.dummySession())
                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
        );

        then(memberCommandService).should(times(1))
                .delegateMember(1L, 2L, MemberType.NORMAL);

        response
                .andExpect(status().isOk())
                .andDo(document("members/post-memberId-delegate",
                        pathParameters(
                                parameterWithName("memberId").description("멤버 ID")
                        ),

                        requestFields(
                                fieldWithPath("type")
                                        .type(JsonFieldType.STRING)
                                        .description("멤버 타입")
                        )
                ));
    }
}