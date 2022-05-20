package boogi.apiserver.domain.community.community.api;

import boogi.apiserver.domain.community.community.application.CommunityCoreService;
import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.domain.CommunityCategory;
import boogi.apiserver.domain.community.community.dto.*;
import boogi.apiserver.domain.community.community.exception.AlreadyExistsCommunityNameException;
import boogi.apiserver.domain.community.joinrequest.application.JoinRequestCoreService;
import boogi.apiserver.domain.community.joinrequest.application.JoinRequestQueryService;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;
import boogi.apiserver.domain.hashtag.community.domain.CommunityHashtag;
import boogi.apiserver.domain.member.application.MemberCoreService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.application.MemberValidationService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.dto.BannedMemberDto;
import boogi.apiserver.domain.member.exception.NotAuthorizedMemberException;
import boogi.apiserver.domain.notice.application.NoticeQueryService;
import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.UserBasicProfileDto;
import boogi.apiserver.global.constant.HeaderConst;
import boogi.apiserver.global.constant.SessionInfoConst;
import boogi.apiserver.global.webclient.push.SendPushNotification;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = CommunityApiController.class)
class CommunityApiControllerTest {

    @MockBean
    JoinRequestCoreService joinRequestCoreService;

    @MockBean
    CommunityCoreService communityCoreService;

    @MockBean
    MemberCoreService memberCoreService;

    @MockBean
    MemberValidationService memberValidationService;

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
    SendPushNotification sendPushNotification;


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

