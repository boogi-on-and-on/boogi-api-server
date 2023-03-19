package boogi.apiserver.domain.comment.api;

import boogi.apiserver.domain.comment.application.CommentCommandService;
import boogi.apiserver.domain.comment.application.CommentQueryService;
import boogi.apiserver.domain.comment.dto.dto.UserCommentDto;
import boogi.apiserver.domain.comment.dto.request.CreateCommentRequest;
import boogi.apiserver.domain.comment.dto.response.UserCommentPageResponse;
import boogi.apiserver.domain.comment.exception.CanNotDeleteCommentException;
import boogi.apiserver.domain.comment.exception.CommentMaxDepthOverException;
import boogi.apiserver.domain.comment.exception.CommentNotFoundException;
import boogi.apiserver.domain.comment.exception.ParentCommentNotFoundException;
import boogi.apiserver.domain.like.application.LikeCommandService;
import boogi.apiserver.domain.like.application.LikeQueryService;
import boogi.apiserver.domain.like.dto.response.LikeMembersAtCommentResponse;
import boogi.apiserver.domain.like.exception.AlreadyDoCommentLikeException;
import boogi.apiserver.domain.member.exception.NotJoinedMemberException;
import boogi.apiserver.domain.member.exception.NotViewableMemberException;
import boogi.apiserver.domain.post.post.exception.PostNotFoundException;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import boogi.apiserver.domain.user.exception.UserNotFoundException;
import boogi.apiserver.global.constant.HeaderConst;
import boogi.apiserver.global.dto.PaginationDto;
import boogi.apiserver.global.webclient.push.SendPushNotification;
import boogi.apiserver.utils.controller.MockHttpSessionCreator;
import boogi.apiserver.utils.controller.TestControllerSetUp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
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


    @Nested
    @DisplayName("유저가 작성한 댓글 조회")
    class GetUserComments {
        @Test
        @DisplayName("유저의 댓글을 페이지네이션으로 가져오는데 성공한다.")
        void userCommentSliceSuccess() throws Exception {
            final UserCommentDto commentDto = new UserCommentDto("내용", LocalDateTime.now(), 1L);
            final PaginationDto pageInfo = new PaginationDto(1, false);
            final UserCommentPageResponse response = new UserCommentPageResponse(List.of(commentDto), pageInfo);

            given(commentQueryService.getUserComments(anyLong(), anyLong(), any(Pageable.class)))
                    .willReturn(response);

            ResultActions result = mvc.perform(
                    get("/api/comments/users")
                            .queryParam("userId", "4")
                            .queryParam("page", "0")
                            .queryParam("size", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
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
        @DisplayName("존재하지 않는 유저로 조회시 UserNotFoundException 발생")
        void notExistUserFail() throws Exception {
            doThrow(new UserNotFoundException())
                    .when(commentQueryService).getUserComments(anyLong(), anyLong(), any(Pageable.class));

            ResultActions result = mvc.perform(
                    get("/api/comments/users")
                            .queryParam("userId", "9999")
                            .queryParam("page", "0")
                            .queryParam("size", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("comment/get-users-UserNotFoundException"));
        }
    }

    @Nested
    @DisplayName("댓글 생성")
    class CreateComment {
        @Test
        @DisplayName("댓글 생성에 성공한다.")
        void createCommentSuccess() throws Exception {
            final long NEW_COMMENT_ID = 3L;

            CreateCommentRequest request = new CreateCommentRequest(2L, null, "댓글", List.of());

            given(commentCommandService.createComment(any(CreateCommentRequest.class), anyLong()))
                    .willReturn(NEW_COMMENT_ID);

            ResultActions result = mvc.perform(
                    post("/api/comments/")
                            .content(mapper.writeValueAsBytes(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
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
        @DisplayName("존재하지 않는 게시글 ID로 요청할 경우 PostNotFoundException 발생")
        void notExistPostFail() throws Exception {
            CreateCommentRequest request =
                    new CreateCommentRequest(9999L, null, "댓글", List.of());

            doThrow(new PostNotFoundException())
                    .when(commentCommandService).createComment(any(CreateCommentRequest.class), anyLong());

            ResultActions result = mvc.perform(
                    post("/api/comments/")
                            .content(mapper.writeValueAsBytes(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("comment/post-PostNotFoundException"));
        }

        @Test
        @DisplayName("해당 커뮤니티의 멤버가 아닌 경우 NotJoinedMemberException 발생")
        void notMemberFail() throws Exception {
            CreateCommentRequest request =
                    new CreateCommentRequest(1L, null, "댓글", List.of());

            doThrow(new NotJoinedMemberException())
                    .when(commentCommandService).createComment(any(CreateCommentRequest.class), anyLong());

            ResultActions result = mvc.perform(
                    post("/api/comments/")
                            .content(mapper.writeValueAsBytes(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(MockHttpSessionCreator.session(9999L))
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("comment/post-NotJoinedMemberException"));
        }

        @Test
        @DisplayName("존재하지 않는 부모 댓글 ID로 요청한 경우 ParentCommentNotFoundException 발생")
        void notExistParentCommentFail() throws Exception {
            CreateCommentRequest request =
                    new CreateCommentRequest(1L, 9999L, "댓글", List.of());

            doThrow(new ParentCommentNotFoundException())
                    .when(commentCommandService).createComment(any(CreateCommentRequest.class), anyLong());

            ResultActions result = mvc.perform(
                    post("/api/comments/")
                            .content(mapper.writeValueAsBytes(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("comment/post-ParentCommentNotFoundException"));
        }

        @Test
        @DisplayName("생성될 댓글의 Depth가 1을 넘은 경우 CommentMaxDepthOverException 발생")
        void depthOverFail() throws Exception {
            CreateCommentRequest request =
                    new CreateCommentRequest(1L, 1L, "댓글", List.of());

            doThrow(new CommentMaxDepthOverException())
                    .when(commentCommandService).createComment(any(CreateCommentRequest.class), anyLong());

            ResultActions result = mvc.perform(
                    post("/api/comments/")
                            .content(mapper.writeValueAsBytes(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("comment/post-CommentMaxDepthOverException"));
        }
    }

    @Nested
    @DisplayName("댓글에 좋아요하기")
    class DoCommentLike {
        @Test
        @DisplayName("댓글에 좋아요를 성공한다.")
        void doCommentLikeSuccess() throws Exception {
            final long NEW_LIKE_ID = 2L;

            given(likeCommandService.doCommentLike(anyLong(), anyLong()))
                    .willReturn(NEW_LIKE_ID);

            ResultActions result = mvc.perform(
                    post("/api/comments/{commentId}/likes", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
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
        @DisplayName("존재하지 않는 댓글 ID로 요청한 경우 CommentNotFoundException 발생")
        void notExistCommentFail() throws Exception {
            doThrow(new CommentNotFoundException())
                    .when(likeCommandService).doCommentLike(anyLong(), anyLong());

            ResultActions result = mvc.perform(
                    post("/api/comments/{commentId}/likes", 9999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("comment/post-commentId-likes-CommentNotFoundException"));
        }

        @Test
        @DisplayName("해당 커뮤니티의 멤버가 아닌 경우 NotJoinedMemberException 발생")
        void notMemberFail() throws Exception {
            doThrow(new NotJoinedMemberException())
                    .when(likeCommandService).doCommentLike(anyLong(), anyLong());

            ResultActions result = mvc.perform(
                    post("/api/comments/{commentId}/likes", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("comment/post-commentId-likes-NotJoinedMemberException"));
        }

        @Test
        @DisplayName("이미 해당 댓글에 좋아요를 한 경우 AlreadyDoCommentLikeException 발생")
        void alreadyDoLikeFail() throws Exception {
            doThrow(new AlreadyDoCommentLikeException())
                    .when(likeCommandService).doCommentLike(anyLong(), anyLong());

            ResultActions result = mvc.perform(
                    post("/api/comments/{commentId}/likes", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("comment/post-commentId-likes-AlreadyDoCommentLikeException"));
        }
    }

    @Nested
    @DisplayName("댓글 삭제")
    class DeleteComment {
        @Test
        @DisplayName("댓글 삭제에 성공한다.")
        void deleteCommentSuccess() throws Exception {
            ResultActions result = mvc.perform(
                    delete("/api/comments/{commentId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            verify(commentCommandService, times(1)).deleteComment(anyLong(), anyLong());

            result
                    .andExpect(status().isOk())
                    .andDo(document("comment/delete-commentId",
                            pathParameters(parameterWithName("commentId").description("삭제할 댓글 ID"))
                    ));
        }

        @Test
        @DisplayName("존재하지 않는 댓글 ID로 요청할 경우 CommentNotFoundException 발생")
        void notExistCommentFail() throws Exception {
            doThrow(new CommentNotFoundException())
                    .when(commentCommandService).deleteComment(anyLong(), anyLong());

            ResultActions result = mvc.perform(
                    delete("/api/comments/{commentId}", 9999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("comment/delete-commentId-CommentNotFoundException"));
        }

        @Test
        @DisplayName("댓글이 달린 커뮤니티의 멤버가 아닌 경우 NotJoinedMemberException 발생")
        void notMemberFail() throws Exception {
            doThrow(new NotJoinedMemberException())
                    .when(commentCommandService).deleteComment(anyLong(), anyLong());

            ResultActions result = mvc.perform(
                    delete("/api/comments/{commentId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("comment/delete-commentId-NotJoinedMemberException"));
        }

        @Test
        @DisplayName("작성자 본인이거나 해당 커뮤니티의 관리자가 아닐 경우 CanNotDeleteCommentException 발생")
        void canNotDeletableMemberFail() throws Exception {
            doThrow(new CanNotDeleteCommentException())
                    .when(commentCommandService).deleteComment(anyLong(), anyLong());

            ResultActions result = mvc.perform(
                    delete("/api/comments/{commentId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("comment/delete-commentId-CanNotDeleteCommentException"));
        }
    }

    @Nested
    @DisplayName("댓글에 좋아요한 멤버 목록 조회")
    class GetLikeMembersAtComment {
        @Test
        @DisplayName("댓글에 좋아요한 멤버 목록 조회에 성공한다.")
        void getLikeMembersAtCommentSuccess() throws Exception {
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

            ResultActions result = mvc.perform(
                    get("/api/comments/{commentId}/likes", 1L)
                            .queryParam("page", "0")
                            .queryParam("size", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
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

        @Test
        @DisplayName("존재하지 않는 댓글 ID로 요청한 경우 CommentNotFoundException 발생")
        void notExistCommentFail() throws Exception {
            doThrow(new CommentNotFoundException())
                    .when(likeQueryService).getLikeMembersAtComment(anyLong(), anyLong(), any(Pageable.class));

            ResultActions result = mvc.perform(
                    get("/api/comments/{commentId}/likes", 9999L)
                            .queryParam("page", "0")
                            .queryParam("size", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("comment/get-commentId-likes-CommentNotFoundException"));
        }

        @Test
        @DisplayName("비공개 커뮤니티에 비가입 멤버로 요청할시 NotViewableMemberException 발생")
        void notViewableMemberFail() throws Exception {
            doThrow(new NotViewableMemberException())
                    .when(likeQueryService).getLikeMembersAtComment(anyLong(), anyLong(), any(Pageable.class));

            ResultActions result = mvc.perform(
                    get("/api/comments/{commentId}/likes", 1L)
                            .queryParam("page", "0")
                            .queryParam("size", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("comment/get-commentId-likes-NotViewableMemberException"));
        }
    }
}