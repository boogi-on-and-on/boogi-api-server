package boogi.apiserver.domain.comment.api;

import boogi.apiserver.builder.TestComment;
import boogi.apiserver.builder.TestLike;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.comment.application.CommentCommandService;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.dto.dto.UserCommentDto;
import boogi.apiserver.domain.comment.dto.request.CreateCommentRequest;
import boogi.apiserver.domain.comment.dto.response.UserCommentPageResponse;
import boogi.apiserver.domain.like.application.LikeCommandService;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.like.dto.response.LikeMembersAtCommentResponse;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.constant.HeaderConst;
import boogi.apiserver.global.constant.SessionInfoConst;
import boogi.apiserver.global.dto.PaginationDto;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = CommentApiController.class)
class CommentApiControllerTest {

    @MockBean
    CommentCommandService commentCommandService;

    @MockBean
    LikeCommandService likeCommandService;

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
    @DisplayName("유저 댓글 슬라이스")
    void userCommentSlice() throws Exception {

        final UserCommentDto commentDto = new UserCommentDto("내용1", LocalDateTime.now(), 1L);
        final PaginationDto pageInfo = new PaginationDto(1, false);
        final UserCommentPageResponse commentPage = new UserCommentPageResponse(List.of(commentDto), pageInfo);

//        given(commentCommandService.getUserComments(anyLong(), any(), any(Pageable.class)))
//                .willReturn(commentPage);
//
//        MockHttpSession session = new MockHttpSession();
//        session.setAttribute(SessionInfoConst.USER_ID, 1L);
//
//        mvc.perform(
//                        MockMvcRequestBuilders.get("/api/comments/users")
//                                .queryParam("userId", "4")
//                                .queryParam("page", "0")
//                                .queryParam("size", "1")
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .session(session)
//                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
//                ).andExpect(status().isOk())
//                .andExpect(jsonPath("$.pageInfo.nextPage").value(1))
//                .andExpect(jsonPath("$.pageInfo.hasNext").value(false))
//                .andExpect(jsonPath("$.comments[0].content").value("내용1"))
//                .andExpect(jsonPath("$.comments[0].postId").value(1))
//                .andExpect(jsonPath("$.comments.size()").value(1));
    }

    @Test
    @DisplayName("댓글 생성")
    void testCreateComment() throws Exception {
        CreateCommentRequest createCommentRequest = new CreateCommentRequest(1L, null, null, null);

        final Comment newComment = TestComment.builder().id(1L).build();

//        given(commentCommandService.createComment(any(CreateCommentRequest.class), eq(1L)))
//                .willReturn(newComment);
//
//        MockHttpSession session = new MockHttpSession();
//        session.setAttribute(SessionInfoConst.USER_ID, 1L);
//
//        mvc.perform(
//                        MockMvcRequestBuilders.post("/api/comments/")
//                                .content(mapper.writeValueAsBytes(createCommentRequest))
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .session(session)
//                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
//                ).andExpect(status().isCreated())
//                .andExpect(jsonPath("$.id").value(newComment.getId()));
    }

    @Test
    @DisplayName("댓글에 좋아요 하기")
    void testDoLikeAtComment() throws Exception {
        final Like like = TestLike.builder().id(1L).build();

//        given(likeCommandService.doCommentLike(anyLong(), anyLong()))
//                .willReturn(like);
//
//        MockHttpSession session = new MockHttpSession();
//        session.setAttribute(SessionInfoConst.USER_ID, 1L);
//
//        mvc.perform(
//                        MockMvcRequestBuilders.post("/api/comments/1/likes")
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .session(session)
//                                .header(HeaderConst.AUTH_TOKEN, "AUTH-TOKEN")
//                ).andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(like.getId()));
    }

    @Test
    @DisplayName("댓글 삭제")
    void testDeleteComment() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                MockMvcRequestBuilders.delete("/api/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .session(session)
                        .header(HeaderConst.AUTH_TOKEN, "AUTH-TOKEN")
        ).andExpect(status().isOk());
    }

    @Test
    @DisplayName("댓글에 좋아요한 멤버 목록 조회")
    void testGetLikeMembersAtComment() throws Exception {
        final User user1 = TestUser.builder()
                .id(1L)
                .username("유저")
                .tagNumber("#0001")
                .profileImageUrl("321")
                .build();

        List<User> users = List.of(user1);

//        LikeMembersAtCommentResponse likeMembersAtCommentResponse = LikeMembersAtCommentResponse.of(users,
//                new PageImpl((users), Pageable.ofSize(1), 1));
//        given(likeCommandService.getLikeMembersAtComment(anyLong(), anyLong(), any(Pageable.class)))
//                .willReturn(likeMembersAtCommentResponse);
//
//        MockHttpSession session = new MockHttpSession();
//        session.setAttribute(SessionInfoConst.USER_ID, 1L);
//
//        mvc.perform(
//                        MockMvcRequestBuilders.get("/api/comments/1/likes")
//                                .queryParam("page", "0")
//                                .queryParam("size", "1")
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .session(session)
//                                .header(HeaderConst.AUTH_TOKEN, "AUTH-TOKEN")
//                ).andExpect(status().isOk())
//                .andExpect(jsonPath("$.members[0].id").value(user1.getId()))
//                .andExpect(jsonPath("$.members[0].name").value(user1.getUsername()))
//                .andExpect(jsonPath("$.members[0].tagNum").value(user1.getTagNumber()))
//                .andExpect(jsonPath("$.members[0].profileImageUrl").value(user1.getProfileImageUrl()))
//                .andExpect(jsonPath("$.pageInfo.nextPage").value(1))
//                .andExpect(jsonPath("$.pageInfo.hasNext").value(false));
    }
}