package boogi.apiserver.domain.hashtag.community.domain;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.model.TimeBaseEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "COMMUNITY_HASHTAG")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommunityHashtag extends TimeBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "community_hashtag_id")
    private Long id;

    @JoinColumn(name = "community_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Community community;

    private String tag;

    private CommunityHashtag(String tag, Community community) {
        this.community = community;
        this.community.addHashtag(this);
        this.tag = tag;
    }

    public static CommunityHashtag of(String tag, Community community) {
        return new CommunityHashtag(tag, community);
    }
}
