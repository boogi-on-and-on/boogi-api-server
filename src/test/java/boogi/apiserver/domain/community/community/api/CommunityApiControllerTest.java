package boogi.apiserver.domain.community.community.api;

import boogi.apiserver.builder.TestCommunity;
import boogi.apiserver.domain.community.community.application.CommunityCommandService;
import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.dto.dto.*;
import boogi.apiserver.domain.community.community.dto.request.CommunitySettingRequest;
import boogi.apiserver.domain.community.community.dto.request.CreateCommunityRequest;
import boogi.apiserver.domain.community.community.dto.request.JoinRequestIdsRequest;
import boogi.apiserver.domain.community.community.dto.request.UpdateCommunityRequest;
import boogi.apiserver.domain.community.community.dto.response.CommunityDetailResponse;
import boogi.apiserver.domain.community.community.dto.response.CommunityPostsResponse;
import boogi.apiserver.domain.community.community.exception.AlreadyExistsCommunityNameException;
import boogi.apiserver.domain.community.community.exception.CanNotDeleteCommunityException;
import boogi.apiserver.domain.community.community.exception.CommunityNotFoundException;
import boogi.apiserver.domain.community.joinrequest.application.JoinRequestCommandService;
import boogi.apiserver.domain.community.joinrequest.application.JoinRequestQueryService;
import boogi.apiserver.domain.community.joinrequest.exception.AlreadyRequestedException;
import boogi.apiserver.domain.community.joinrequest.exception.UnmatchedJoinRequestCommunityException;
import boogi.apiserver.domain.member.application.MemberCommandService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.dto.dto.BannedMemberDto;
import boogi.apiserver.domain.member.dto.dto.MemberDto;
import boogi.apiserver.domain.member.dto.response.JoinedMembersPageResponse;
import boogi.apiserver.domain.member.exception.*;
import boogi.apiserver.domain.notice.application.NoticeQueryService;
import boogi.apiserver.domain.notice.dto.dto.NoticeDto;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.dto.dto.CommunityPostDto;
import boogi.apiserver.domain.post.post.dto.dto.LatestCommunityPostDto;
import boogi.apiserver.domain.post.postmedia.dto.dto.PostMediaMetadataDto;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import boogi.apiserver.domain.user.dto.response.UserDetailInfoDto;
import boogi.apiserver.global.constant.HeaderConst;
import boogi.apiserver.global.dto.PaginationDto;
import boogi.apiserver.global.util.PageableUtil;
import boogi.apiserver.global.webclient.push.SendPushNotification;
import boogi.apiserver.utils.controller.TestControllerSetUp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = CommunityApiController.class)
class CommunityApiControllerTest extends TestControllerSetUp {

    @MockBean
    JoinRequestCommandService joinRequestCommandService;

    @MockBean
    CommunityCommandService communityCommandService;

    @MockBean
    MemberCommandService memberCommandService;

    @MockBean
    CommunityQueryService communityQueryService;

    @MockBean
    NoticeQueryService noticeQueryService;

    @MockBean
    MemberQueryService memberQueryService;

    @MockBean
    PostQueryService postQueryService;

    @MockBean
    JoinRequestQueryService joinRequestQueryService;

    @MockBean
    CommunityRepository communityRepository;

    @MockBean
    UserRepository userRepository;

    @MockBean
    SendPushNotification sendPushNotification;


