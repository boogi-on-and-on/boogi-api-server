package boogi.apiserver.domain.hashtag.community.domain;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.hashtag.domain.Tag;
import boogi.apiserver.domain.model.TimeBaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityHashtag extends TimeBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "community_hashtag_id")
    private Long id;

    @JoinColumn(name = "community_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Community community;

    private Tag tag;

    @Builder
    private CommunityHashtag(final Long id, final Community community, final String tag) {
        this.id = id;
        this.community = community;
        this.tag = new Tag(tag);
    }

    private CommunityHashtag(String tag, Community community) {
        this.community = community;
        this.tag = new Tag(tag);
    }

    public static CommunityHashtag of(String tag, Community community) {
        return new CommunityHashtag(tag, community);
    }

    // 해당 코드는 CommunityHashtags의 메서드를 통해 사용하지 않으면 OneToMany 연관관계상 문제가 생길 수 있습니다.
    public static List<CommunityHashtag> listOf(List<String> tags, Community community) {
        return tags.stream()
                .map(tag -> CommunityHashtag.of(tag, community))
                .collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public Community getCommunity() {
        return community;
    }

    public String getTag() {
        return tag.getValue();
    }
}
