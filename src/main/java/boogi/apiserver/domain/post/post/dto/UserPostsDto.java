package boogi.apiserver.domain.post.post.dto;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.post.post.domain.Post;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.stream.Collectors;

public class UserPostsDto {
    private String content;
    private CommunityDto community;
    private String at;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> hashtags;

    public class CommunityDto {
        private String id;
        private String name;

        private CommunityDto(Community community) {
            this.id = community.getId().toString();
            this.name = community.getCommunityName();
        }
    }

    private UserPostsDto(Post post) {
        this.content = post.getContent();
        this.community = new CommunityDto(post.getCommunity());
        this.at = post.getCreatedAt().toString(); //TODO: 시간 유틸함수 만들기
        List<PostHashtag> hashtags = post.getHashtags();
        if (hashtags.size() > 0) {
            this.hashtags = hashtags.stream().map(PostHashtag::getTag).collect(Collectors.toList());
        }
    }

    public static UserPostsDto of(Post post) {
        return new UserPostsDto(post);
    }
}