    @Nested
    @DisplayName("커뮤니티 생성")
    class CreateCommunity {
        @Test
        @DisplayName("커뮤니티 생성에 성공한다.")
        void createCommunitySuccess() throws Exception {
            final Long NEW_COMMUNITY_ID = 1L;
            //given
            CreateCommunityRequest request = new CreateCommunityRequest("커뮤니티", "CLUB",
                    "커뮤니티 설명입니다.", List.of("해시태그"), false, true);

            given(communityCommandService.createCommunity(any(), anyLong())).willReturn(NEW_COMMUNITY_ID);

            //when
            ResultActions result = mvc.perform(
                    post("/api/communities")
                            .content(mapper.writeValueAsBytes(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            //then
            result
                    .andExpect(status().isCreated())
                    .andDo(document("communities/post",
                            requestFields(
                                    fieldWithPath("name").type(JsonFieldType.STRING)
                                            .description("커뮤니티 이름"),
                                    fieldWithPath("category").type(JsonFieldType.STRING)
                                            .description("커뮤니티 카테고리"),
                                    fieldWithPath("description").type(JsonFieldType.STRING)
                                            .description("커뮤니티 소개"),
                                    fieldWithPath("hashtags").type(JsonFieldType.ARRAY)
                                            .description("커뮤니티 해시태그"),
                                    fieldWithPath("isPrivate").type(JsonFieldType.BOOLEAN)
                                            .description("true -> 커뮤니티 공개"),
                                    fieldWithPath("autoApproval").type(JsonFieldType.BOOLEAN)
                                            .description("true -> 커뮤니티 가입 자동 승인")
                            ),
                            responseFields(
                                    fieldWithPath("id").type(JsonFieldType.NUMBER)
                                            .description("생성된 커뮤니티 ID")
                            )
                    ));
        }

        @Test
        @DisplayName("이미 존재하는 커뮤니티 이름으로 요청할 경우 AlreadyExistsCommunityNameException 발생")
        void alreadyExistCommunityNameFail() throws Exception {
            CreateCommunityRequest request = new CreateCommunityRequest("커뮤니티", "CLUB",
                    "커뮤니티 설명입니다.", List.of("해시태그"), false, true);

            doThrow(new AlreadyExistsCommunityNameException())
                    .when(communityCommandService).createCommunity(any(), anyLong());

            ResultActions result = mvc.perform(
                    post("/api/communities")
                            .content(mapper.writeValueAsBytes(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/post-AlreadyExistsCommunityNameException"));
        }
    }

    @Nested
    @DisplayName("커뮤니티 상세 조회")
    class GetCommunityDetail {
        @Test
        @DisplayName("커뮤니티 상세조회에 성공한다.")
        void getCommunityDetailSuccess() throws Exception {
            final CommunityDetailInfoDto communityDto = new CommunityDetailInfoDto(true, "ACADEMIC",
                    "커뮤니티 이름", "소개", List.of("해시태그"), "1", LocalDateTime.now());
            final NoticeDto noticeDto = new NoticeDto(1L, "공지", LocalDateTime.now());
            final LatestCommunityPostDto postDto = new LatestCommunityPostDto(1L, "글 내용", LocalDateTime.now());
            final CommunityDetailResponse response =
                    new CommunityDetailResponse(MemberType.MANAGER, communityDto, List.of(noticeDto), List.of(postDto));

            given(communityQueryService.getCommunityDetail(anyLong(), anyLong()))
                    .willReturn(response);

            ResultActions result = mvc.perform(
                    get("/api/communities/{communityId}", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().isOk())
                    .andDo(document("communities/get-communityId",
                            pathParameters(
                                    parameterWithName("communityId").description("커뮤니티 ID")
                            ),
                            responseFields(
                                    fieldWithPath("sessionMemberType").type(JsonFieldType.STRING)
                                            .description("요청한 유저의 멤버타입").optional(),
                                    fieldWithPath("community").type(JsonFieldType.OBJECT)
                                            .description("커뮤니티 정보"),
                                    fieldWithPath("community.isPrivated").type(JsonFieldType.BOOLEAN)
                                            .description("true -> 비공개 커뮤니티"),
                                    fieldWithPath("community.category").type(JsonFieldType.STRING)
                                            .description("커뮤니티 카테고리"),
                                    fieldWithPath("community.name").type(JsonFieldType.STRING)
                                            .description("커뮤니티 이름"),
                                    fieldWithPath("community.introduce").type(JsonFieldType.STRING)
                                            .description("커뮤니티 소개"),
                                    fieldWithPath("community.hashtags").type(JsonFieldType.ARRAY)
                                            .description("커뮤니티 해시태그").optional(),
                                    fieldWithPath("community.memberCount").type(JsonFieldType.STRING)
                                            .description("커뮤니티 멤버 수"),
                                    fieldWithPath("community.createdAt").type(JsonFieldType.STRING)
                                            .description("커뮤니티 생성시각"),
                                    fieldWithPath("notices").type(JsonFieldType.ARRAY)
                                            .description("커뮤니티의 공지사항 목록"),
                                    fieldWithPath("notices[].id").type(JsonFieldType.NUMBER)
                                            .description("공지사항 ID"),
                                    fieldWithPath("notices[].title").type(JsonFieldType.STRING)
                                            .description("공지사항 제목"),
                                    fieldWithPath("notices[].createdAt").type(JsonFieldType.STRING)
                                            .description("공지사항 생성시각"),

                                    fieldWithPath("posts").type(JsonFieldType.ARRAY)
                                            .description("커뮤니티의 최근 글의 목록").optional(),
                                    fieldWithPath("posts[].id").type(JsonFieldType.NUMBER)
                                            .description("게시글 ID"),
                                    fieldWithPath("posts[].content").type(JsonFieldType.STRING)
                                            .description("게시글의 내용"),
                                    fieldWithPath("posts[].createdAt").type(JsonFieldType.STRING)
                                            .description("게시글의 생성시각")
                            )
                    ));
        }

        @Test
        @DisplayName("존재하지 않는 커뮤니티 ID로 요청한 경우 CommunityNotFoundException 발생")
        void notExistCommunityFail() throws Exception {
            doThrow(new CommunityNotFoundException())
                    .when(communityQueryService).getCommunityDetail(anyLong(), anyLong());

            ResultActions result = mvc.perform(
                    get("/api/communities/{communityId}", 9999L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/get-communityId-CommunityNotFoundException"));
        }
    }

    @Nested
    @DisplayName("커뮤니티 메타데이터 조회")
    class GetCommunityMetaData {
        @Test
        @DisplayName("커뮤니티 메타데이터 조회에 성공한다.")
        void getMetadataSuccess() throws Exception {
            CommunityMetadataDto response = new CommunityMetadataDto("이름", "소개", List.of("해시태그"));

            given(communityQueryService.getCommunityMetadata(anyLong(), anyLong()))
                    .willReturn(response);

            ResultActions result = mvc.perform(
                    get("/api/communities/{communityId}/metadata", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().isOk())
                    .andDo(document("communities/get-communityId-metadata",
                            pathParameters(
                                    parameterWithName("communityId").description("커뮤니티 ID")
                            ),
                            responseFields(
                                    fieldWithPath("metadata").type(JsonFieldType.OBJECT)
                                            .description("커뮤니티 메타데이터"),
                                    fieldWithPath("metadata.name").type(JsonFieldType.STRING)
                                            .description("커뮤니티 이름"),
                                    fieldWithPath("metadata.introduce").type(JsonFieldType.STRING)
                                            .description("커뮤니티 소개"),
                                    fieldWithPath("metadata.hashtags").type(JsonFieldType.ARRAY)
                                            .description("커뮤니티 해시태그")
                            )
                    ));
        }

        @Test
        @DisplayName("존재하지 않는 커뮤니티 ID로 요청한 경우 CommunityNotFoundException 발생")
        void notExistCommunityFail() throws Exception {
            doThrow(new CommunityNotFoundException())
                    .when(communityQueryService).getCommunityMetadata(anyLong(), anyLong());

            ResultActions result = mvc.perform(
                    get("/api/communities/{communityId}/metadata", 9999L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/get-communityId-metadata-CommunityNotFoundException"));
        }

        @Test
        @DisplayName("해당 커뮤니티에 가입되지 않는 경우 NotJoinedMemberException 발생")
        void notMemberFail() throws Exception {
            doThrow(new NotJoinedMemberException())
                    .when(communityQueryService).getCommunityMetadata(anyLong(), anyLong());

            ResultActions result = mvc.perform(
                    get("/api/communities/{communityId}/metadata", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/get-communityId-metadata-NotJoinedMemberException"));
        }

        @Test
        @DisplayName("해당 커뮤니티의 매니저가 아닌 경우 NotManagerException 발생")
        void notManagerFail() throws Exception {
            doThrow(new NotManagerException())
                    .when(communityQueryService).getCommunityMetadata(anyLong(), anyLong());

            ResultActions result = mvc.perform(
                    get("/api/communities/{communityId}/metadata", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/get-communityId-metadata-NotManagerException"));
        }
    }

    @Nested
    @DisplayName("커뮤니티 수정")
    class UpdateCommunity {
        @Test
        @DisplayName("커뮤니티 업데이트 성공")
        void updateCommunitySuccess() throws Exception {
            UpdateCommunityRequest request =
                    new UpdateCommunityRequest("커뮤니티 설명 수정입니다.", List.of("해시태그2"));

            final ResultActions result = mvc.perform(
                    patch("/api/communities/{communityId}", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request))
            );

            result
                    .andExpect(status().isOk())
                    .andDo(document("communities/patch-communityId",
                            pathParameters(
                                    parameterWithName("communityId").description("커뮤니티 ID")
                            ),
                            requestFields(
                                    fieldWithPath("description").type(JsonFieldType.STRING)
                                            .description("커뮤니티 소개란"),
                                    fieldWithPath("hashtags").type(JsonFieldType.ARRAY)
                                            .description("커뮤니티 해시테그").optional()
                            )
                    ));
        }

        @Test
        @DisplayName("존재하지 않는 커뮤니티 ID로 요청한 경우 CommunityNotFoundException 발생")
        void notExistCommunityFail() throws Exception {
            UpdateCommunityRequest request =
                    new UpdateCommunityRequest("커뮤니티 설명 수정입니다.", List.of("해시태그"));

            doThrow(new CommunityNotFoundException())
                    .when(communityCommandService).updateCommunity(anyLong(), anyLong(), anyString(), anyList());

            final ResultActions result = mvc.perform(
                    patch("/api/communities/{communityId}", 9999L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request))
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/patch-communityId-CommunityNotFoundException"));
        }

        @Test
        @DisplayName("해당 커뮤니티에 가입되지 않는 경우 NotJoinedMemberException 발생")
        void notMemberFail() throws Exception {
            UpdateCommunityRequest request =
                    new UpdateCommunityRequest("커뮤니티 설명 수정입니다.", List.of("해시태그"));

            doThrow(new NotJoinedMemberException())
                    .when(communityCommandService).updateCommunity(anyLong(), anyLong(), anyString(), anyList());

            final ResultActions result = mvc.perform(
                    patch("/api/communities/{communityId}", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request))
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/patch-communityId-NotJoinedMemberException"));
        }

        @Test
        @DisplayName("해당 커뮤니티의 매니저가 아닌 경우 NotManagerException 발생")
        void notManagerFail() throws Exception {
            UpdateCommunityRequest request =
                    new UpdateCommunityRequest("커뮤니티 설명 수정입니다.", List.of("해시태그"));

            doThrow(new NotManagerException())
                    .when(communityCommandService).updateCommunity(anyLong(), anyLong(), anyString(), anyList());

            final ResultActions result = mvc.perform(
                    patch("/api/communities/{communityId}", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request))
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/patch-communityId-NotManagerException"));
        }
    }

    @Nested
    @DisplayName("커뮤니티 폐쇄")
    class CommunityShutdown {
        @Test
        @DisplayName("커뮤니티 폐쇄에 성공한다.")
        void communityShutdownSuccess() throws Exception {
            final ResultActions result = mvc.perform(
                    delete("/api/communities/{communityId}", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().isOk())
                    .andDo(document("communities/delete-communityId",
                            pathParameters(
                                    parameterWithName("communityId").description("커뮤니티 ID")
                            )
                    ));
        }

        @Test
        @DisplayName("존재하지 않는 커뮤니티 ID로 요청한 경우 CommunityNotFoundException 발생")
        void notExistCommunityFail() throws Exception {
            doThrow(new CommunityNotFoundException())
                    .when(communityCommandService).shutdown(anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    delete("/api/communities/{communityId}", 9999L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/delete-communityId-CommunityNotFoundException"));
        }

        @Test
        @DisplayName("해당 커뮤니티에 가입되지 않는 경우 NotJoinedMemberException 발생")
        void notMemberFail() throws Exception {
            doThrow(new NotJoinedMemberException())
                    .when(communityCommandService).shutdown(anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    delete("/api/communities/{communityId}", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/delete-communityId-NotJoinedMemberException"));
        }

        @Test
        @DisplayName("해당 커뮤니티의 매니저가 아닌 경우 NotManagerException 발생")
        void notManagerFail() throws Exception {
            doThrow(new NotManagerException())
                    .when(communityCommandService).shutdown(anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    delete("/api/communities/{communityId}", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/delete-communityId-NotManagerException"));
        }

        @Test
        @DisplayName("해당 커뮤니티에 매니저 이외의 멤버가 존재하는 경우 CanNotDeleteCommunityException 발생")
        void notOnlyManagerIncludedFail() throws Exception {
            doThrow(new CanNotDeleteCommunityException())
                    .when(communityCommandService).shutdown(anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    delete("/api/communities/{communityId}", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/delete-communityId-CanNotDeleteCommunityException"));
        }
    }

    @Nested
    @DisplayName("커뮤니티 설정 정보 조회")
    class GetCommunitySetting {
        @Test
        @DisplayName("커뮤니티 설정정보 조회에 성공한다.")
        void getSettingInfoSuccess() throws Exception {
            final Community community = TestCommunity.builder()
                    .isPrivate(true)
                    .autoApproval(true)
                    .build();

            CommunitySettingInfoDto settingInfo = CommunitySettingInfoDto.of(community);

            given(communityQueryService.getSetting(anyLong(), anyLong())).willReturn(settingInfo);

            final ResultActions result = mvc.perform(
                    get("/api/communities/{communityId}/settings", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            result
                    .andExpect(status().isOk())
                    .andDo(document("communities/get-communityId-settings",
                            pathParameters(
                                    parameterWithName("communityId").description("커뮤니티 ID")
                            ),
                            responseFields(
                                    fieldWithPath("settingInfo").type(JsonFieldType.OBJECT)
                                            .description("커뮤니티 설정 정보"),
                                    fieldWithPath("settingInfo.isAuto").type(JsonFieldType.BOOLEAN)
                                            .description("true -> 자동 가입 커뮤니티로 전환"),
                                    fieldWithPath("settingInfo.isSecret").type(JsonFieldType.BOOLEAN)
                                            .description("true -> 비공개 커뮤니티로 전환")
                            )
                    ));
        }

        @Test
        @DisplayName("존재하지 않는 커뮤니티 ID로 요청한 경우 CommunityNotFoundException 발생")
        void notExistCommunityFail() throws Exception {
            doThrow(new CommunityNotFoundException())
                    .when(communityQueryService).getSetting(anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    get("/api/communities/{communityId}/settings", 9999L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/get-communityId-settings-CommunityNotFoundException"));
        }

        @Test
        @DisplayName("해당 커뮤니티에 가입되지 않는 경우 NotJoinedMemberException 발생")
        void notMemberFail() throws Exception {
            doThrow(new NotJoinedMemberException())
                    .when(communityQueryService).getSetting(anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    get("/api/communities/{communityId}/settings", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/get-communityId-settings-NotJoinedMemberException"));
        }

        @Test
        @DisplayName("해당 커뮤니티의 매니저가 아닌 경우 NotManagerException 발생")
        void notManagerFail() throws Exception {
            doThrow(new NotManagerException())
                    .when(communityQueryService).getSetting(anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    get("/api/communities/{communityId}/settings", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/get-communityId-settings-NotManagerException"));
        }
    }

    @Nested
    @DisplayName("커뮤니티 설정 정보 수정")
    class ChangeCommunitySetting {
        @Test
        @DisplayName("커뮤니티 설정 정보 수정에 성공한다.")
        void changeCommunitySettingSuccess() throws Exception {
            final CommunitySettingRequest request = new CommunitySettingRequest(true, true);

            final ResultActions result = mvc.perform(
                    post("/api/communities/{communityId}/settings", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request))
            );

            result
                    .andExpect(status().isOk())
                    .andDo(document("communities/post-communityId-settings",
                            pathParameters(
                                    parameterWithName("communityId").description("커뮤니티 ID")
                            ),
                            requestFields(
                                    fieldWithPath("isSecret").type(JsonFieldType.BOOLEAN)
                                            .description("true -> 비공개 커뮤니티로 전환"),
                                    fieldWithPath("isAutoApproval").type(JsonFieldType.BOOLEAN)
                                            .description("true -> 자동 가입 커뮤니티로 전환")
                            )
                    ));
        }

        @Test
        @DisplayName("존재하지 않는 커뮤니티 ID로 요청한 경우 CommunityNotFoundException 발생")
        void notExistCommunityFail() throws Exception {
            final CommunitySettingRequest request = new CommunitySettingRequest(true, true);

            doThrow(new CommunityNotFoundException())
                    .when(communityCommandService).changeSetting(anyLong(), anyLong(), any(CommunitySettingRequest.class));

            final ResultActions result = mvc.perform(
                    post("/api/communities/{communityId}/settings", 9999L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request))
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/post-communityId-settings-CommunityNotFoundException"));
        }

        @Test
        @DisplayName("해당 커뮤니티에 가입되지 않는 경우 NotJoinedMemberException 발생")
        void notMemberFail() throws Exception {
            final CommunitySettingRequest request = new CommunitySettingRequest(true, true);

            doThrow(new NotJoinedMemberException())
                    .when(communityCommandService).changeSetting(anyLong(), anyLong(), any(CommunitySettingRequest.class));

            final ResultActions result = mvc.perform(
                    post("/api/communities/{communityId}/settings", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request))
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/post-communityId-settings-NotJoinedMemberException"));
        }

        @Test
        @DisplayName("해당 커뮤니티의 매니저가 아닌 경우 NotManagerException 발생")
        void notManagerFail() throws Exception {
            final CommunitySettingRequest request = new CommunitySettingRequest(true, true);

            doThrow(new NotManagerException())
                    .when(communityCommandService).changeSetting(anyLong(), anyLong(), any(CommunitySettingRequest.class));

            final ResultActions result = mvc.perform(
                    post("/api/communities/{communityId}/settings", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request))
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/post-communityId-settings-NotManagerException"));
        }
    }

    @Nested
    @DisplayName("커뮤니티 게시글 목록 조회")
    class GetPostsOfCommunity {
        @Test
        @DisplayName("커뮤니티 게시글 목록 조회에 성공한다.")
        void getPostsOfCommunitySuccess() throws Exception {
            UserBasicProfileDto userDto = new UserBasicProfileDto(1L, "url", "#0001", "유저");

            PostMediaMetadataDto postMediaDto = new PostMediaMetadataDto("media url", "IMG");

            List<CommunityPostDto> postDtos = List.of(
                    new CommunityPostDto(1L, userDto, LocalDateTime.now(), "글 내용", List.of("해시태그"),
                            List.of(postMediaDto), 1, 1, false, null)
            );

            CommunityPostsResponse response = new CommunityPostsResponse("커뮤니티 이름", postDtos,
                    new PaginationDto(1, false), MemberType.NORMAL);

            given(postQueryService.getPostsOfCommunity(any(), anyLong(), anyLong()))
                    .willReturn(response);

            final ResultActions result = mvc.perform(
                    get("/api/communities/{communityId}/posts", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .session(dummySession)
                            .queryParam("page", "0")
                            .queryParam("size", "1")
            );

            result
                    .andExpect(status().isOk())
                    .andDo(document("communities/get-communityId-posts",
                            pathParameters(
                                    parameterWithName("communityId").description("커뮤니티 ID")
                            ),
                            requestParameters(
                                    parameterWithName("page").description("페이지 번호"),
                                    parameterWithName("size").description("페이지 사이즈")
                            ),
                            responseFields(
                                    fieldWithPath("communityName").type(JsonFieldType.STRING)
                                            .description("커뮤니티 이름"),
                                    fieldWithPath("memberType").type(JsonFieldType.STRING)
                                            .description("멤버의 타입"),
                                    fieldWithPath("posts").type(JsonFieldType.ARRAY)
                                            .description("커뮤니티의 게시글 목록"),
                                    fieldWithPath("posts[].id").type(JsonFieldType.NUMBER)
                                            .description("게시글 ID"),
                                    fieldWithPath("posts[].createdAt").type(JsonFieldType.STRING)
                                            .description("게시글 생성시각"),
                                    fieldWithPath("posts[].content").type(JsonFieldType.STRING)
                                            .description("게시글 내용"),
                                    fieldWithPath("posts[].hashtags").type(JsonFieldType.ARRAY)
                                            .description("해시태그").optional(),
                                    fieldWithPath("posts[].postMedias").type(JsonFieldType.ARRAY)
                                            .description("게시글 미디어 정보").optional(),
                                    fieldWithPath("posts[].postMedias[].url").type(JsonFieldType.STRING)
                                            .description("게시글 미디어 경로"),
                                    fieldWithPath("posts[].postMedias[].type").type(JsonFieldType.STRING)
                                            .description("게시글 미디어 타입"),
                                    fieldWithPath("posts[].likeCount").type(JsonFieldType.NUMBER)
                                            .description("게시글 좋아요 수"),
                                    fieldWithPath("posts[].commentCount").type(JsonFieldType.NUMBER)
                                            .description("게시글 댓글 수"),
                                    fieldWithPath("posts[].me").type(JsonFieldType.BOOLEAN)
                                            .description("true -> 내가 작성한 게시글"),
                                    fieldWithPath("posts[].likeId").type(JsonFieldType.NUMBER)
                                            .description("좋아요 ID /요청한 유저가 좋아요 한 경우").optional(),
                                    fieldWithPath("posts[].user").type(JsonFieldType.OBJECT)
                                            .description("게시글 작성자 정보"),
                                    fieldWithPath("posts[].user.id").type(JsonFieldType.NUMBER)
                                            .description("유저 ID"),
                                    fieldWithPath("posts[].user.profileImageUrl").type(JsonFieldType.STRING)
                                            .description("유저 프로필 경로").optional(),
                                    fieldWithPath("posts[].user.tagNum").type(JsonFieldType.STRING)
                                            .description("태그번호"),
                                    fieldWithPath("posts[].user.name").type(JsonFieldType.STRING)
                                            .description("이름"),

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

        @Test
        @DisplayName("존재하지 않는 커뮤니티 ID로 요청한 경우 CommunityNotFoundException 발생")
        void notExistCommunityFail() throws Exception {
            doThrow(new CommunityNotFoundException())
                    .when(postQueryService).getPostsOfCommunity(any(Pageable.class), anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    get("/api/communities/{communityId}/posts", 9999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .session(dummySession)
                            .queryParam("page", "0")
                            .queryParam("size", "1")
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/get-communityId-posts-CommunityNotFoundException"));
        }

        @Test
        @DisplayName("해당 커뮤니티에 비가입으로 요청한 경우 NotViewableMemberException 발생")
        void notViewableMemberFail() throws Exception {
            doThrow(new NotViewableMemberException())
                    .when(postQueryService).getPostsOfCommunity(any(Pageable.class), anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    get("/api/communities/{communityId}/posts", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .session(dummySession)
                            .queryParam("page", "0")
                            .queryParam("size", "1")
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/get-communityId-posts-NotViewableMemberException"));
        }
    }

    @Nested
    @DisplayName("커뮤니티에 가입된 멤버 목록 조회")
    class GetCommunityJoinedMembers {
        @Test
        @DisplayName("커뮤니티에 가입된 멤버 목록을 페이지네이션해서 조회한다.")
        void getCommunityJoinedMembersSuccess() throws Exception {
            final UserDetailInfoDto userDto = new UserDetailInfoDto(1L, "url", "유저", "#0001",
                    "자기소개", "학과");
            final JoinedMemberInfoDto memberDto = new JoinedMemberInfoDto(1L, "SUB_MANAGER",
                    LocalDateTime.now().toString(), userDto);

            final JoinedMembersPageResponse response = new JoinedMembersPageResponse(List.of(memberDto),
                    new PaginationDto(1, false));

            given(memberQueryService.getCommunityJoinedMembers(any(), anyLong()))
                    .willReturn(response);

            final ResultActions result = mvc.perform(
                    get("/api/communities/{communityId}/members", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().isOk())
                    .andDo(document("communities/get-communityId-members",
                            pathParameters(
                                    parameterWithName("communityId").description("커뮤니티 ID")
                            ),
                            responseFields(
                                    fieldWithPath("members").type(JsonFieldType.ARRAY)
                                            .description("가입한 멤버 목록"),
                                    fieldWithPath("members[].id").type(JsonFieldType.NUMBER)
                                            .description("멤버 아이디"),
                                    fieldWithPath("members[].memberType").type(JsonFieldType.STRING)
                                            .description("멤버 타입"),
                                    fieldWithPath("members[].createdAt").type(JsonFieldType.STRING)
                                            .description("커뮤니티 가입일"),

                                    //todo: UserDetailInfoDto 공통화하기
                                    fieldWithPath("members[].user.id").type(JsonFieldType.NUMBER)
                                            .description("유저의 ID"),
                                    fieldWithPath("members[].user.profileImageUrl").type(JsonFieldType.STRING)
                                            .description("프로필이미지 경로").optional(),
                                    fieldWithPath("members[].user.name").type(JsonFieldType.STRING)
                                            .description("유저의 이름"),
                                    fieldWithPath("members[].user.tagNum").type(JsonFieldType.STRING)
                                            .description("태그번호"),
                                    fieldWithPath("members[].user.introduce").type(JsonFieldType.STRING)
                                            .description("자기소개"),
                                    fieldWithPath("members[].user.department").type(JsonFieldType.STRING)
                                            .description("학과"),

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

        @Test
        @DisplayName("존재하지 않는 커뮤니티 ID로 요청한 경우 CommunityNotFoundException 발생")
        void notExistCommunityFail() throws Exception {
            doThrow(new CommunityNotFoundException())
                    .when(memberQueryService).getCommunityJoinedMembers(any(Pageable.class), anyLong());

            final ResultActions result = mvc.perform(
                    get("/api/communities/{communityId}/members", 9999L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/get-communityId-members-CommunityNotFoundException"));
        }
    }

    @Nested
    @DisplayName("차단된 멤버 목록 조회")
    class GetBannedMembers {
        @Test
        @DisplayName("차단된 멤버 목록 조회시 성공한다.")
        void getBannedMembersSuccess() throws Exception {
            UserBasicProfileDto userDto = new UserBasicProfileDto(2L, "url", "#0001", "홍길동");
            BannedMemberDto memberDto = new BannedMemberDto(1L, userDto);

            given(memberQueryService.getBannedMembers(anyLong(), anyLong()))
                    .willReturn(List.of(memberDto));

            final ResultActions result = mvc.perform(
                    get("/api/communities/{communityId}/members/banned", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().isOk())
                    .andDo(document("communities/get-communityId-members-banned",
                            pathParameters(
                                    parameterWithName("communityId").description("커뮤니티 ID")
                            ),
                            responseFields(
                                    fieldWithPath("banned").type(JsonFieldType.ARRAY)
                                            .description("차단된 멤버 목록"),
                                    fieldWithPath("banned[].memberId").type(JsonFieldType.NUMBER)
                                            .description("멤버 아이디"),

                                    //todo: UserBasicProfile 공통화하기
                                    fieldWithPath("banned[].user.id").type(JsonFieldType.NUMBER)
                                            .description("유저의 ID"),
                                    fieldWithPath("banned[].user.profileImageUrl").type(JsonFieldType.STRING)
                                            .description("프로필이미지 경로").optional(),
                                    fieldWithPath("banned[].user.name").type(JsonFieldType.STRING)
                                            .description("유저의 이름"),
                                    fieldWithPath("banned[].user.tagNum").type(JsonFieldType.STRING)
                                            .description("태그번호")
                            )
                    ));
        }

        @Test
        @DisplayName("존재하지 않는 커뮤니티 ID로 요청한 경우 CommunityNotFoundException 발생")
        void notExistCommunityFail() throws Exception {
            doThrow(new CommunityNotFoundException())
                    .when(memberQueryService).getBannedMembers(anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    get("/api/communities/{communityId}/members/banned", 9999L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/get-communityId-members-banned-CommunityNotFoundException"));
        }

        @Test
        @DisplayName("해당 커뮤니티에 가입되지 않는 경우 NotJoinedMemberException 발생")
        void notMemberFail() throws Exception {
            doThrow(new NotJoinedMemberException())
                    .when(memberQueryService).getBannedMembers(anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    get("/api/communities/{communityId}/members/banned", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/get-communityId-members-banned-NotJoinedMemberException"));
        }

        @Test
        @DisplayName("해당 커뮤니티의 관리자가 아닌 경우 NotOperatorException 발생")
        void notOperatorFail() throws Exception {
            doThrow(new NotOperatorException())
                    .when(memberQueryService).getBannedMembers(anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    get("/api/communities/{communityId}/members/banned", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/get-communityId-members-banned-NotOperatorException"));
        }
    }

    @Nested
    @DisplayName("커뮤니티 가입 요청 목록 조회")
    class GetCommunityJoinRequest {
        @Test
        @DisplayName("커뮤니티 가입 요청 목록 조회시 성공한다.")
        void getCommunityJoinRequestSuccess() throws Exception {
            UserBasicProfileDto userDto = new UserBasicProfileDto(1L, "url", "#0001", "유저");
            UserJoinRequestInfoDto joinRequestDto = new UserJoinRequestInfoDto(userDto, 1L);

            given(joinRequestQueryService.getAllRequests(anyLong(), anyLong()))
                    .willReturn(List.of(joinRequestDto));

            final ResultActions result = mvc.perform(
                    get("/api/communities/{communityId}/requests", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().isOk())
                    .andDo(document("communities/get-communityId-requests",
                            pathParameters(
                                    parameterWithName("communityId").description("커뮤니티 ID")
                            ),
                            responseFields(
                                    fieldWithPath("requests[].id").type(JsonFieldType.NUMBER)
                                            .description("가입요청 ID"),
                                    fieldWithPath("requests[].user").type(JsonFieldType.OBJECT)
                                            .description("가입요청한 유저 정보"),

                                    //todo: UserBasicProfile extract하기
                                    fieldWithPath("requests[].user.id").type(JsonFieldType.NUMBER)
                                            .description("유저의 ID"),
                                    fieldWithPath("requests[].user.profileImageUrl").type(JsonFieldType.STRING)
                                            .description("프로필이미지 경로").optional(),
                                    fieldWithPath("requests[].user.name").type(JsonFieldType.STRING)
                                            .description("유저의 이름"),
                                    fieldWithPath("requests[].user.tagNum").type(JsonFieldType.STRING)
                                            .description("태그번호")
                            )
                    ));
        }

        @Test
        @DisplayName("존재하지 않는 커뮤니티 ID로 요청한 경우 CommunityNotFoundException 발생")
        void notExistCommunityFail() throws Exception {
            doThrow(new CommunityNotFoundException())
                    .when(joinRequestQueryService).getAllRequests(anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    get("/api/communities/{communityId}/requests", 9999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/get-communityId-requests-CommunityNotFoundException"));
        }

        @Test
        @DisplayName("해당 커뮤니티에 가입되지 않는 경우 NotJoinedMemberException 발생")
        void notMemberFail() throws Exception {
            doThrow(new NotJoinedMemberException())
                    .when(joinRequestQueryService).getAllRequests(anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    get("/api/communities/{communityId}/requests", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/get-communityId-requests-NotJoinedMemberException"));
        }

        @Test
        @DisplayName("해당 커뮤니티의 관리자가 아닌 경우 NotOperatorException 발생")
        void notOperatorFail() throws Exception {
            doThrow(new NotOperatorException())
                    .when(joinRequestQueryService).getAllRequests(anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    get("/api/communities/{communityId}/requests", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/get-communityId-requests-NotOperatorException"));
        }
    }

    @Nested
    @DisplayName("가입 요청")
    class JoinRequest {
        @Test
        @DisplayName("가입 요청시 성공한다.")
        void joinRequestSuccess() throws Exception {
            final long APPLIED_JOIN_REQUEST_ID = 1L;
            given(joinRequestCommandService.request(anyLong(), anyLong()))
                    .willReturn(APPLIED_JOIN_REQUEST_ID);

            final ResultActions result = mvc.perform(
                    post("/api/communities/{communityId}/requests", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().isOk())
                    .andDo(document("communities/post-communityId-requests",
                            pathParameters(
                                    parameterWithName("communityId").description("커뮤니티 ID")
                            ),
                            responseFields(
                                    fieldWithPath("id").type(JsonFieldType.NUMBER)
                                            .description("가입요청 ID")
                            )
                    ));
        }

        @Test
        @DisplayName("존재하지 않는 커뮤니티 ID로 요청한 경우 CommunityNotFoundException 발생")
        void notExistCommunityFail() throws Exception {
            doThrow(new CommunityNotFoundException())
                    .when(joinRequestCommandService).request(anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    post("/api/communities/{communityId}/requests", 9999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/post-communityId-requests-CommunityNotFoundException"));
        }

        @Test
        @DisplayName("이미 가입된 상태인 경우 AlreadyJoinedMemberException 발생")
        void alreadyJoinedFail() throws Exception {
            doThrow(new AlreadyJoinedMemberException())
                    .when(joinRequestCommandService).request(anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    post("/api/communities/{communityId}/requests", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/post-communityId-requests-AlreadyJoinedMemberException"));
        }

        @Test
        @DisplayName("이미 가입 요청을 했고, 해당 가입 요청의 상태가 대기중 경우 AlreadyRequestedException 발생")
        void joinRequestStatusFail() throws Exception {
            doThrow(new AlreadyRequestedException())
                    .when(joinRequestCommandService).request(anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    post("/api/communities/{communityId}/requests", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/post-communityId-requests-AlreadyRequestedException"));
        }
    }

    @Nested
    @DisplayName("가입 요청 승인")
    class ConfirmJoinRequest {
        @Test
        @DisplayName("가입 요청 승인에 성공한다.")
        void confirmJoinRequestSuccess() throws Exception {
            final long APPLY_JOIN_REQUEST_ID = 1L;
            JoinRequestIdsRequest request = new JoinRequestIdsRequest(List.of(APPLY_JOIN_REQUEST_ID));

            final ResultActions result = mvc.perform(
                    post("/api/communities/{communityId}/requests/confirm", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .session(dummySession)
                            .content(mapper.writeValueAsString(request))
            );

            then(sendPushNotification).should(times(1))
                    .joinNotification(anyList());

            result
                    .andExpect(status().isOk())
                    .andDo(document("communities/post-communityId-requests-confirm",
                            pathParameters(
                                    parameterWithName("communityId").description("커뮤니티 ID")
                            ),
                            requestFields(
                                    fieldWithPath("requestIds").type(JsonFieldType.ARRAY)
                                            .description("가입요청 ID 목록")
                            )
                    ));
        }

        @Test
        @DisplayName("존재하지 않는 커뮤니티 ID로 요청한 경우 CommunityNotFoundException 발생")
        void notExistCommunityFail() throws Exception {
            JoinRequestIdsRequest request = new JoinRequestIdsRequest(List.of(1L));

            doThrow(new CommunityNotFoundException())
                    .when(joinRequestCommandService).confirmUsers(anyLong(), anyList(), anyLong());

            final ResultActions result = mvc.perform(
                    post("/api/communities/{communityId}/requests/confirm", 9999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .session(dummySession)
                            .content(mapper.writeValueAsString(request))
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/post-communityId-requests-confirm-CommunityNotFoundException"));
        }

        @Test
        @DisplayName("해당 커뮤니티에 가입되지 않는 경우 NotJoinedMemberException 발생")
        void notMemberFail() throws Exception {
            JoinRequestIdsRequest request = new JoinRequestIdsRequest(List.of(1L));

            doThrow(new NotJoinedMemberException())
                    .when(joinRequestCommandService).confirmUsers(anyLong(), anyList(), anyLong());

            final ResultActions result = mvc.perform(
                    post("/api/communities/{communityId}/requests/confirm", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .session(dummySession)
                            .content(mapper.writeValueAsString(request))
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/post-communityId-requests-confirm-NotJoinedMemberException"));
        }

        @Test
        @DisplayName("해당 커뮤니티의 관리자가 아닌 경우 NotOperatorException 발생")
        void notOperatorFail() throws Exception {
            JoinRequestIdsRequest request = new JoinRequestIdsRequest(List.of(1L));

            doThrow(new NotOperatorException())
                    .when(joinRequestCommandService).confirmUsers(anyLong(), anyList(), anyLong());

            final ResultActions result = mvc.perform(
                    post("/api/communities/{communityId}/requests/confirm", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .session(dummySession)
                            .content(mapper.writeValueAsString(request))
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/post-communityId-requests-confirm-NotOperatorException"));
        }

        @Test
        @DisplayName("해당 커뮤니티의 가입 요청이 아닌 경우 UnmatchedJoinRequestCommunityException 발생")
        void unmatchedCommunityIdFail() throws Exception {
            JoinRequestIdsRequest request = new JoinRequestIdsRequest(List.of(1L));

            doThrow(new UnmatchedJoinRequestCommunityException())
                    .when(joinRequestCommandService).confirmUsers(anyLong(), anyList(), anyLong());

            final ResultActions result = mvc.perform(
                    post("/api/communities/{communityId}/requests/confirm", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .session(dummySession)
                            .content(mapper.writeValueAsString(request))
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/post-communityId-requests-confirm-UnmatchedJoinRequestCommunityException"));
        }

        @Test
        @DisplayName("이미 가입된 유저가 있는 경우 AlreadyJoinedMemberException 발생")
        void alreadyJoinedFail() throws Exception {
            JoinRequestIdsRequest request = new JoinRequestIdsRequest(List.of(1L));

            doThrow(new AlreadyJoinedMemberException())
                    .when(joinRequestCommandService).confirmUsers(anyLong(), anyList(), anyLong());

            final ResultActions result = mvc.perform(
                    post("/api/communities/{communityId}/requests/confirm", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .session(dummySession)
                            .content(mapper.writeValueAsString(request))
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/post-communityId-requests-confirm-AlreadyJoinedMemberException"));
        }
    }

    @Nested
    @DisplayName("가입 요청 거절")
    class RejectJoinRequest {
        @Test
        @DisplayName("가입요청 거절에 성공한다.")
        void reject() throws Exception {
            final long REJECT_JOIN_REQUEST_ID = 1L;
            JoinRequestIdsRequest request = new JoinRequestIdsRequest(List.of(REJECT_JOIN_REQUEST_ID));

            final ResultActions result = mvc.perform(
                    post("/api/communities/{communityId}/requests/reject", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .session(dummySession)
                            .content(mapper.writeValueAsString(request))
            );

            then(sendPushNotification).should(times(1))
                    .rejectNotification(anyList());

            result
                    .andExpect(status().isOk())
                    .andDo(document("communities/post-communityId-requests-reject",
                            pathParameters(
                                    parameterWithName("communityId").description("커뮤니티 ID")
                            ),
                            requestFields(
                                    fieldWithPath("requestIds").type(JsonFieldType.ARRAY)
                                            .description("가입요청 ID 목록")
                            )
                    ));
        }

        @Test
        @DisplayName("존재하지 않는 커뮤니티 ID로 요청한 경우 CommunityNotFoundException 발생")
        void notExistCommunityFail() throws Exception {
            JoinRequestIdsRequest request = new JoinRequestIdsRequest(List.of(1L));

            doThrow(new CommunityNotFoundException())
                    .when(joinRequestCommandService).rejectUsers(anyLong(), anyList(), anyLong());

            final ResultActions result = mvc.perform(
                    post("/api/communities/{communityId}/requests/reject", 9999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .session(dummySession)
                            .content(mapper.writeValueAsString(request))
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/post-communityId-requests-reject-CommunityNotFoundException"));
        }

        @Test
        @DisplayName("해당 커뮤니티에 가입되지 않는 경우 NotJoinedMemberException 발생")
        void notMemberFail() throws Exception {
            JoinRequestIdsRequest request = new JoinRequestIdsRequest(List.of(1L));

            doThrow(new NotJoinedMemberException())
                    .when(joinRequestCommandService).rejectUsers(anyLong(), anyList(), anyLong());

            final ResultActions result = mvc.perform(
                    post("/api/communities/{communityId}/requests/reject", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .session(dummySession)
                            .content(mapper.writeValueAsString(request))
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/post-communityId-requests-reject-NotJoinedMemberException"));
        }

        @Test
        @DisplayName("해당 커뮤니티의 관리자가 아닌 경우 NotOperatorException 발생")
        void notOperatorFail() throws Exception {
            JoinRequestIdsRequest request = new JoinRequestIdsRequest(List.of(1L));

            doThrow(new NotOperatorException())
                    .when(joinRequestCommandService).rejectUsers(anyLong(), anyList(), anyLong());

            final ResultActions result = mvc.perform(
                    post("/api/communities/{communityId}/requests/reject", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .session(dummySession)
                            .content(mapper.writeValueAsString(request))
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/post-communityId-requests-reject-NotOperatorException"));
        }

        @Test
        @DisplayName("해당 커뮤니티의 가입 요청이 아닌 경우 UnmatchedJoinRequestCommunityException 발생")
        void unmatchedCommunityIdFail() throws Exception {
            JoinRequestIdsRequest request = new JoinRequestIdsRequest(List.of(1L));

            doThrow(new UnmatchedJoinRequestCommunityException())
                    .when(joinRequestCommandService).rejectUsers(anyLong(), anyList(), anyLong());

            final ResultActions result = mvc.perform(
                    post("/api/communities/{communityId}/requests/reject", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .session(dummySession)
                            .content(mapper.writeValueAsString(request))
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/post-communityId-requests-reject-UnmatchedJoinRequestCommunityException"));
        }
    }

    @Test
    @DisplayName("커뮤니티 검색에 성공한다.")
    void searchCommunitySuccess() throws Exception {
        SearchCommunityDto communityDto = new SearchCommunityDto(1L, "커뮤니티", "소개",
                LocalDateTime.now(), List.of("해시태그"), 1, "HOBBY", false);

        Slice<SearchCommunityDto> communityPage = PageableUtil.getSlice(List.of(communityDto), PageRequest.of(0, 1));

        given(communityQueryService.getSearchedCommunities(any(), any()))
                .willReturn(communityPage);

        final ResultActions result = mvc.perform(
                get("/api/communities/search")
                        .session(dummySession)
                        .header(HeaderConst.AUTH_TOKEN, TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("isPrivate", "FALSE")
                        .queryParam("order", "OLDER")
                        .queryParam("category", "HOBBY")
                        .queryParam("keyword", "안녕")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
        );

        result
                .andExpect(status().isOk())
                .andDo(document("communities/get-search",
                        requestParameters(
                                parameterWithName("isPrivate").description("true -> 비공개로 검색").optional(),
                                parameterWithName("category").description("검색할 카테고리 정보").optional(),
                                parameterWithName("order")
                                        .description("기본값 NEWER(생성 최신순) / ORDER(생성 과거순) / MANY_PEOPLE(많은 사람 순) / LESS_PEOPLE(적은 사람 순)"),
                                parameterWithName("keyword").description("검색 키워드").optional(),
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").description("페이지 사이즈")
                        ),
                        responseFields(
                                fieldWithPath("communities").type(JsonFieldType.ARRAY)
                                        .description("커뮤니티 정보"),
                                fieldWithPath("communities[].id").type(JsonFieldType.NUMBER)
                                        .description("커뮤니티 ID"),
                                fieldWithPath("communities[].name").type(JsonFieldType.STRING)
                                        .description("커뮤니티 이름"),
                                fieldWithPath("communities[].description").type(JsonFieldType.STRING)
                                        .description("커뮤니티 소개란"),
                                fieldWithPath("communities[].createdAt").type(JsonFieldType.STRING)
                                        .description("커뮤니티 생성시각"),
                                fieldWithPath("communities[].hashtags").type(JsonFieldType.ARRAY)
                                        .description("커뮤니티 해시태그 목록").optional(),
                                fieldWithPath("communities[].memberCount").type(JsonFieldType.NUMBER)
                                        .description("커뮤니티 멤버 수"),
                                fieldWithPath("communities[].category").type(JsonFieldType.STRING)
                                        .description("커뮤니티 카테고리"),
                                fieldWithPath("communities[].isPrivate").type(JsonFieldType.BOOLEAN)
                                        .description("true -> 커뮤니티가 비공개 커뮤니티"),
                                fieldWithPath("communities").type(JsonFieldType.ARRAY)
                                        .description("커뮤니티 정보"),

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
    @DisplayName("커뮤니티에 가입된 모든 멤버 목록 조회")
    class GetMemberAll {
        @Test
        @DisplayName("해당 커뮤니티에 가입된 모든 멤버 가져오기")
        void getMembersAllSuccess() throws Exception {
            UserBasicProfileDto userDto = new UserBasicProfileDto(1L, "url", "#0001", "유저");
            MemberDto memberDto = new MemberDto(1L, MemberType.MANAGER, userDto);

            given(memberQueryService.getJoinedMembersAll(anyLong(), anyLong()))
                    .willReturn(List.of(memberDto));

            final ResultActions result = mvc.perform(
                    get("/api/communities/{communityId}/members/all", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            result
                    .andExpect(status().isOk())
                    .andDo(document("communities/get-communityId-members-all",
                            pathParameters(
                                    parameterWithName("communityId").description("커뮤니티 ID")
                            ),
                            responseFields(
                                    fieldWithPath("members").type(JsonFieldType.ARRAY)
                                            .description("가입한 멤버 정보"),
                                    fieldWithPath("members[].id").type(JsonFieldType.NUMBER)
                                            .description("멤버 ID"),
                                    fieldWithPath("members[].type").type(JsonFieldType.STRING)
                                            .description("멤버 타입"),

                                    //todo: UserBasicProfile extract하기
                                    fieldWithPath("members[].user.id").type(JsonFieldType.NUMBER)
                                            .description("유저의 ID"),
                                    fieldWithPath("members[].user.profileImageUrl").type(JsonFieldType.STRING)
                                            .description("프로필이미지 경로").optional(),
                                    fieldWithPath("members[].user.name").type(JsonFieldType.STRING)
                                            .description("유저의 이름"),
                                    fieldWithPath("members[].user.tagNum").type(JsonFieldType.STRING)
                                            .description("태그번호")
                            )
                    ));
        }

        @Test
        @DisplayName("존재하지 않는 커뮤니티 ID로 요청한 경우 CommunityNotFoundException 발생")
        void notExistCommunityFail() throws Exception {
            doThrow(new CommunityNotFoundException())
                    .when(memberQueryService).getJoinedMembersAll(anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    get("/api/communities/{communityId}/members/all", 9999L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/get-communityId-members-all-CommunityNotFoundException"));
        }

        @Test
        @DisplayName("해당 커뮤니티에 가입되지 않는 경우 NotJoinedMemberException 발생")
        void notMemberFail() throws Exception {
            doThrow(new NotJoinedMemberException())
                    .when(memberQueryService).getJoinedMembersAll(anyLong(), anyLong());

            final ResultActions result = mvc.perform(
                    get("/api/communities/{communityId}/members/all", 1L)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("communities/get-communityId-members-all-NotJoinedMemberException"));
        }
    }
}