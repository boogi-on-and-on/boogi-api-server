package boogi.apiserver.builder;

import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequestStatus;

public class TestJoinRequest {
    public static JoinRequest.JoinRequestBuilder builder() {
        return JoinRequest.builder()
                .status(JoinRequestStatus.PENDING);
    }
}
