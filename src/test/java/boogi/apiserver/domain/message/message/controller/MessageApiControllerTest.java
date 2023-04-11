package boogi.apiserver.domain.message.message.controller;

import boogi.apiserver.domain.message.message.application.MessageCommandService;
import boogi.apiserver.domain.message.message.application.MessageQueryService;
import boogi.apiserver.domain.message.message.dto.request.SendMessageRequest;
import boogi.apiserver.domain.message.message.dto.response.MessageResponse;
import boogi.apiserver.domain.message.message.dto.response.MessageRoomResponse;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import boogi.apiserver.domain.user.exception.UserNotFoundException;
import boogi.apiserver.global.constant.HeaderConst;
import boogi.apiserver.global.dto.PaginationDto;
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
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(MessageApiController.class)
class MessageApiControllerTest extends TestControllerSetUp {

    @MockBean
    MessageCommandService messageCommandService;

    @MockBean
    MessageQueryService messageQueryService;

    @Nested
    @DisplayName("쪽지 전송")
    class SendMessage {
        @Test
        @DisplayName("쪽지 전송에 성공한다.")
        void sendMessageSuccess() throws Exception {
            final long NEW_MESSAGE_ID = 2L;
            SendMessageRequest request = new SendMessageRequest(1L, "쪽지");

            given(messageCommandService.sendMessage(any(SendMessageRequest.class), anyLong()))
                    .willReturn(NEW_MESSAGE_ID);

            ResultActions result = mvc.perform(
                    post("/api/messages/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .content(mapper.writeValueAsBytes(request))
            );

            result.andExpect(status().isOk())
                    .andDo(document("messages/post",
                            requestFields(
                                    fieldWithPath("receiverId").type(JsonFieldType.NUMBER)
                                            .description("수신할 유저 ID"),
                                    fieldWithPath("content").type(JsonFieldType.STRING)
                                            .description("쪽지 내용")
                                            .attributes(key("constraint").value("1 ~ 255 길이의 문자열"))
                            ),
                            responseFields(
                                    fieldWithPath("id").type(JsonFieldType.NUMBER)
                                            .description("생성된 쪽지 ID")
                            )
                    ));
        }

        @Test
        @DisplayName("존재하지 않는 수신 유저 ID로 요청한 경우 UserNotFoundException 발생")
        void notExistReceiverFail() throws Exception {
            SendMessageRequest request = new SendMessageRequest(1L, "쪽지");

            doThrow(new UserNotFoundException())
                    .when(messageCommandService).sendMessage(any(SendMessageRequest.class), anyLong());

            ResultActions result = mvc.perform(
                    post("/api/messages/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
                            .content(mapper.writeValueAsBytes(request))
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("messages/post-UserNotFoundException"));
        }
    }

    @Test
    @DisplayName("대화방 목록 조회에 성공한다.")
    void getMessageRoomsSuccess() throws Exception {
        MessageRoomResponse.RecentMessageDto recentMessage =
                new MessageRoomResponse.RecentMessageDto("쪽지", LocalDateTime.now());

        MessageRoomResponse.MessageRoomDto messageRoom =
                new MessageRoomResponse.MessageRoomDto(1L, "유저", "#0001", "url", recentMessage);

        MessageRoomResponse response = new MessageRoomResponse(List.of(messageRoom));

        given(messageQueryService.getMessageRooms(anyLong()))
                .willReturn(response);

        ResultActions result = mvc.perform(
                get("/api/messages/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .session(dummySession)
                        .header(HeaderConst.AUTH_TOKEN, TOKEN)
        );

        result
                .andExpect(status().isOk())
                .andDo(document("messages/get",
                        responseFields(
                                fieldWithPath("messageRooms").type(JsonFieldType.ARRAY)
                                        .description("대화방 목록"),
                                fieldWithPath("messageRooms[].id").type(JsonFieldType.NUMBER)
                                        .description("상대방 유저 ID"),
                                fieldWithPath("messageRooms[].name").type(JsonFieldType.STRING)
                                        .description("유저 이름"),
                                fieldWithPath("messageRooms[].tagNum").type(JsonFieldType.STRING)
                                        .description("태그 번호"),
                                fieldWithPath("messageRooms[].profileImageUrl").type(JsonFieldType.STRING)
                                        .description("프로필 이미지 url"),
                                fieldWithPath("messageRooms[].recentMessage").type(JsonFieldType.OBJECT)
                                        .description("가장 최근 쪽지"),
                                fieldWithPath("messageRooms[].recentMessage.content").type(JsonFieldType.STRING)
                                        .description("쪽지 내용"),
                                fieldWithPath("messageRooms[].recentMessage.receivedAt").type(JsonFieldType.STRING)
                                        .description("쪽지 수신 일시")
                        )
                ));
    }

    @Nested
    @DisplayName("상대방과의 대화 목록 조회")
    class GetMessages {
        @Test
        @DisplayName("상대방과의 대화 목록을 페이지네이션해서 조회한다")
        void getMessagesSuccess() throws Exception {
            UserBasicProfileDto userDto = new UserBasicProfileDto(1L, "url", "#0001", "유저");

            MessageResponse.MessageDto messageDto =
                    new MessageResponse.MessageDto(1L, "쪽지", LocalDateTime.now(), true);

            PaginationDto paginationDto = new PaginationDto(1, false);

            MessageResponse response = new MessageResponse(userDto, List.of(messageDto), paginationDto);

            given(messageQueryService.getMessagesByOpponentId(anyLong(), anyLong(), any(Pageable.class)))
                    .willReturn(response);

            ResultActions result = mvc.perform(
                    get("/api/messages/{opponentId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().isOk())
                    .andDo(document("messages/get-opponentId",
                            pathParameters(
                                    parameterWithName("opponentId").description("상대방 ID")
                            ),
                            responseFields(
                                    fieldWithPath("user").type(JsonFieldType.OBJECT)
                                            .description("쪽지 상대방 유저 정보"),
                                    fieldWithPath("user.id").type(JsonFieldType.NUMBER)
                                            .description("상대방 유저 ID"),
                                    fieldWithPath("user.profileImageUrl").type(JsonFieldType.STRING)
                                            .description("유저 프로필 url").optional(),
                                    fieldWithPath("user.tagNum").type(JsonFieldType.STRING)
                                            .description("태그 번호"),
                                    fieldWithPath("user.name").type(JsonFieldType.STRING)
                                            .description("유저 이름"),
                                    fieldWithPath("messages").type(JsonFieldType.ARRAY)
                                            .description("메시지 목록"),
                                    fieldWithPath("messages[].id").type(JsonFieldType.NUMBER)
                                            .description("메시지 ID"),
                                    fieldWithPath("messages[].content").type(JsonFieldType.STRING)
                                            .description("메시지 내용"),
                                    fieldWithPath("messages[].receivedAt").type(JsonFieldType.STRING)
                                            .description("메시지 수신 일시"),
                                    fieldWithPath("messages[].me").type(JsonFieldType.BOOLEAN)
                                            .description("본인이 송신한 메시지일때 true"),
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
        @DisplayName("메시지 수신 유저가 존재하지 않는 경우 UserNotFoundException 발생")
        void notExistUserFail() throws Exception {
            doThrow(new UserNotFoundException())
                    .when(messageQueryService).getMessagesByOpponentId(anyLong(), anyLong(), any(Pageable.class));

            ResultActions result = mvc.perform(
                    get("/api/messages/{opponentId}", 9999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .session(dummySession)
                            .header(HeaderConst.AUTH_TOKEN, TOKEN)
            );

            result
                    .andExpect(status().is4xxClientError())
                    .andDo(document("messages/get-opponentId-UserNotFoundException"));
        }
    }
}