    @Test
    void 커뮤니티_생성_성공() throws Exception {

        //given
        List<String> hashtags = List.of("해시테그1", "해시테그1");
        CreateCommunityRequest request = CreateCommunityRequest.builder()
                .name("커뮤니티1")
                .category("동아리")
                .description("설명")
                .autoApproval(true)
                .isPrivate(false)
                .hashtags(hashtags)
                .build();

        Community community = Community.builder()
                .id(1L)
                .build();

        given(communityCoreService.createCommunity(any(), any(), anyLong())).willReturn(community);


        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        //when, then
        mvc.perform(
                        MockMvcRequestBuilders.post("/api/communities")
                                .content(mapper.writeValueAsBytes(request))
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.communityId").value("1"));
    }

    @Test
    void 커뮤니티_생성_이미_동일한_이름() throws Exception {

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        List<String> hashtags = List.of("해시테그1", "해시테그1");
        CreateCommunityRequest request = CreateCommunityRequest.builder()
                .name("커뮤니티1")
                .category("동아리")
                .description("설명")
                .autoApproval(true)
                .isPrivate(false)
                .hashtags(hashtags)
                .build();

        given(communityCoreService.createCommunity(any(), any(), anyLong())).willThrow(new AlreadyExistsCommunityNameException());

        mvc.perform(
                        MockMvcRequestBuilders.post("/api/communities")
                                .content(mapper.writeValueAsBytes(request))
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                )
                .andExpect(jsonPath("$.message").value("이미 해당 커뮤니티 이름이 존재합니다."));
    }

    @Test
    void 커뮤니티_상세조회_글목록_보여주는_경우() throws Exception {
        Member member = Member.builder().build();
        given(memberQueryService.getMemberOfTheCommunity(anyLong(), anyLong()))
                .willReturn(member);

        Community community = Community.builder()
                .id(1L)
                .communityName("커뮤니티1")
                .description("반가워")
                .isPrivate(false)
                .hashtags(List.of(CommunityHashtag.builder().tag("테그1").build()))
                .memberCount(3)
                .category(CommunityCategory.ACADEMIC)
                .build();
        community.setCreatedAt(LocalDateTime.now());

        given(communityQueryService.getCommunityWithHashTag(anyLong()))
                .willReturn(community);

        Notice notice = Notice.builder()
                .id(1L)
                .title("노티스")
                .build();
        notice.setCreatedAt(LocalDateTime.now());

        given(noticeQueryService.getCommunityLatestNotice(anyLong()))
                .willReturn(List.of(notice));

        Post post = Post.builder()
                .id(4L)
                .content("글")
                .build();
        post.setCreatedAt(LocalDateTime.now());

        given(postQueryService.getLatestPostOfCommunity(anyLong()))
                .willReturn(List.of(post));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        //when
        mvc.perform(
                        MockMvcRequestBuilders.get("/api/communities/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")

                )
                .andExpect(jsonPath("$.isJoined").value(true))
                .andExpect(jsonPath("$.community.isPrivated").value(false))
                .andExpect(jsonPath("$.community.name").value("커뮤니티1"))
                .andExpect(jsonPath("$.community.introduce").value("반가워"))
                .andExpect(jsonPath("$.community.hashtags[0]").value("테그1"))
                .andExpect(jsonPath("$.community.memberCount").value("3"))
                .andExpect(jsonPath("$.posts[0].id").value(4))
                .andExpect(jsonPath("$.posts[0].content").value("글"))
                .andExpect(jsonPath("$.notices[0].id").value(1))
                .andExpect(jsonPath("$.notices[0].title").value("노티스"));
    }

    @Test
    void 커뮤니티_상세조회_글목록_안보여주는_경우() throws Exception {
        given(memberQueryService.getMemberOfTheCommunity(anyLong(), anyLong()))
                .willReturn(null);

        Community community = Community.builder()
                .id(1L)
                .communityName("커뮤니티1")
                .description("반가워")
                .isPrivate(true)
                .hashtags(List.of(CommunityHashtag.builder().tag("테그1").build()))
                .memberCount(3)
                .category(CommunityCategory.ACADEMIC)
                .build();
        community.setCreatedAt(LocalDateTime.now());

        given(communityQueryService.getCommunityWithHashTag(anyLong()))
                .willReturn(community);

        Notice notice = Notice.builder()
                .id(1L)
                .title("노티스")
                .build();
        notice.setCreatedAt(LocalDateTime.now());

        given(noticeQueryService.getCommunityLatestNotice(anyLong()))
                .willReturn(List.of(notice));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        //when
        mvc.perform(
                        MockMvcRequestBuilders.get("/api/communities/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")

                )
                .andExpect(jsonPath("$.isJoined").value(false))
                .andExpect(jsonPath("$.notices").isArray())
                .andExpect(jsonPath("$.community").isMap())
                .andExpect(jsonPath("$.posts").doesNotExist());
    }

    @Test
    void 가입요청_조회_권한_없을때() throws Exception {

        given(memberValidationService.hasAuth(anyLong(), anyLong(), any()))
                .willThrow(new NotAuthorizedMemberException());

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                        MockMvcRequestBuilders.get("/api/communities/1/requests")
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                )
                .andExpect(jsonPath("$.code").value("MEMBER_002"));
    }

