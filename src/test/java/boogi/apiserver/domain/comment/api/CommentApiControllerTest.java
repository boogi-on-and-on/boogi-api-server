package boogi.apiserver.domain.comment.api;

import boogi.apiserver.domain.comment.application.CommentCoreService;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.dto.request.CreateComment;
import boogi.apiserver.domain.comment.dto.response.UserCommentDto;
import boogi.apiserver.domain.comment.dto.response.UserCommentPage;
import boogi.apiserver.domain.like.application.LikeCoreService;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.like.dto.response.LikeMembersAtComment;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.constant.HeaderConst;
import boogi.apiserver.global.constant.SessionInfoConst;
import boogi.apiserver.global.dto.PaginationDto;
import boogi.apiserver.global.webclient.push.SendPushNotification;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = CommentApiController.class)
class CommentApiControllerTest {

    @MockBean
    CommentCoreService commentCoreService;

    @MockBean
    LikeCoreService likeCoreService;

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
        UserCommentPage commentPage = UserCommentPage.builder()
                .pageInfo(PaginationDto.builder()
                        .nextPage(1)
                        .hasNext(false)
                        .build())
                .comments(List.of(
                        UserCommentDto
                                .builder()
                                .postId(1L)
                                .createdAt(LocalDateTime.now())
                                .content("내용1")
                                .build()
                ))
                .build();

        given(commentCoreService.getUserComments(anyLong(), any(), any(Pageable.class)))
                .willReturn(commentPage);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                        MockMvcRequestBuilders.get("/api/comments/users")
                                .queryParam("userId", "4")
                                .queryParam("page", "0")
                                .queryParam("size", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.pageInfo.nextPage").value(1))
                .andExpect(jsonPath("$.pageInfo.hasNext").value(false))
                .andExpect(jsonPath("$.comments[0].content").value("내용1"))
                .andExpect(jsonPath("$.comments[0].postId").value(1))
                .andExpect(jsonPath("$.comments.size()").value(1));
    }

    @Test
    @DisplayName("댓글 생성")
    void testCreateComment() throws Exception {
        CreateComment createComment = new CreateComment(1L, null, null, null);

        final Comment newComment = TestEmptyEntityGenerator.Comment();
        ReflectionTestUtils.setField(newComment, "id", 1L);

        given(commentCoreService.createComment(any(CreateComment.class), eq(1L)))
                .willReturn(newComment);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                        MockMvcRequestBuilders.post("/api/comments/")
                                .content(mapper.writeValueAsBytes(createComment))
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
                ).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(newComment.getId()));
    }

    @Test
    @DisplayName("댓글에 좋아요 하기")
    void testDoLikeAtComment() throws Exception {
        final Like like = TestEmptyEntityGenerator.Like();
        ReflectionTestUtils.setField(like, "id", 1L);

        given(likeCoreService.doLikeAtComment(anyLong(), anyLong()))
                .willReturn(like);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                        MockMvcRequestBuilders.post("/api/comments/1/likes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH-TOKEN")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(like.getId()));
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
        User user1 = User.builder()
                .id(1L)
                .username("유저1")
                .tagNumber("#0001")
                .build();
        List<User> users = List.of(user1);

        List<LikeMembersAtComment.UserInfo> userInfos = users.stream()
                .map(user -> LikeMembersAtComment.UserInfo.toDto(user))
                .collect(Collectors.toList());

        LikeMembersAtComment likeMembersAtComment = LikeMembersAtComment.builder()
                .members(userInfos)
                .pageInfo(PaginationDto.builder().nextPage(1).hasNext(false).build())
                .build();
        given(likeCoreService.getLikeMembersAtComment(anyLong(), anyLong(), any(Pageable.class)))
                .willReturn(likeMembersAtComment);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        mvc.perform(
                        MockMvcRequestBuilders.get("/api/comments/1/likes")
                                .queryParam("page", "0")
                                .queryParam("size", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .session(session)
                                .header(HeaderConst.AUTH_TOKEN, "AUTH-TOKEN")
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.members[0].id").value(user1.getId()))
                .andExpect(jsonPath("$.members[0].name").value(user1.getUsername()))
                .andExpect(jsonPath("$.members[0].tagNum").value(user1.getTagNumber()))
                .andExpect(jsonPath("$.members[0].profileImageUrl").value(user1.getProfileImageUrl()))
                .andExpect(jsonPath("$.pageInfo.nextPage").value(1))
                .andExpect(jsonPath("$.pageInfo.hasNext").value(false));
    }
}