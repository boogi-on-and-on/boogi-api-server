package boogi.apiserver.domain.like.api;

import boogi.apiserver.domain.like.application.LikeCommandService;
import boogi.apiserver.domain.like.exception.LikeNotFoundException;
import boogi.apiserver.domain.like.exception.UnmatchedLikeUserException;
import boogi.apiserver.global.constant.HeaderConst;
import boogi.apiserver.utils.controller.TestControllerSetUp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(LikeApiController.class)
class LikeApiControllerTest extends TestControllerSetUp {

    @MockBean
    LikeCommandService likeCommandService;

    @Nested
    @DisplayName("좋아요 취소")
    class DoUnlike {
        @Test
        @DisplayName("좋아요 취소에 성공한다.")
        void doUnlikeSuccess() throws Exception {
            ResultActions result = mvc.perform(
                    delete("/api/likes/{likeId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().isOk())
                    .andDo(document("like/delete",
                            pathParameters(
                                    parameterWithName("likeId").description("좋아요 ID")
                            )
                    ));
        }

        @Test
        @DisplayName("존재하지 않는 좋아요 ID로 요청한 경우 LikeNotFoundException 발생")
        void notExistLikeFail() throws Exception {
            doThrow(new LikeNotFoundException())
                    .when(likeCommandService).doUnlike(anyLong(), anyLong());

            ResultActions result = mvc.perform(
                    delete("/api/likes/{likeId}", 9999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("like/delete-LikeNotFoundException"));
        }

        @Test
        @DisplayName("본인이 한 좋아요가 아닌 경우 UnmatchedLikeUserException 발생")
        void unmatchedLikeUserFail() throws Exception {
            doThrow(new UnmatchedLikeUserException())
                    .when(likeCommandService).doUnlike(anyLong(), anyLong());

            ResultActions result = mvc.perform(
                    delete("/api/likes/{likeId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("like/delete-UnmatchedLikeUserException"));
        }
    }
}