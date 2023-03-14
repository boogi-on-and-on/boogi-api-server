package boogi.apiserver.domain.community.community.api;

import boogi.apiserver.builder.*;
import boogi.apiserver.domain.community.community.application.CommunityCommandService;
import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.domain.CommunityCategory;
import boogi.apiserver.domain.community.community.dto.dto.*;
import boogi.apiserver.domain.community.community.dto.request.CommunitySettingRequest;
import boogi.apiserver.domain.community.community.dto.request.CreateCommunityRequest;
import boogi.apiserver.domain.community.community.dto.request.UpdateCommunityRequest;
import boogi.apiserver.domain.community.community.dto.response.CommunityDetailResponse;
import boogi.apiserver.domain.community.community.dto.response.CommunityPostsResponse;
import boogi.apiserver.domain.community.joinrequest.application.JoinRequestCommandService;
import boogi.apiserver.domain.community.joinrequest.application.JoinRequestQueryService;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.member.application.MemberCommandService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.dto.dto.BannedMemberDto;
import boogi.apiserver.domain.member.dto.dto.MemberDto;
import boogi.apiserver.domain.member.dto.response.JoinedMembersPageResponse;
import boogi.apiserver.domain.notice.application.NoticeQueryService;
import boogi.apiserver.domain.notice.dto.dto.NoticeDto;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.dto.dto.LatestCommunityPostDto;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import boogi.apiserver.domain.user.dto.response.UserDetailInfoDto;
import boogi.apiserver.global.constant.HeaderConst;
import boogi.apiserver.global.dto.PaginationDto;
import boogi.apiserver.global.webclient.push.SendPushNotification;
import boogi.apiserver.utils.TestTimeReflection;
import boogi.apiserver.utils.controller.MockHttpSessionCreator;
import boogi.apiserver.utils.controller.TestControllerSetUp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static boogi.apiserver.domain.post.postmedia.domain.MediaType.IMG;
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
    SendPushNotification sendPushNotification;

    @Test
    @DisplayName("커뮤니티 생성")
    void createCommunity() throws Exception {
        //given
        CreateCommunityRequest request = new CreateCommunityRequest("커뮤니티", "CLUB", "A".repeat(10),
                List.of("해시테그1", "해시테그1"), false, true);

        final Community community = TestCommunity.builder().id(1L).build();

        given(communityCommandService.createCommunity(any(), anyLong())).willReturn(community.getId());

        //when
        final ResultActions response = mvc.perform(RestDocumentationRequestBuilders
                .post("/api/communities")
                .content(mapper.writeValueAsBytes(request))
                .contentType(MediaType.APPLICATION_JSON)
                .session(MockHttpSessionCreator.dummySession())
                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
        );

        //then
        response
                .andExpect(status().isCreated())
                .andDo(document("communities/post",
                        requestFields(
                                fieldWithPath("name")
                                        .type(JsonFieldType.STRING)
                                        .description("커뮤니티 이름"),

                                fieldWithPath("category")
                                        .type(JsonFieldType.STRING)
                                        .description("커뮤니티 카테고리"),

                                fieldWithPath("description")
                                        .type(JsonFieldType.STRING)
                                        .description("커뮤니티 소개"),

                                fieldWithPath("hashtags")
                                        .type(JsonFieldType.ARRAY)
                                        .description("커뮤니티 해시태그"),

                                fieldWithPath("isPrivate")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("true -> 커뮤니티 공개"),

                                fieldWithPath("autoApproval")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("true -> 커뮤니티 가입 자동 승인")
                        ),

                        responseFields(
                                fieldWithPath("id")
                                        .type(JsonFieldType.NUMBER)
                                        .description("생성된 커뮤니티 ID")
                        )
                ));
    }

    @Test
    @DisplayName("커뮤니티 상세조회 with 글/게시글 목록")
    void getCommunityDetail() throws Exception {
        final CommunityDetailInfoDto communityDetailInfoDto = new CommunityDetailInfoDto(true, CommunityCategory.ACADEMIC.toString(), "커뮤니티 이름", "소개",
                List.of("태그1"), "1", LocalDateTime.now());
        final NoticeDto noticeDto = new NoticeDto(1L, "공지", LocalDateTime.now());
        final LatestCommunityPostDto postDto = new LatestCommunityPostDto(1L, "글 내용", LocalDateTime.now());
        final CommunityDetailResponse dto = new CommunityDetailResponse(MemberType.MANAGER, communityDetailInfoDto, List.of(noticeDto), List.of(postDto));

        given(communityQueryService.getCommunityDetail(anyLong(), anyLong()))
                .willReturn(dto);


        final ResultActions response = mvc.perform(RestDocumentationRequestBuilders
                .get("/api/communities/{communityId}", 1L)
                .session(MockHttpSessionCreator.dummySession())
                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
        );

        response
                .andExpect(status().isOk())
                .andDo(document("communities/get-communityId",
                        pathParameters(
                                parameterWithName("communityId").description("커뮤니티 ID")
                        ),

                        responseFields(
                                fieldWithPath("sessionMemberType")
                                        .type(JsonFieldType.STRING)
                                        .description("요청한 유저의 멤버타입")
                                        .optional(),

                                fieldWithPath("community")
                                        .type(JsonFieldType.OBJECT)
                                        .description("커뮤니티 정보"),

                                fieldWithPath("community.isPrivated")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("true -> 비공개 커뮤니티"),

                                fieldWithPath("community.category")
                                        .type(JsonFieldType.STRING)
                                        .description("커뮤니티 카테고리"),

                                fieldWithPath("community.name")
                                        .type(JsonFieldType.STRING)
                                        .description("커뮤니티 이름"),

                                fieldWithPath("community.introduce")
                                        .type(JsonFieldType.STRING)
                                        .description("커뮤니티 소개"),

                                fieldWithPath("community.hashtags")
                                        .type(JsonFieldType.ARRAY)
                                        .description("커뮤니티 해시태그")
                                        .optional(),

                                fieldWithPath("community.memberCount")
                                        .type(JsonFieldType.STRING)
                                        .description("커뮤니티 멤버 수"),

                                fieldWithPath("community.createdAt")
                                        .type(JsonFieldType.STRING)
                                        .description("커뮤니티 생성시각"),

                                fieldWithPath("notices")
                                        .type(JsonFieldType.ARRAY)
                                        .description("커뮤니티의 공지사항 목록"),

                                fieldWithPath("notices[].id")
                                        .type(JsonFieldType.NUMBER)
                                        .description("공지사항 ID"),

                                fieldWithPath("notices[].title")
                                        .type(JsonFieldType.STRING)
                                        .description("공지사항 제목"),

                                fieldWithPath("notices[].createdAt")
                                        .type(JsonFieldType.STRING)
                                        .description("공지사항 생성시각"),

                                fieldWithPath("posts")
                                        .type(JsonFieldType.ARRAY)
                                        .description("커뮤니티의 최근 글의 목록")
                                        .optional(),

                                fieldWithPath("posts[].id")
                                        .type(JsonFieldType.NUMBER)
                                        .description("게시글 ID"),

                                fieldWithPath("posts[].content")
                                        .type(JsonFieldType.STRING)
                                        .description("게시글의 내용"),

                                fieldWithPath("posts[].createdAt")
                                        .type(JsonFieldType.STRING)
                                        .description("게시글의 생성시각")

                        )
                ));
    }

    @Test
    @DisplayName("기본 메타데이터 전달")
    void getMetadata() throws Exception {
        CommunityMetadataDto dto = new CommunityMetadataDto("이름", "소개", List.of("테그"));

        given(communityQueryService.getCommunityMetadata(anyLong(), anyLong()))
                .willReturn(dto);

        final ResultActions response = mvc.perform(RestDocumentationRequestBuilders
                .get("/api/communities/1/metadata")
                .session(MockHttpSessionCreator.dummySession())
                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
        );

        response
                .andExpect(status().isOk())
                .andDo(document("communities/get-communityId-metadata",
                        responseFields(
                                fieldWithPath("metadata")
                                        .type(JsonFieldType.OBJECT)
                                        .description("커뮤니티 메타데이터"),

                                fieldWithPath("metadata.name")
                                        .type(JsonFieldType.STRING)
                                        .description("커뮤니티 이름"),

                                fieldWithPath("metadata.introduce")
                                        .type(JsonFieldType.STRING)
                                        .description("커뮤니티 소개"),

                                fieldWithPath("metadata.hashtags")
                                        .type(JsonFieldType.ARRAY)
                                        .description("커뮤니티 해시태그")
                        )
                ));
    }


    @Test
    @DisplayName("커뮤니티 업데이트 성공")
    void updateSuccess() throws Exception {
        UpdateCommunityRequest request = new UpdateCommunityRequest("@1fasdfadsfasdf3", List.of("t1", "t2", "t3", "t4"));

        final ResultActions response = mvc.perform(RestDocumentationRequestBuilders
                .patch("/api/communities/{communityId}", 1L)
                .session(MockHttpSessionCreator.dummySession())
                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        );

        response
                .andExpect(status().isOk())
                .andDo(document("communities/patch-communityId",
                        pathParameters(
                                parameterWithName("communityId").description("커뮤니티 ID")
                        ),

                        requestFields(
                                fieldWithPath("description")
                                        .type(JsonFieldType.STRING)
                                        .description("커뮤니티 소개란"),

                                fieldWithPath("hashtags")
                                        .type(JsonFieldType.ARRAY)
                                        .description("커뮤니티 해시테그")
                                        .optional()
                        )
                ));

    }

    @Test
    @DisplayName("커뮤니티 폐쇄")
    void communityShutdown() throws Exception {
        final ResultActions response = mvc.perform(RestDocumentationRequestBuilders
                .delete("/api/communities/{communityId}", 1L)
                .session(MockHttpSessionCreator.dummySession())
                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
        );

        response
                .andExpect(status().isOk())
                .andDo(document("communities/delete-communityId",
                        pathParameters(
                                parameterWithName("communityId").description("커뮤니티 ID")
                        )
                ));
    }

    @Nested
    @DisplayName("커뮤니티 설정 테스트")
    class CommunitySettingTest {

        @Test
        @DisplayName("설정정보 조회")
        void getSettingInfo() throws Exception {
            final Community community = TestCommunity.builder()
                    .isPrivate(true)
                    .autoApproval(true)
                    .build();

            CommunitySettingInfoDto settingInfo = CommunitySettingInfoDto.of(community);

            given(communityQueryService.getSetting(anyLong(), anyLong())).willReturn(settingInfo);

            final ResultActions response = mvc.perform(RestDocumentationRequestBuilders
                    .get("/api/communities/{communityId}/settings", 1L)
                    .session(MockHttpSessionCreator.dummySession())
                    .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                    .contentType(MediaType.APPLICATION_JSON)
            );

            response
                    .andExpect(status().isOk())
                    .andDo(document("communities/get-communityId-settings",
                            pathParameters(
                                    parameterWithName("communityId").description("커뮤니티 ID")
                            ),
                            responseFields(
                                    fieldWithPath("settingInfo")
                                            .type(JsonFieldType.OBJECT)
                                            .description("커뮤니티 설정 정보"),

                                    fieldWithPath("settingInfo.isAuto")
                                            .type(JsonFieldType.BOOLEAN)
                                            .description("true -> 자동 가입 커뮤니티로 전환"),

                                    fieldWithPath("settingInfo.isSecret")
                                            .type(JsonFieldType.BOOLEAN)
                                            .description("true -> 비공개 커뮤니티로 전환")
                            )
                    ));
        }

        @Test
        @DisplayName("설정정보 수정")
        void postSettingCommunity() throws Exception {
            final CommunitySettingRequest request = new CommunitySettingRequest(true, true);

            final ResultActions response = mvc.perform(RestDocumentationRequestBuilders
                    .post("/api/communities/{communityId}/settings", 1L)
                    .session(MockHttpSessionCreator.dummySession())
                    .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request))
            );

            response
                    .andExpect(status().isOk())
                    .andDo(document("communities/post-communityId-settings",
                            pathParameters(
                                    parameterWithName("communityId").description("커뮤니티 ID")
                            ),

                            requestFields(
                                    fieldWithPath("isSecret")
                                            .type(JsonFieldType.BOOLEAN)
                                            .description("true -> 비공개 커뮤니티로 전환"),

                                    fieldWithPath("isAutoApproval")
                                            .type(JsonFieldType.BOOLEAN)
                                            .description("true -> 자동 가입 커뮤니티로 전환")
                            )
                    ));
        }
    }


    @Test
    @DisplayName("커뮤니티 게시글 목록 조회")
    void getCommunityPostList() throws Exception {
        given(postQueryService.getPostsOfCommunity(any(), anyLong(), anyLong()))
                .willReturn(setUpGetCommunityWithPostId());

        final ResultActions response = mvc.perform(RestDocumentationRequestBuilders
                .get("/api/communities/{communityId}/posts", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                .session(MockHttpSessionCreator.dummySession())
                .queryParam("page", "0")
                .queryParam("size", "3")
        );

        response
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
                                fieldWithPath("communityName")
                                        .type(JsonFieldType.STRING)
                                        .description("커뮤니티 이름"),

                                fieldWithPath("memberType")
                                        .type(JsonFieldType.STRING)
                                        .description("멤버의 타입"),

                                fieldWithPath("posts")
                                        .type(JsonFieldType.ARRAY)
                                        .description("커뮤니티의 게시글 목록"),

                                fieldWithPath("posts[].id")
                                        .type(JsonFieldType.NUMBER)
                                        .description("게시글 ID"),

                                fieldWithPath("posts[].createdAt")
                                        .type(JsonFieldType.STRING)
                                        .description("게시글 생성시각"),

                                fieldWithPath("posts[].content")
                                        .type(JsonFieldType.STRING)
                                        .description("게시글 내용"),

                                fieldWithPath("posts[].hashtags")
                                        .type(JsonFieldType.ARRAY)
                                        .description("해시태그")
                                        .optional(),

                                fieldWithPath("posts[].postMedias")
                                        .type(JsonFieldType.ARRAY)
                                        .description("게시글 미디어 정보")
                                        .optional(),

                                fieldWithPath("posts[].postMedias[].url")
                                        .type(JsonFieldType.STRING)
                                        .description("게시글 미디어 경로"),

                                fieldWithPath("posts[].postMedias[].type")
                                        .type(JsonFieldType.STRING)
                                        .description("게시글 미디어 타입"),

                                fieldWithPath("posts[].likeCount")
                                        .type(JsonFieldType.NUMBER)
                                        .description("게시글 좋아요 수"),

                                fieldWithPath("posts[].commentCount")
                                        .type(JsonFieldType.NUMBER)
                                        .description("게시글 댓글 수"),

                                fieldWithPath("posts[].me")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("true -> 내가 작성한 게시글"),

                                fieldWithPath("posts[].likeId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("좋아요 ID /요청한 유저가 좋아요 한 경우")
                                        .optional(),

                                fieldWithPath("posts[].user")
                                        .type(JsonFieldType.OBJECT)
                                        .description("게시글 작성자 정보"),

                                fieldWithPath("posts[].user.id")
                                        .type(JsonFieldType.NUMBER)
                                        .description("유저 ID"),

                                fieldWithPath("posts[].user.profileImageUrl")
                                        .type(JsonFieldType.STRING)
                                        .description("유저 프로필 경로")
                                        .optional(),

                                fieldWithPath("posts[].user.tagNum")
                                        .type(JsonFieldType.STRING)
                                        .description("태그번호"),

                                fieldWithPath("posts[].user.name")
                                        .type(JsonFieldType.STRING)
                                        .description("이름"),

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
    @DisplayName("커뮤니티 멤버 목록 페이지네이션")
    void communityMembersPagination() throws Exception {

        final UserDetailInfoDto userDto = new UserDetailInfoDto(1L, "url", "#0001", "name", "introduce", "department");
        final JoinedMemberInfoDto memberDto = new JoinedMemberInfoDto(1L, MemberType.SUB_MANAGER.toString(), LocalDateTime.now().toString(), userDto);

        final JoinedMembersPageResponse dto = new JoinedMembersPageResponse(List.of(memberDto), new PaginationDto(1, false));
        given(memberQueryService.getCommunityJoinedMembers(any(), anyLong()))
                .willReturn(dto);

        final ResultActions response = mvc.perform(RestDocumentationRequestBuilders
                .get("/api/communities/{communityId}/members", 1L)
                .session(MockHttpSessionCreator.dummySession())
                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
        );

        response
                .andExpect(status().isOk())
                .andDo(document("communities/get-communityId-members",
                        pathParameters(
                                parameterWithName("communityId").description("커뮤니티 ID")
                        ),
                        responseFields(
                                fieldWithPath("members")
                                        .type(JsonFieldType.ARRAY)
                                        .description("가입한 멤버 목록"),

                                fieldWithPath("members[].id")
                                        .type(JsonFieldType.NUMBER)
                                        .description("멤버 아이디"),

                                fieldWithPath("members[].memberType")
                                        .type(JsonFieldType.STRING)
                                        .description("멤버 타입"),

                                fieldWithPath("members[].createdAt")
                                        .type(JsonFieldType.STRING)
                                        .description("커뮤니티 가입일"),

                                //todo: UserDetailInfoDto 공통화하기
                                fieldWithPath("members[].user.id")
                                        .type(JsonFieldType.NUMBER)
                                        .description("유저의 ID"),

                                fieldWithPath("members[].user.profileImageUrl")
                                        .type(JsonFieldType.STRING)
                                        .description("프로필이미지 경로")
                                        .optional(),

                                fieldWithPath("members[].user.name")
                                        .type(JsonFieldType.STRING)
                                        .description("유저의 이름"),

                                fieldWithPath("members[].user.tagNum")
                                        .type(JsonFieldType.STRING)
                                        .description("태그번호"),

                                fieldWithPath("members[].user.introduce")
                                        .type(JsonFieldType.STRING)
                                        .description("자기소개"),

                                fieldWithPath("members[].user.department")
                                        .type(JsonFieldType.STRING)
                                        .description("학과"),

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
    @DisplayName("차단된 멤버 목록 조회")
    void getBlockedMemberList() throws Exception {
        UserBasicProfileDto user = new UserBasicProfileDto(2L, "adsf", "#0001", "홍길동");
        BannedMemberDto dto = new BannedMemberDto(1L, user);

        given(memberQueryService.getBannedMembers(anyLong(), anyLong()))
                .willReturn(List.of(dto));

        final ResultActions response = mvc.perform(RestDocumentationRequestBuilders
                .get("/api/communities/{communityId}/members/banned", 1L)
                .session(MockHttpSessionCreator.dummySession())
                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
        );

        response
                .andExpect(status().isOk())
                .andDo(document("communities/get-communityId-members-banned",
                        pathParameters(
                                parameterWithName("communityId").description("커뮤니티 ID")
                        ),

                        responseFields(
                                fieldWithPath("banned")
                                        .type(JsonFieldType.ARRAY)
                                        .description("차단된 멤버 목록"),

                                fieldWithPath("banned[].memberId")
                                        .type(JsonFieldType.NUMBER)
                                        .description("멤버 아이디"),

                                //todo: UserBasicProfile 공통화하기
                                fieldWithPath("banned[].user.id")
                                        .type(JsonFieldType.NUMBER)
                                        .description("유저의 ID"),

                                fieldWithPath("banned[].user.profileImageUrl")
                                        .type(JsonFieldType.STRING)
                                        .description("프로필이미지 경로")
                                        .optional(),

                                fieldWithPath("banned[].user.name")
                                        .type(JsonFieldType.STRING)
                                        .description("유저의 이름"),

                                fieldWithPath("banned[].user.tagNum")
                                        .type(JsonFieldType.STRING)
                                        .description("태그번호")
                        )
                ));
    }

    @Nested
    @DisplayName("커뮤니티 가입요청 테스트")
    class JoinRequestTest {
        @Test
        @DisplayName("관리자의 가입요청목록 조회")
        void getJoinRequestList() throws Exception {
            final User user = TestUser.builder()
                    .id(1L)
                    .username("홍길동")
                    .tagNumber("#0001")
                    .build();

            given(joinRequestQueryService.getAllRequests(anyLong(), anyLong()))
                    .willReturn(List.of(
                            UserJoinRequestInfoDto.of(user, 2L)
                    ));

            final ResultActions response = mvc.perform(RestDocumentationRequestBuilders
                    .get("/api/communities/{communityId}/requests", 1L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .session(MockHttpSessionCreator.dummySession())
                    .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
            );

            response
                    .andExpect(status().isOk())
                    .andDo(document("communities/get-communityId-requests",
                            pathParameters(
                                    parameterWithName("communityId").description("커뮤니티 ID")
                            ),
                            responseFields(
                                    fieldWithPath("requests[].id")
                                            .type(JsonFieldType.NUMBER)
                                            .description("가입요청 ID"),

                                    fieldWithPath("requests[].user")
                                            .type(JsonFieldType.OBJECT)
                                            .description("가입요청한 유저 정보"),

                                    //todo: UserBasicProfile extract하기
                                    fieldWithPath("requests[].user.id")
                                            .type(JsonFieldType.NUMBER)
                                            .description("유저의 ID"),

                                    fieldWithPath("requests[].user.profileImageUrl")
                                            .type(JsonFieldType.STRING)
                                            .description("프로필이미지 경로")
                                            .optional(),

                                    fieldWithPath("requests[].user.name")
                                            .type(JsonFieldType.STRING)
                                            .description("유저의 이름"),

                                    fieldWithPath("requests[].user.tagNum")
                                            .type(JsonFieldType.STRING)
                                            .description("태그번호")
                            )
                    ));
        }

        @Test
        @DisplayName("가입요청 성공")
        void applySuccess() throws Exception {
            given(joinRequestCommandService.request(anyLong(), anyLong()))
                    .willReturn(1L);

            final ResultActions response = mvc.perform(RestDocumentationRequestBuilders
                    .post("/api/communities/{communityId}/requests", 1L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .session(MockHttpSessionCreator.dummySession())
                    .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
            );

            response
                    .andExpect(status().isOk())
                    .andDo(document("communities/post-communityId-requests",
                            pathParameters(
                                    parameterWithName("communityId").description("커뮤니티 ID")
                            ),
                            responseFields(
                                    fieldWithPath("id")
                                            .type(JsonFieldType.NUMBER)
                                            .description("가입요청 ID")

                            )
                    ));

        }

        @Test
        @DisplayName("관리자가 가입요청 컨펌 성공")
        void confirm() throws Exception {
            final ResultActions response = mvc.perform(RestDocumentationRequestBuilders
                    .post("/api/communities/{communityId}/requests/confirm", 1L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                    .session(MockHttpSessionCreator.dummySession())
                    .content(mapper.writeValueAsString(Map.of("requestIds", List.of(1L, 2L))))
            );

            then(sendPushNotification).should(times(1))
                    .joinNotification(List.of(1L, 2L));

            response
                    .andExpect(status().isOk())
                    .andDo(document("communities/post-communityId-requests-confirm",
                            pathParameters(
                                    parameterWithName("communityId").description("커뮤니티 ID")
                            ),

                            requestFields(
                                    fieldWithPath("requestIds")
                                            .type(JsonFieldType.ARRAY)
                                            .description("가입요청 ID 목록")
                            )
                    ));
        }

        @Test
        @DisplayName("관리자가 가입요청 거절 성공")
        void reject() throws Exception {
            final ResultActions response = mvc.perform(RestDocumentationRequestBuilders
                    .post("/api/communities/{communityId}/requests/reject", 1L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                    .session(MockHttpSessionCreator.dummySession())
                    .content(mapper.writeValueAsString(Map.of("requestIds", List.of(1L, 2L))))
            );

            then(sendPushNotification).should(times(1))
                    .rejectNotification(List.of(1L, 2L));

            response
                    .andExpect(status().isOk())
                    .andDo(document("communities/post-communityId-requests-reject",
                            pathParameters(
                                    parameterWithName("communityId").description("커뮤니티 ID")
                            ),

                            requestFields(
                                    fieldWithPath("requestIds")
                                            .type(JsonFieldType.ARRAY)
                                            .description("가입요청 ID 목록")
                            )
                    ));
        }
    }

    @Test
    @DisplayName("커뮤니티 검색하기")
    void searchCommunity() throws Exception {
        List<String> hashtags = List.of("안녕", "헤헤");
        SearchCommunityDto dto = new SearchCommunityDto(1L, "커뮤니티1", "소개", LocalDateTime.now(),
                hashtags, 23, "HOBBY", false);

        given(communityQueryService.getSearchedCommunities(any(), any()))
                .willReturn(new PageImpl<>(List.of(dto), Pageable.ofSize(1), 1));

        final ResultActions response = mvc.perform(RestDocumentationRequestBuilders
                .get("/api/communities/search")
                .session(MockHttpSessionCreator.dummySession())
                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                .contentType(MediaType.APPLICATION_JSON)
                .queryParam("isPrivate", "FALSE")
                .queryParam("order", "OLDER")
                .queryParam("category", "HOBBY")
                .queryParam("keyword", "안녕")
                .queryParam("page", "0")
                .queryParam("size", "10")
        );

        response
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
                                fieldWithPath("communities")
                                        .type(JsonFieldType.ARRAY)
                                        .description("커뮤니티 정보"),

                                fieldWithPath("communities[].id")
                                        .type(JsonFieldType.NUMBER)
                                        .description("커뮤니티 ID"),

                                fieldWithPath("communities[].name")
                                        .type(JsonFieldType.STRING)
                                        .description("커뮤니티 이름"),

                                fieldWithPath("communities[].description")
                                        .type(JsonFieldType.STRING)
                                        .description("커뮤니티 소개란"),

                                fieldWithPath("communities[].createdAt")
                                        .type(JsonFieldType.STRING)
                                        .description("커뮤니티 생성시각"),

                                fieldWithPath("communities[].hashtags")
                                        .type(JsonFieldType.ARRAY)
                                        .description("커뮤니티 해시태그 목록")
                                        .optional(),

                                fieldWithPath("communities[].memberCount")
                                        .type(JsonFieldType.NUMBER)
                                        .description("커뮤니티 멤버 수"),

                                fieldWithPath("communities[].category")
                                        .type(JsonFieldType.STRING)
                                        .description("커뮤니티 카테고리"),

                                fieldWithPath("communities[].isPrivate")
                                        .type(JsonFieldType.BOOLEAN)
                                        .description("true -> 커뮤니티가 비공개 커뮤니티"),

                                fieldWithPath("communities")
                                        .type(JsonFieldType.ARRAY)
                                        .description("커뮤니티 정보"),

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
    @DisplayName("해당 커뮤니티에 가입된 모든 멤버 가져오기")
    void testGetMembersAll() throws Exception {
        final UserBasicProfileDto profile = new UserBasicProfileDto(1L, "url", "#0001", "이름");
        final MemberDto memberDto = new MemberDto(1L, MemberType.MANAGER, profile);
        given(memberQueryService.getJoinedMembersAll(anyLong(), anyLong()))
                .willReturn(List.of(memberDto));

        final ResultActions response = mvc.perform(RestDocumentationRequestBuilders
                .get("/api/communities/{communityId}/members/all", 1L)
                .session(MockHttpSessionCreator.dummySession())
                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                .contentType(MediaType.APPLICATION_JSON)
        );

        response
                .andExpect(status().isOk())
                .andDo(document("communities/get-communityId-members-all",
                        pathParameters(
                                parameterWithName("communityId").description("커뮤니티 ID")
                        ),
                        responseFields(
                                fieldWithPath("members")
                                        .type(JsonFieldType.ARRAY)
                                        .description("가입한 멤버 정보"),

                                fieldWithPath("members[].id")
                                        .type(JsonFieldType.NUMBER)
                                        .description("멤버 ID"),

                                fieldWithPath("members[].type")
                                        .type(JsonFieldType.STRING)
                                        .description("멤버 타입"),

                                //todo: UserBasicProfile extract하기
                                fieldWithPath("members[].user.id")
                                        .type(JsonFieldType.NUMBER)
                                        .description("유저의 ID"),

                                fieldWithPath("members[].user.profileImageUrl")
                                        .type(JsonFieldType.STRING)
                                        .description("프로필이미지 경로")
                                        .optional(),

                                fieldWithPath("members[].user.name")
                                        .type(JsonFieldType.STRING)
                                        .description("유저의 이름"),

                                fieldWithPath("members[].user.tagNum")
                                        .type(JsonFieldType.STRING)
                                        .description("태그번호")
                        )
                ));
    }


    private CommunityPostsResponse setUpGetCommunityWithPostId() {
        final Community community = TestCommunity.builder()
                .communityName("커뮤니티")
                .build();
        TestTimeReflection.setCreatedAt(community, LocalDateTime.now());

        final User user = TestUser.builder()
                .id(1L)
                .username("홍길동")
                .tagNumber("#0001")
                .build();

        final Member member = Member.builder()
                .memberType(MemberType.NORMAL)
                .user(user)
                .build();

        final PostHashtag postHashtag1 = TestPostHashtag.builder()
                .tag("tag")
                .build();

        final PostMedia postMedia = TestPostMedia.builder()
                .mediaURL("123")
                .mediaType(IMG)
                .build();

        final Post post = TestPost.builder()
                .id(1L)
                .content("A".repeat(10))
                .likeCount(1)
                .postMedias(List.of(postMedia))
                .commentCount(1)
                .member(member)
                .community(community)
                .hashtags(List.of(postHashtag1))
                .build();
        TestTimeReflection.setCreatedAt(post, LocalDateTime.now());

        PageImpl<Post> page = new PageImpl(List.of(post), Pageable.ofSize(1), 1);

        return CommunityPostsResponse.of("커뮤니티 이름", 1L, page, member);
    }
}