package boogi.apiserver.domain.message.message.acceptance;

import boogi.apiserver.domain.message.message.dto.request.SendMessageRequest;
import boogi.apiserver.domain.user.dto.request.BlockMessageUsersRequest;
import boogi.apiserver.utils.AcceptanceTest;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static boogi.apiserver.utils.fixture.HttpMethodFixture.httpGet;
import static boogi.apiserver.utils.fixture.HttpMethodFixture.httpPost;
import static boogi.apiserver.utils.fixture.TokenFixture.getSundoToken;
import static boogi.apiserver.utils.fixture.TokenFixture.getYongjinToken;
import static boogi.apiserver.utils.fixture.UserFixture.SUNDO_ID;
import static boogi.apiserver.utils.fixture.UserFixture.YONGJIN_ID;
import static org.assertj.core.api.Assertions.assertThat;

public class MessageAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("쪽지를 보내면 상대방 쪽지 목록과 내 쪽지 목록에 추가된다.")
    void sendMessage() {
        SendMessageRequest request = new SendMessageRequest(YONGJIN_ID, "안녕 나는 선도야");
        ExtractableResponse<Response> sendResponse = httpPost(request, "/messages/", getSundoToken());

        ExtractableResponse<Response> senderResponse = httpGet("/messages/" + YONGJIN_ID, getSundoToken());
        ExtractableResponse<Response> receiverResponse = httpGet("/messages/" + SUNDO_ID, getYongjinToken());

        List<Object> senderMessages = senderResponse.body().jsonPath().getList("messages");
        List<Object> receiverMessages = receiverResponse.body().jsonPath().getList("messages");

        assertThat(sendResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(senderMessages).extracting("content")
                .containsExactly("안녕 나는 선도야");
        assertThat(receiverMessages).extracting("content")
                .containsExactly("안녕 나는 선도야");
    }

    @Test
    @DisplayName("상대방 한 명에 대해 가장 최근 쪽지 1개만 조회한다.")
    void getMessageRoom() {
        SendMessageRequest request1 = new SendMessageRequest(YONGJIN_ID, "안녕 나는 선도야");
        httpPost(request1, "/messages/", getSundoToken());

        SendMessageRequest request2 = new SendMessageRequest(SUNDO_ID, "안녕안녕");
        httpPost(request2, "/messages/", getYongjinToken());

        ExtractableResponse<Response> response = httpGet("/messages/", getSundoToken());

        List<String> messageContent =
                response.body().jsonPath().getList("messageRooms.recentMessage.content", String.class);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(messageContent).containsExactly("안녕안녕");
    }

    @Test
    @DisplayName("차단한 유저의 쪽지는 조회하지 않는다.")
    void getMessagesWithBlockedUser() {
        BlockMessageUsersRequest request = new BlockMessageUsersRequest(List.of(YONGJIN_ID));
        httpPost(request, "/users/messages/block", getSundoToken());

        SendMessageRequest sendRequest = new SendMessageRequest(SUNDO_ID, "안녕안녕");
        httpPost(sendRequest, "/messages/", getYongjinToken());

        ExtractableResponse<Response> response = httpGet("/messages/", getSundoToken());

        List<Object> messageRooms = response.body().jsonPath().getList("messageRooms");

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(messageRooms).isEmpty();
    }
}