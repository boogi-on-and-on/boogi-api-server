package boogi.apiserver.domain.post.post.api;

import boogi.apiserver.domain.comment.application.CommentCoreService;
import boogi.apiserver.domain.like.application.LikeCoreService;
import boogi.apiserver.domain.post.post.application.PostCoreService;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.dto.HotPost;
import boogi.apiserver.domain.post.post.dto.SearchPostDto;
import boogi.apiserver.domain.post.post.dto.UserPostPage;
import boogi.apiserver.domain.post.post.dto.UserPostsDto;
import boogi.apiserver.domain.post.post.dto.request_enum.PostListingOrder;
import boogi.apiserver.domain.user.dto.UserBasicProfileDto;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = PostApiController.class)
class PostApiControllerTest {

    @MockBean
    private PostQueryService postQueryService;

    @MockBean
    PostCoreService postCoreService;

    @MockBean
    LikeCoreService likeCoreService;

    @MockBean
    CommentCoreService commentCoreService;

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

    @Test
    void 유저_게시글_페이지네이션() throws Exception {
        UserPostsDto postsDto = UserPostsDto.builder()
                .id(1L)
                .content("게시글 내용1")
                .community(UserPostsDto.CommunityDto.builder()
                        .id("1")
                        .name("커뮤니티1")
                        .build())
                .build();

        UserPostPage pageInfo = UserPostPage.builder()
                .nextPage(1)
                .posts(List.of(postsDto))
                .hasNext(false)
                .totalCount(20).build();

        given(postQueryService.getUserPosts(any(), anyLong())).willReturn(pageInfo);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                        MockMvcRequestBuilders.get("/api/posts/user/1")
                                .queryParam("page", "0")
                                .queryParam("size", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTO_TOKEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextPage").value(1))
                .andExpect(jsonPath("$.totalCount").value(20))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.posts[0].community.id").value("1"))
                .andExpect(jsonPath("$.posts[0].community.name").value("커뮤니티1"))
                .andExpect(jsonPath("$.posts.size()").value(1));
    }

    @Test
    void 핫한게시물() throws Exception {
        HotPost hotPost1 = HotPost.builder()
                .postId(1L)
                .content("내용")
                .commentCount("1")
                .likeCount("1")
                .communityId("1")
                .hashtags(List.of("hashtag1"))
                .build();

        HotPost hotPost2 = HotPost.builder()
                .postId(2L)
                .content("내용")
                .commentCount("2")
                .likeCount("2")
                .communityId("2")
                .build();

        HotPost hotPost3 = HotPost.builder()
                .postId(3L)
                .content("내용")
                .commentCount("3")
                .communityId("3")
                .likeCount("3")
                .build();

        given(postQueryService.getHotPosts())
                .willReturn(List.of(hotPost1, hotPost2, hotPost3));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                        MockMvcRequestBuilders.get("/api/posts/hot")
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN"))
                .andExpect(jsonPath("$.hots.size()").value(3))
                .andExpect(jsonPath("$.hots[0].postId").isNumber())
                .andExpect(jsonPath("$.hots[0].content").isString())
                .andExpect(jsonPath("$.hots[0].commentCount").isString())
                .andExpect(jsonPath("$.hots[0].likeCount").isString())
                .andExpect(jsonPath("$.hots[0].communityId").isString())
                .andExpect(jsonPath("$.hots[0].hashtags").isArray());


    }

    @Test
    void 게시물_검색() throws Exception {
        SearchPostDto dto = SearchPostDto.builder()
                .id(1L)
                .commentCount(1)
                .communityName("팍스")
                .createdAt(LocalDateTime.now().toString())
                .communityId(2L)
                .content("게시글내용")
                .hashtags(List.of("해시테그1", "해시태그2"))
                .likeCount(2)
                .user(UserBasicProfileDto.builder()
                        .id(1L)
                        .tagNum("#0001")
                        .name("김")
                        .build())
                .build();


        PageImpl page = new PageImpl(List.of(dto), Pageable.ofSize(1), 1);
        given(postQueryService.getSearchedPosts(any(), any(), anyLong()))
                .willReturn(page);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                        MockMvcRequestBuilders.get("/api/posts/search")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                                .session(session)
                                .queryParam("page", "0")
                                .queryParam("size", "1")
                                .queryParam("keyword", "헤헤")
                                .queryParam("order", PostListingOrder.LIKE_UPPER.toString())

                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.posts").isArray())
                .andExpect(jsonPath("$.posts[0].id").value(1L))
                .andExpect(jsonPath("$.posts[0].commentCount").value(1))
                .andExpect(jsonPath("$.posts[0].communityName").value("팍스"))
                .andExpect(jsonPath("$.posts[0].communityId").value(2L))
                .andExpect(jsonPath("$.posts[0].content").value("게시글내용"))
                .andExpect(jsonPath("$.posts[0].likeCount").value(2))
                .andExpect(jsonPath("$.posts[0].hashtags").isArray())
                .andExpect(jsonPath("$.posts[0].user").isMap());
    }
}