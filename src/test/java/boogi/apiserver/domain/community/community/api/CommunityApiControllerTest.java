package boogi.apiserver.domain.community.community.api;

import boogi.apiserver.domain.community.community.application.CommunityCoreService;
import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.domain.CommunityCategory;
import boogi.apiserver.domain.community.community.dto.request.CommunitySettingRequest;
import boogi.apiserver.domain.community.community.dto.request.CommunityUpdateRequest;
import boogi.apiserver.domain.community.community.dto.request.CreateCommunityRequest;
import boogi.apiserver.domain.community.community.dto.request.DelegateMemberRequest;
import boogi.apiserver.domain.community.community.dto.response.CommunityMetadataDto;
import boogi.apiserver.domain.community.community.dto.response.CommunitySettingInfo;
import boogi.apiserver.domain.community.community.dto.response.SearchCommunityDto;
import boogi.apiserver.domain.community.community.exception.AlreadyExistsCommunityNameException;
import boogi.apiserver.domain.community.joinrequest.application.JoinRequestCoreService;
import boogi.apiserver.domain.community.joinrequest.application.JoinRequestQueryService;
import boogi.apiserver.domain.hashtag.community.domain.CommunityHashtag;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.member.application.MemberCoreService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.application.MemberValidationService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.dto.response.BannedMemberDto;
import boogi.apiserver.domain.member.exception.NotAuthorizedMemberException;
import boogi.apiserver.domain.notice.application.NoticeQueryService;
import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.response.UserBasicProfileDto;
import boogi.apiserver.global.constant.HeaderConst;
import boogi.apiserver.global.constant.SessionInfoConst;
import boogi.apiserver.global.util.time.CustomDateTimeFormatter;
import boogi.apiserver.global.util.time.TimePattern;
import boogi.apiserver.global.webclient.push.SendPushNotification;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static boogi.apiserver.domain.post.postmedia.domain.MediaType.IMG;
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


    @Nested
    @DisplayName("커뮤니티 생성 테스트")
    class CommunityCreationTest {

        @Test
        @DisplayName("커뮤니티 생성 성공")
        void communityCreationSuccess() throws Exception {

            //given
            List<String> hashtags = List.of("해시테그1", "해시테그1");
            CreateCommunityRequest request = CreateCommunityRequest.builder()
                    .name("커뮤니티1")
                    .category("CLUB")
                    .description("설명")
                    .autoApproval(true)
                    .isPrivate(false)
                    .hashtags(hashtags)
                    .build();

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);


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
        @DisplayName("커뮤니티의 이름이 이미 존재하는 경우")
        void communityAlreadyExistsName() throws Exception {

            MockHttpSession session = new MockHttpSession();
            session.setAttribute(SessionInfoConst.USER_ID, 1L);

            List<String> hashtags = List.of("해시테그1", "해시테그1");
            CreateCommunityRequest request = CreateCommunityRequest.builder()
                    .name("커뮤니티1")
                    .category("CLUB")
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
    }

    @Test
    @DisplayName("커뮤니티 상세조회 (글 목록 조회)")
    void communityDetailWithPosts() throws Exception {

        final Member member = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(member, "memberType", MemberType.NORMAL);

        given(memberQueryService.getMemberOfTheCommunity(anyLong(), anyLong()))
                .willReturn(member);


        final CommunityHashtag hashtag = TestEmptyEntityGenerator.CommunityHashtag();
        ReflectionTestUtils.setField(hashtag, "tag", "테그1");

        final Community community = TestEmptyEntityGenerator.Community();
        ReflectionTestUtils.setField(community, "id", 1L);
        ReflectionTestUtils.setField(community, "communityName", "커뮤니티1");
        ReflectionTestUtils.setField(community, "description", "반가워");
        ReflectionTestUtils.setField(community, "isPrivate", false);
        ReflectionTestUtils.setField(community, "hashtags", List.of(hashtag));
        ReflectionTestUtils.setField(community, "memberCount", 3);
        ReflectionTestUtils.setField(community, "category", CommunityCategory.ACADEMIC);
        ReflectionTestUtils.setField(community, "createdAt", LocalDateTime.now());

        given(communityQueryService.getCommunityWithHashTag(anyLong()))
                .willReturn(community);

        final Notice notice = TestEmptyEntityGenerator.Notice();
        ReflectionTestUtils.setField(notice, "id", 1L);
        ReflectionTestUtils.setField(notice, "title", "노티스");
        ReflectionTestUtils.setField(notice, "createdAt", LocalDateTime.now());

        given(noticeQueryService.getCommunityLatestNotice(anyLong()))
                .willReturn(List.of(notice));

        final Post post = TestEmptyEntityGenerator.Post();
        ReflectionTestUtils.setField(post, "id", 4L);
        ReflectionTestUtils.setField(post, "content", "글");
        ReflectionTestUtils.setField(post, "createdAt", LocalDateTime.now());

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
                .andExpect(jsonPath("$.community.isPrivated").value(false))
                .andExpect(jsonPath("$.community.category").value("ACADEMIC"))
                .andExpect(jsonPath("$.community.name").value("커뮤니티1"))
                .andExpect(jsonPath("$.community.introduce").value("반가워"))
                .andExpect(jsonPath("$.community.hashtags[0]").value("테그1"))
                .andExpect(jsonPath("$.community.memberCount").value("3"))
                .andExpect(jsonPath("$.community.createdAt").value(CustomDateTimeFormatter.toString(community.getCreatedAt(), TimePattern.BASIC_FORMAT)))
                .andExpect(jsonPath("$.sessionMemberType").value("NORMAL"))
                .andExpect(jsonPath("$.posts[0].id").value(4))
                .andExpect(jsonPath("$.posts[0].content").value("글"))
                .andExpect(jsonPath("$.posts[0].createdAt").value(CustomDateTimeFormatter.toString(post.getCreatedAt(), TimePattern.BASIC_FORMAT)))
                .andExpect(jsonPath("$.notices[0].id").value(1))
                .andExpect(jsonPath("$.notices[0].title").value("노티스"))
                .andExpect(jsonPath("$.notices[0].createdAt").value(CustomDateTimeFormatter.toString(notice.getCreatedAt(), TimePattern.BASIC_FORMAT)));
    }

    @Test
    @DisplayName("기본 메타데이터 전달")
    void getMetadata() throws Exception {
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

    @Nested
    @DisplayName("커뮤니티 기본정보 테스트")
    class CommunityUpdateTest {

        @Test
        @DisplayName("커뮤니티 업데이트 소개란이 없는 경우")
        void noIntroduce() throws Exception {

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
        @DisplayName("커뮤니티 업데이트 소개란이 10글자 미만인 경우")
        void introduceIsLessThan10() throws Exception {

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
        @DisplayName("해시테그가 5개 초과하는 경우")
        void hashTagsIsGreaterThan5() throws Exception {

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
        @DisplayName("커뮤니티 업데이트 성공")
        void updateSuccess() throws Exception {
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
    }

    @Test
    @DisplayName("커뮤니티 폐쇄")
    void communityShutdown() throws Exception {

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                MockMvcRequestBuilders.delete("/api/communities/1")
                        .session(session)
                        .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
        ).andExpect(status().isOk());
    }

    @Nested
    @DisplayName("커뮤니티 설정 테스트")
    class CommunitySettingTest {

        @Test
        @DisplayName("설정정보 조회")
        void getSettingInfo() throws Exception {

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "isPrivate", true);
            ReflectionTestUtils.setField(community, "autoApproval", true);

            CommunitySettingInfo settingInfo = CommunitySettingInfo.of(community);

            given(communityQueryService.getSettingInfo(anyLong())).willReturn(settingInfo);

            MockHttpSession session = new MockHttpSession();
            session.setAttribute(SessionInfoConst.USER_ID, 1L);

            mvc.perform(
                            MockMvcRequestBuilders.get("/api/communities/1/settings")
                                    .session(session)
                                    .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(jsonPath("$.settingInfo.isAuto").value(true))
                    .andExpect(jsonPath("$.settingInfo.isSecret").value(true));

        }


        @Test
        @DisplayName("설정정보 수정하기")
        void postSettingCommunity() throws Exception {

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
    }

    @Nested
    @DisplayName("커뮤니티 게시글목록 조회 테스트")
    class CommunityPostsTest {

        @Test
        @DisplayName("비공개 && 가입 안한 경우")
        void privateAndNotJoined() throws Exception {
            given(memberQueryService.getMemberOfTheCommunity(anyLong(), anyLong()))
                    .willReturn(null);

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "isPrivate", true);

            given(communityQueryService.getCommunity(anyLong()))
                    .willReturn(community);

            MockHttpSession session = new MockHttpSession();
            session.setAttribute(SessionInfoConst.USER_ID, 1L);

            mvc.perform(
                            MockMvcRequestBuilders.get("/api/communities/1/posts")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                                    .session(session)
                                    .queryParam("page", "0")
                                    .queryParam("size", "3")
                    )
                    .andExpect(jsonPath("$.message").value("비공개 커뮤니티이면서, 가입되지 않았습니다."));
        }

        @Test
        @DisplayName("커뮤니티 게시글 목록 조회")
        void getCommunityPostList() throws Exception {
            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "communityName", "커뮤니티1");

            community.setCreatedAt(LocalDateTime.now());
            given(communityQueryService.getCommunity(anyLong()))
                    .willReturn(community);

            final User user = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user, "id", 1L);
            ReflectionTestUtils.setField(user, "username", "홍길동");
            ReflectionTestUtils.setField(user, "tagNumber", "#0001");

            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "memberType", MemberType.NORMAL);
            ReflectionTestUtils.setField(member, "user", user);

            given(memberQueryService.getMemberOfTheCommunity(anyLong(), anyLong()))
                    .willReturn(member);

            final PostHashtag postHashtag1 = TestEmptyEntityGenerator.PostHashtag();
            ReflectionTestUtils.setField(postHashtag1, "tag", "t1");

            final PostMedia postMedia = TestEmptyEntityGenerator.PostMedia();
            ReflectionTestUtils.setField(postMedia, "mediaURL", "123");
            ReflectionTestUtils.setField(postMedia, "mediaType", IMG);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 1L);
            ReflectionTestUtils.setField(post, "content", "내용1");
            ReflectionTestUtils.setField(post, "likeCount", 1);
            ReflectionTestUtils.setField(post, "postMedias", List.of(postMedia));
            ReflectionTestUtils.setField(post, "commentCount", 1);
            ReflectionTestUtils.setField(post, "member", member);
            ReflectionTestUtils.setField(post, "community", community);
            ReflectionTestUtils.setField(post, "hashtags", List.of(postHashtag1));
            ReflectionTestUtils.setField(post, "createdAt", LocalDateTime.now());


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
                    .andExpect(jsonPath("$.memberType").value("NORMAL"))
                    .andExpect(jsonPath("$.pageInfo.nextPage").value(1))
                    .andExpect(jsonPath("$.pageInfo.hasNext").value(false))
                    .andExpect(jsonPath("$.communityName").value("커뮤니티1"))
                    .andExpect(jsonPath("$.posts.size()").value(1))
                    .andExpect(jsonPath("$.posts[0].likeId").doesNotExist())
                    .andExpect(jsonPath("$.posts[0].postMedias[0].url").value("123"))
                    .andExpect(jsonPath("$.posts[0].id").value(1L))
                    .andExpect(jsonPath("$.posts[0].content").value("내용1"))
                    .andExpect(jsonPath("$.posts[0].createdAt").value(CustomDateTimeFormatter.toString(post.getCreatedAt(), TimePattern.BASIC_FORMAT)))
                    .andExpect(jsonPath("$.posts[0].hashtags.size()").value(1))
                    .andExpect(jsonPath("$.posts[0].hashtags[0]").value("t1"))
                    .andExpect(jsonPath("$.posts[0].likeCount").value(1))
                    .andExpect(jsonPath("$.posts[0].commentCount").value(1))
                    .andExpect(jsonPath("$.posts[0].me").value(true))
                    .andExpect(jsonPath("$.posts[0].user.id").value(1L))
                    .andExpect(jsonPath("$.posts[0].user.name").value("홍길동"))
                    .andExpect(jsonPath("$.posts[0].user.tagNum").value("#0001"));
        }
    }

    @Test
    @DisplayName("커뮤니티 멤버 목록 페이지네이션")
    void communityMembersPagination() throws Exception {

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        final User user = TestEmptyEntityGenerator.User();
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "username", "김가나");
        ReflectionTestUtils.setField(user, "tagNumber", "#0001");
        ReflectionTestUtils.setField(user, "department", "컴공");

        final Member member = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(member, "id", 2L);
        ReflectionTestUtils.setField(member, "memberType", MemberType.NORMAL);
        ReflectionTestUtils.setField(member, "user", user);

        member.setCreatedAt(LocalDateTime.now());

        PageImpl<Member> page = new PageImpl<>(List.of(member), Pageable.ofSize(1), 1);
        given(memberQueryService.getCommunityJoinedMembers(any(), anyLong())).willReturn(page);

        mvc.perform(
                        MockMvcRequestBuilders.get("/api/communities/1/members")
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.pageInfo.nextPage").value(1))
                .andExpect(jsonPath("$.pageInfo.hasNext").value(false))
                .andExpect(jsonPath("$.members[0].id").value(2))
                .andExpect(jsonPath("$.members[0].memberType").value("NORMAL"))
                .andExpect(jsonPath("$.members[0].createdAt").value(CustomDateTimeFormatter.toString(member.getCreatedAt(), TimePattern.BASIC_FORMAT)))
                .andExpect(jsonPath("$.members[0].user.id").value(1))
                .andExpect(jsonPath("$.members[0].user.tagNum").value("#0001"))
                .andExpect(jsonPath("$.members[0].user.name").value("김가나"))
                .andExpect(jsonPath("$.members[0].user.department").value("컴공"));
    }

    @Nested
    @DisplayName("커뮤니티 멤버 차단")
    class BlockedMember {
        @Test
        @DisplayName("차단된 멤버 목록 조회")
        void getBlockedMemberList() throws Exception {
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
        @DisplayName("멤버 차단 해제")
        void unblockMember() throws Exception {
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
    }

    @Test
    @DisplayName("멤버 권한 부여하기")
    void delegate() throws Exception {
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

    @Nested
    @DisplayName("커뮤니티 가입요청 테스트")
    class JoinRequestTest {
        @Test
        @DisplayName("가입요청 조회 권한이 없는 경우")
        void unauthorized() throws Exception {

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
        @DisplayName("관리자의 가입요청목록 조회 성공")
        void getJoinRequestList() throws Exception {
            final User user = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user, "id", 1L);
            ReflectionTestUtils.setField(user, "username", "홍길동");
            ReflectionTestUtils.setField(user, "tagNumber", "#0001");

            given(joinRequestQueryService.getAllRequests(anyLong()))
                    .willReturn(List.of(
                            Map.of("user", UserBasicProfileDto.of(user),
                                    "id", 2L)
                    ));

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
        @DisplayName("가입요청 성공")
        void applySuccess() throws Exception {

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
        @DisplayName("관리가자 가입요청 컨펌 성공")
        void confirm() throws Exception {

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
    }

    @Test
    @DisplayName("커뮤니티 검색하기")
    void searchCommunity() throws Exception {

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        SearchCommunityDto dto = SearchCommunityDto.builder()
                .category("HOBBY")
                .createdAt(LocalDateTime.now())
                .id(1L)
                .hashtags(List.of("안녕", "헤헤"))
                .memberCount(23)
                .isPrivate(false)
                .name("커뮤니티1")
                .description("소개")
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
                                .queryParam("category", "HOBBY")
                                .queryParam("keyword", "안녕")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.communities[0].id").value(1))
                .andExpect(jsonPath("$.communities[0].name").value("커뮤니티1"))
                .andExpect(jsonPath("$.communities[0].description").value("소개"))
                .andExpect(jsonPath("$.communities[0].hashtags.size()").value(2))
                .andExpect(jsonPath("$.communities[0].hashtags[0]").value("안녕"))
                .andExpect(jsonPath("$.communities[0].memberCount").value(23))
                .andExpect(jsonPath("$.communities[0].category").value("HOBBY"))
                .andExpect(jsonPath("$.communities[0].isPrivate").value(false));
    }

    @Test
    @DisplayName("해당 커뮤니티에 가입된 모든 멤버 가져오기")
    void testGetMembersAll() throws Exception {
        final User user = TestEmptyEntityGenerator.User();
        ReflectionTestUtils.setField(user, "id", 2L);

        final Community community = TestEmptyEntityGenerator.Community();
        ReflectionTestUtils.setField(community, "id", 1L);

        final Member member = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(member, "id", 2L);
        ReflectionTestUtils.setField(member, "community", community);
        ReflectionTestUtils.setField(member, "user", user);

        List<Member> membersWithoutMe = List.of(member);
        given(memberCoreService.getJoinedMembersAll(anyLong(), anyLong()))
                .willReturn(membersWithoutMe);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                        MockMvcRequestBuilders.get("/api/communities/1/members/all")
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.members[0].id").value(2L))
                .andExpect(jsonPath("$.members[0].user.id").value(2L));
    }
}