    @Test
    void 가입요청_조회_성공() throws Exception {

        JoinRequest request = JoinRequest.builder()
                .id(2L)
                .user(User.builder()
                        .id(1L)
                        .tagNumber("#0001")
                        .username("홍길동")
                        .build())
                .build();

        given(joinRequestQueryService.getAllRequests(anyLong()))
                .willReturn(List.of(request));


        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                        MockMvcRequestBuilders.get("/api/communities/1/requests")
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                )
                .andExpect(jsonPath("$.requests[0].id").value(2))
                .andExpect(jsonPath("$.requests[0].user.tagNum").value("#0001"))
                .andExpect(jsonPath("$.requests[0].user.id").value(1))
                .andExpect(jsonPath("$.requests[0].user.name").value("홍길동"))
                .andExpect(jsonPath("$.requests[0].user.profileImageUrl").doesNotExist());
    }

    @Test
    void 가입요청_성공() throws Exception {

        given(joinRequestCoreService.request(anyLong(), anyLong()))
                .willReturn(1L);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                MockMvcRequestBuilders.post("/api/communities/1/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .session(session)
                        .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
        ).andExpect(jsonPath("$.requestId").value(1));

    }

    @Test
    void 가입_컨펌하기() throws Exception {

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                MockMvcRequestBuilders.post("/api/communities/1/requests/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                        .session(session)
                        .content(mapper.writeValueAsString(Map.of("requestIds", List.of(1L, 2L))))
        ).andExpect(status().isOk());
    }

    //todo: pagnation controller 테스트 방법 (CommunityApiController.getMembers())


    @Test
    void 커뮤니티_게시글_목록() throws Exception {
        Community community = Community.builder()
                .communityName("커뮤니티1")
                .build();
        given(communityQueryService.getCommunity(anyLong()))
                .willReturn(community);

        User user = User.builder().id(2L)
                .username("홍길동")
                .tagNumber("#0001")
                .build();

        Member member = Member.builder()
                .memberType(MemberType.MANAGER)
                .user(user)
                .build();
        given(memberQueryService.getMemberOfTheCommunity(anyLong(), anyLong()))
                .willReturn(member);

        Post post = Post.builder()
                .id(1L)
                .content("내용1")
                .likeCount(2)
                .commentCount(3)
                .community(community)
                .member(member)
                .build();

        post.setCreatedAt(LocalDateTime.now());

        PageImpl<Post> page = new PageImpl(List.of(post), Pageable.ofSize(1), 1);
        given(postQueryService.getPostsOfCommunity(any(), anyLong()))
                .willReturn(page);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                        MockMvcRequestBuilders.get("/api/communities/1/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                                .session(session)
                                .queryParam("page", "0")
                                .queryParam("size", "3")

                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.pageInfo.nextPage").value(1))
                .andExpect(jsonPath("$.pageInfo.totalCount").value(1))
                .andExpect(jsonPath("$.pageInfo.hasNext").value(false))
                .andExpect(jsonPath("$.posts.size()").value(1))
                .andExpect(jsonPath("$.posts[0].id").value(1L))
                .andExpect(jsonPath("$.posts[0].content").value("내용1"))
                .andExpect(jsonPath("$.posts[0].user.id").value(2L))
                .andExpect(jsonPath("$.posts[0].user.name").value("홍길동"))
                .andExpect(jsonPath("$.posts[0].user.tagNum").value("#0001"));
    }

    @Test
    void 커뮤니티_폐쇄() throws Exception {

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                MockMvcRequestBuilders.delete("/api/communities/1")
                        .session(session)
                        .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
        ).andExpect(status().isOk());
    }

    @Test
    void 차단된_멤버() throws Exception {
        BannedMemberDto dto = BannedMemberDto.builder()
                .memberId(1L)
                .user(UserBasicProfileDto.builder()
                        .id(2L)
                        .name("홍길동")
                        .tagNum("#0001")
                        .build())
                .build();

        given(memberQueryService.getBannedMembers(anyLong()))
                .willReturn(List.<BannedMemberDto>of(dto));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);


        mvc.perform(
                        MockMvcRequestBuilders.get("/api/communities/1/members/banned")
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.banned[0].memberId").value(1L))
                .andExpect(jsonPath("$.banned[0].user.id").value(2L));
    }

    @Test
    void 멤버_차단해제() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                MockMvcRequestBuilders.post("/api/communities/1/members/release")
                        .session(session)
                        .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("memberId", "1")))
        ).andExpect(status().isOk());
    }

    @Test
    void 멤버_권한주기() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        DelegateMemberRequest request = DelegateMemberRequest.builder()
                .memberId(1L)
                .type(MemberType.MANAGER)
                .build();

        mvc.perform(
                MockMvcRequestBuilders.post("/api/communities/1/members/delegate")
                        .session(session)
                        .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        ).andExpect(status().isOk());
    }

    @Test
    void 커뮤니티_설정() throws Exception {

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        CommunitySettingRequest request = CommunitySettingRequest.builder()
                .isAutoApproval(true)
                .isSecret(true)
                .build();

        mvc.perform(
                MockMvcRequestBuilders.post("/api/communities/1/settings")
                        .session(session)
                        .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        ).andExpect(status().isOk());
    }

    @Test
    void 커뮤니티_메타데이터_전달() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        CommunityMetadataDto dto = CommunityMetadataDto.builder()
                .introduce("소개")
                .name("이름")
                .hashtags(List.of("테그1"))
                .build();

        given(communityQueryService.getCommunityMetadata(anyLong()))
                .willReturn(dto);

        mvc.perform(
                        MockMvcRequestBuilders.get("/api/communities/1/metadata")
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.name").value("이름"))
                .andExpect(jsonPath("$.metadata.introduce").value("소개"))
                .andExpect(jsonPath("$.metadata.hashtags[0]").value("테그1"));
    }

    @Test
    void 커뮤니티_업데이트_소개란_없는경우() throws Exception {

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        CommunityUpdateRequest request = CommunityUpdateRequest.builder()
                .hashtags(List.of("t1"))
                .build();

        mvc.perform(
                        MockMvcRequestBuilders.patch("/api/communities/1")
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(request))
                ).andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value("COMMON_004"))
                .andExpect(jsonPath("$.message").value("커뮤니티 소개란을 입력해주세요."));
    }

    @Test
    void 커뮤니티_업데이트_소개란_10글자_미만() throws Exception {

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        CommunityUpdateRequest request = CommunityUpdateRequest.builder()
                .description("@13")
                .hashtags(List.of("t1"))
                .build();

        mvc.perform(
                        MockMvcRequestBuilders.patch("/api/communities/1")
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(request))
                ).andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value("COMMON_004"))
                .andExpect(jsonPath("$.message").value("10글자 이상 소개란을 입력해주세요."));
    }

    @Test
    void 커뮤니티_업데이트_해시테그_5개_초과() throws Exception {

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        CommunityUpdateRequest request = CommunityUpdateRequest.builder()
                .description("@1fasdfadsfasdf3")
                .hashtags(List.of("t1", "t2", "t3", "t4", "t5", "t6"))
                .build();

        mvc.perform(
                        MockMvcRequestBuilders.patch("/api/communities/1")
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(request))
                ).andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value("COMMON_004"))
                .andExpect(jsonPath("$.message").value("해시테그는 5개까지만 입력가능합니다."));
    }

    @Test
    void 커뮤니티_업데이트_성공() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        CommunityUpdateRequest request = CommunityUpdateRequest.builder()
                .description("@1fasdfadsfasdf3")
                .hashtags(List.of("t1", "t2", "t3", "t4"))
                .build();

        mvc.perform(
                MockMvcRequestBuilders.patch("/api/communities/1")
                        .session(session)
                        .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        ).andExpect(status().isOk());
    }

    @Test
    void 커뮤니티_검색하기() throws Exception {

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        SearchCommunityDto dto = SearchCommunityDto.builder()
                .category("INTEREST")
                .createdAt(LocalDateTime.now().toString())
                .id(1L)
                .hashtags(List.of("안녕", "헤헤"))
                .memberCount(23)
                .isPrivate(false)
                .name("커뮤니티1")
                .build();

        given(communityQueryService.getSearchedCommunities(any(), any()))
                .willReturn(new PageImpl<>(List.of(dto), Pageable.ofSize(1), 1));

        mvc.perform(
                        MockMvcRequestBuilders.get("/api/communities/search")
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .queryParam("isPrivate", "FALSE")
                                .queryParam("order", "NEWER")
                                .queryParam("category", "INTEREST")
                                .queryParam("keyword", "안녕")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.communities[0]").isMap());
    }
}