package boogi.apiserver.utils.fixture;

import boogi.apiserver.domain.post.post.dto.request.CreatePostRequest;

import java.util.ArrayList;
import java.util.List;

import static boogi.apiserver.utils.fixture.CommunityFixture.POCS_ID;
import static boogi.apiserver.utils.fixture.HttpMethodFixture.*;
import static boogi.apiserver.utils.fixture.TokenFixture.*;

public enum PostFixture {
    ;


    public static long createNewPost() {
        CreatePostRequest request = new CreatePostRequest(POCS_ID, "같이 모각코 하실분 구합니다.",
                List.of("모각코", "모집"), new ArrayList<>(), new ArrayList<>());

        return httpPost(request, "/posts/", getSundoToken())
                .body().jsonPath().getLong("id");
    }
}
