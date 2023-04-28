package boogi.apiserver.domain.user.acceptance;

import boogi.apiserver.domain.alarm.alarmconfig.dto.dto.AlarmConfigSettingInfoDto;
import boogi.apiserver.domain.alarm.alarmconfig.dto.request.AlarmConfigSettingRequest;
import boogi.apiserver.domain.community.community.dto.dto.JoinedCommunitiesDto;
import boogi.apiserver.domain.community.community.dto.request.JoinRequestIdsRequest;
import boogi.apiserver.domain.message.block.dto.dto.MessageBlockedUserDto;
import boogi.apiserver.domain.user.dto.request.BlockMessageUsersRequest;
import boogi.apiserver.domain.user.dto.request.BlockedUserIdRequest;
import boogi.apiserver.utils.AcceptanceTest;
import boogi.apiserver.utils.fixture.CommunityFixture;
import boogi.apiserver.utils.fixture.UserFixture;
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
import static org.assertj.core.api.Assertions.assertThat;

public class UserAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("커뮤니티 가입 시, 내가 가입한 커뮤니티 목록에 추가된다.")
    void joinCommunity() {
        long requestId = httpPost("/communities/" + CommunityFixture.ENGLISH_ID + "/requests", getSundoToken())
                .body().jsonPath()
                .getLong("id");

        JoinRequestIdsRequest confirmRequest = new JoinRequestIdsRequest(List.of(requestId));
        httpPost(confirmRequest, "/communities/" + CommunityFixture.ENGLISH_ID + "/requests/confirm", getYongjinToken());

        ExtractableResponse<Response> response = httpGet("/users/communities/joined", getSundoToken());
        List<JoinedCommunitiesDto.CommunityInfo> communities = response.body().jsonPath()
                .getList("communities", JoinedCommunitiesDto.CommunityInfo.class);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(communities)
                .extracting("id")
                .contains(CommunityFixture.ENGLISH_ID);
    }

    @Test
    @DisplayName("특정 유저의 메시지를 수신을 차단하고 차단목록에 추가된다.")
    void blockUser() {
        BlockMessageUsersRequest blockRequest = new BlockMessageUsersRequest(List.of(UserFixture.YONGJIN_ID));
        httpPost(blockRequest, "/users/messages/block", getSundoToken());

        ExtractableResponse<Response> response = httpGet("/users/messages/blocked", getSundoToken());
        List<MessageBlockedUserDto> blockedUsers = response.body().jsonPath().getList("blocked", MessageBlockedUserDto.class);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(blockedUsers)
                .extracting("userId")
                .contains(UserFixture.YONGJIN_ID);
    }

    @Test
    @DisplayName("특정 유저의 메시지 차단을 해지하면, 차단 목록에서 제거된다.")
    void unblockUser() {
        BlockMessageUsersRequest blockRequest = new BlockMessageUsersRequest(List.of(UserFixture.YONGJIN_ID));
        httpPost(blockRequest, "/users/messages/block", getSundoToken());

        BlockedUserIdRequest unblockRequest = new BlockedUserIdRequest(UserFixture.YONGJIN_ID);
        httpPost(unblockRequest, "/users/messages/unblock", getSundoToken());

        ExtractableResponse<Response> response = httpGet("/users/messages/blocked", getSundoToken());
        List<MessageBlockedUserDto> blockedUsers = response.body().jsonPath().getList("blocked", MessageBlockedUserDto.class);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(blockedUsers)
                .extracting("userId")
                .doesNotContain(UserFixture.YONGJIN_ID);
    }

    @Test
    @DisplayName("알람 설정 변경 시, 변경된 결과가 조회된다.")
    void changeAlarmConfig() {
        AlarmConfigSettingRequest configureAlarmRequest
                = new AlarmConfigSettingRequest(false, false, false, true, true);
        httpPost(configureAlarmRequest, "/users/config/notifications", getSundoToken());

        ExtractableResponse<Response> response = httpGet("/users/config/notifications", getSundoToken());
        AlarmConfigSettingInfoDto parsed = response.body().jsonPath().getObject("alarmInfo", AlarmConfigSettingInfoDto.class);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        AlarmConfigSettingInfoDto.Personal personal = parsed.getPersonal();
        assertThat(personal.getMessage()).isFalse();

        AlarmConfigSettingInfoDto.Community community = parsed.getCommunity();
        assertThat(community.getNotice()).isFalse();
        assertThat(community.getJoin()).isFalse();

        AlarmConfigSettingInfoDto.Post post = parsed.getPost();
        assertThat(post.getComment()).isTrue();
        assertThat(post.getMention()).isTrue();
    }
}
