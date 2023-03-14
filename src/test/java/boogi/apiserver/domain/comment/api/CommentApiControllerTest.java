package boogi.apiserver.domain.comment.api;

import boogi.apiserver.domain.comment.application.CommentCommandService;
import boogi.apiserver.domain.comment.application.CommentQueryService;
import boogi.apiserver.domain.comment.dto.dto.UserCommentDto;
import boogi.apiserver.domain.comment.dto.request.CreateCommentRequest;
import boogi.apiserver.domain.comment.dto.response.UserCommentPageResponse;
import boogi.apiserver.domain.like.application.LikeCommandService;
import boogi.apiserver.domain.like.application.LikeQueryService;
import boogi.apiserver.domain.like.dto.response.LikeMembersAtCommentResponse;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import boogi.apiserver.global.constant.HeaderConst;
import boogi.apiserver.global.constant.SessionInfoConst;
import boogi.apiserver.global.dto.PaginationDto;
import boogi.apiserver.global.webclient.push.MentionType;
import boogi.apiserver.global.webclient.push.SendPushNotification;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = CommentApiController.class)
class CommentApiControllerTest {

    @MockBean
    CommentQueryService commentQueryService;

    @MockBean
    CommentCommandService commentCommandService;

    @MockBean
    LikeCommandService likeCommandService;

    @MockBean
    LikeQueryService likeQueryService;

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
    @DisplayName("유저의 댓글을 페이지네이션으로 가져온다.")
    void userCommentSlice() throws Exception {
        final UserCommentDto commentDto = new UserCommentDto("내용1", LocalDateTime.now(), 1L);
        final PaginationDto pageInfo = new PaginationDto(1, false);
        final UserCommentPageResponse response = new UserCommentPageResponse(List.of(commentDto), pageInfo);

        given(commentQueryService.getUserComments(anyLong(), anyLong(), any(Pageable.class)))
                .willReturn(response);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                        get("/api/comments/users")
                                .queryParam("userId", "4")
                                .queryParam("page", "0")
                                .queryParam("size", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.comments.size()").value(1))
                .andExpect(jsonPath("$.comments[0].content").value("내용1"))
                .andExpect(jsonPath("$.comments[0].postId").value(1))
                .andExpect(jsonPath("$.pageInfo.nextPage").value(1))
                .andExpect(jsonPath("$.pageInfo.hasNext").value(false));
    }

    @Test
    @DisplayName("댓글을 생성한다.")
    void testCreateComment() throws Exception {
        final long NEW_COMMENT_ID = 3L;

        CreateCommentRequest request =
                new CreateCommentRequest(2L, null, "댓글", List.of());

        given(commentCommandService.createComment(any(CreateCommentRequest.class), anyLong()))
                .willReturn(NEW_COMMENT_ID);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                        post("/api/comments/")
                                .content(mapper.writeValueAsBytes(request))
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                ).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(NEW_COMMENT_ID));
    }

    @Test
    @DisplayName("댓글 생성시 맨션할 유저 아이디를 추가하지 않으면 댓글 생성 푸시 알람만 보낸다.")
    void verifyOnlyCreateCommentPushNotification() throws Exception {
        CreateCommentRequest request =
                new CreateCommentRequest(2L, null, "댓글", List.of());

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                post("/api/comments/")
                        .content(mapper.writeValueAsBytes(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .session(session)
                        .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
        ).andExpect(status().isCreated());

        verify(sendPushNotification, times(1))
                .commentNotification(anyLong());
        verify(sendPushNotification, times(0))
                .mentionNotification(anyList(), anyLong(), any(MentionType.class));
    }

    @Test
    @DisplayName("댓글 생성시 맨션할 유저 아이디를 추가하면 댓글 생성과 맨션 푸시 알람을 보낸다.")
    void verifyCreateCommentPushNotification() throws Exception {
        CreateCommentRequest request =
                new CreateCommentRequest(2L, null, "댓글", List.of(3L));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                post("/api/comments/")
                        .content(mapper.writeValueAsBytes(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .session(session)
                        .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
        ).andExpect(status().isCreated());

        verify(sendPushNotification, times(1))
                .commentNotification(anyLong());
        verify(sendPushNotification, times(1))
                .mentionNotification(anyList(), anyLong(), any(MentionType.class));
    }

    @Test
    @DisplayName("댓글에 좋아요를 한다.")
    void testDoLikeAtComment() throws Exception {
        final long NEW_LIKE_ID = 2L;

        given(likeCommandService.doCommentLike(anyLong(), anyLong()))
                .willReturn(NEW_LIKE_ID);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                        post("/api/comments/1/likes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH-TOKEN")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(NEW_LIKE_ID));
    }

    @Test
    @DisplayName("댓글을 삭제한다.")
    void testDeleteComment() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                delete("/api/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .session(session)
                        .header(HeaderConst.AUTH_TOKEN, "AUTH-TOKEN")
        ).andExpect(status().isOk());
    }

    @Test
    @DisplayName("댓글에 좋아요한 멤버 목록 조회")
    void testGetLikeMembersAtComment() throws Exception {
        final long USER_ID = 2L;
        final String PROFILE_URL = "url";
        final String TAG_NUM = "#0001";
        final String USERNAME = "유저";

        UserBasicProfileDto userDto = new UserBasicProfileDto(USER_ID, PROFILE_URL, TAG_NUM, USERNAME);
        PaginationDto paginationDto = new PaginationDto(1, false);

        LikeMembersAtCommentResponse response =
                new LikeMembersAtCommentResponse(List.of(userDto), paginationDto);
        given(likeQueryService.getLikeMembersAtComment(anyLong(), anyLong(), any(Pageable.class)))
                .willReturn(response);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                        get("/api/comments/1/likes")
                                .queryParam("page", "0")
                                .queryParam("size", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH-TOKEN")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.members[0].id").value(USER_ID))
                .andExpect(jsonPath("$.members[0].name").value(USERNAME))
                .andExpect(jsonPath("$.members[0].tagNum").value(TAG_NUM))
                .andExpect(jsonPath("$.members[0].profileImageUrl").value(PROFILE_URL))
                .andExpect(jsonPath("$.pageInfo.nextPage").value(1))
                .andExpect(jsonPath("$.pageInfo.hasNext").value(false));
    }
}