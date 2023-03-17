package boogi.apiserver.domain.like.api;

import boogi.apiserver.domain.like.application.LikeCommandService;
import boogi.apiserver.global.constant.HeaderConst;
import boogi.apiserver.utils.controller.MockHttpSessionCreator;
import boogi.apiserver.utils.controller.TestControllerSetUp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.ResultActions;

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

    @Test
    @DisplayName("좋아요 취소하기")
    void testDoUnlike() throws Exception {
        MockHttpSession session = MockHttpSessionCreator.dummySession();

        ResultActions result = mvc.perform(
                delete("/api/likes/{likeId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .session(session)
                        .header(HeaderConst.AUTH_TOKEN, "AUTH_TOKEN")
        );

        result
                .andExpect(status().isOk())
                .andDo(document("like/delete",
                        pathParameters(
                                parameterWithName("likeId").description("좋아요 ID")
                        )
                ));
    }
}