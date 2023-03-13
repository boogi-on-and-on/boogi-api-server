package boogi.apiserver.builder;

import boogi.apiserver.domain.community.community.domain.Community;

import java.util.ArrayList;

public class TestCommunity {

    public static Community.CommunityBuilder builder() {
        return Community.builder()
                .communityName("TEST COMMUNITY")
                .description("COMMUNITY DESCRIPTION")
                .hashtags(new ArrayList<>());
    }
}
