package boogi.apiserver.domain.community.community.api;

import boogi.apiserver.domain.community.community.application.CommunityCoreService;
import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.dto.CreateCommunityRequest;
import boogi.apiserver.domain.community.community.exception.AlreadyExistsCommunityNameException;
import boogi.apiserver.domain.hashtag.community.domain.CommunityHashtag;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.notice.application.NoticeQueryService;
import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.global.constant.HeaderConst;
import boogi.apiserver.global.constant.SessionInfoConst;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDateTime;
import java.util.List;

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
    CommunityCoreService communityCoreService;

    @MockBean
    CommunityQueryService communityQueryService;

    @MockBean
    NoticeQueryService noticeQueryService;

    @MockBean
    MemberQueryService memberQueryService;

    @MockBean
    PostQueryService postQueryService;

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
}