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
import boogi.apiserver.utils.controller.TestControllerSetUp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = CommentApiController.class)
class CommentApiControllerTest extends TestControllerSetUp {

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

        ResultActions result = mvc.perform(
                get("/api/comments/users")
                        .queryParam("userId", "4")
                        .queryParam("page", "0")
                        .queryParam("size", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .session(session)
                        .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
        );

        result
                .andExpect(status().isOk())
                .andDo(document("comment/get-user-comments",
                        responseFields(
                                fieldWithPath("comments").type(JsonFieldType.ARRAY)
                                        .description("댓글 목록"),
                                fieldWithPath("comments[].content").type(JsonFieldType.STRING)
                                        .description("댓글 내용"),
                                fieldWithPath("comments[].createdAt").type(JsonFieldType.STRING)
                                        .description("댓글 생성시간"),
                                fieldWithPath("comments[].postId").type(JsonFieldType.NUMBER)
                                        .description("댓글이 작성된 게시글의 ID"),
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
    @DisplayName("댓글을 생성한다.")
    void testCreateComment() throws Exception {
        final long NEW_COMMENT_ID = 3L;

        CreateCommentRequest request =
                new CreateCommentRequest(2L, null, "댓글", List.of());

        given(commentCommandService.createComment(any(CreateCommentRequest.class), anyLong()))
                .willReturn(NEW_COMMENT_ID);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        ResultActions result = mvc.perform(
                post("/api/comments/")
                        .content(mapper.writeValueAsBytes(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .session(session)
                        .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
        );

        result
                .andExpect(status().isCreated())
                .andDo(document("comment/post",
                        requestFields(
                                fieldWithPath("postId").type(JsonFieldType.NUMBER)
                                        .description("게시글 ID"),
                                fieldWithPath("parentCommentId").type(JsonFieldType.VARIES)
                                        .description("부모 댓글 ID -> 생성할 댓글이 부모 댓글일 경우 null"),
                                fieldWithPath("content").type(JsonFieldType.STRING)
                                        .description("댓글 내용"),
                                fieldWithPath("mentionedUserIds").type(JsonFieldType.ARRAY)
                                        .description("맨션할 유저의 ID들")
                        ),
                        responseFields(
                                fieldWithPath("id").type(JsonFieldType.NUMBER)
                                        .description("생성된 댓글 ID")
                        )));
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

        ResultActions result = mvc.perform(
                post("/api/comments/{commentId}/likes", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .session(session)
                        .header(HeaderConst.AUTH_TOKEN, "AUTH-TOKEN")
        );

        result
                .andExpect(status().isOk())
                .andDo(document("comment/post-commentId-likes",
                        pathParameters(
                                parameterWithName("commentId").description("좋아요할 댓글 ID")
                        ),
                        responseFields(
                                fieldWithPath("id").type(JsonFieldType.NUMBER)
                                        .description("생성된 좋아요 ID")
                        ))
                );
    }

    @Test
    @DisplayName("댓글을 삭제한다.")
    void testDeleteComment() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        ResultActions result = mvc.perform(
                delete("/api/comments/{commentId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .session(session)
                        .header(HeaderConst.AUTH_TOKEN, "AUTH-TOKEN")
        );

        result
                .andExpect(status().isOk())
                .andDo(document("comment/delete-commentId",
                        pathParameters(parameterWithName("commentId").description("삭제할 댓글 ID"))
                ));
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

        ResultActions result = mvc.perform(
                get("/api/comments/{commentId}/likes", 1L)
                        .queryParam("page", "0")
                        .queryParam("size", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .session(session)
                        .header(HeaderConst.AUTH_TOKEN, "AUTH-TOKEN")
        );

        result
                .andExpect(status().isOk())
                .andDo(document("comment/get-commentId-likes",
                        pathParameters(
                                parameterWithName("commentId").description("댓글 ID")
                        ),
                        requestParameters(
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").description("페이지 사이즈")
                        ),
                        responseFields(
                                fieldWithPath("members").type(JsonFieldType.ARRAY)
                                        .description("좋아요한 유저 목록"),
                                fieldWithPath("members[].id").type(JsonFieldType.NUMBER)
                                        .description("유저 ID"),
                                fieldWithPath("members[].profileImageUrl").type(JsonFieldType.STRING)
                                        .description("프로필 이미지 주소").optional(),
                                fieldWithPath("members[].tagNum").type(JsonFieldType.STRING)
                                        .description("태그 번호"),
                                fieldWithPath("members[].name").type(JsonFieldType.STRING)
                                        .description("유저 이름"),
                                fieldWithPath("pageInfo").type(JsonFieldType.OBJECT)
                                        .description("페이지네이션 정보"),
                                fieldWithPath("pageInfo.nextPage").type(JsonFieldType.NUMBER)
                                        .description("다음 페이지 번호"),
                                fieldWithPath("pageInfo.hasNext").type(JsonFieldType.BOOLEAN)
                                        .description("true -> 다음 페이지가 있는 경우")
                        )
                ));
    }
}