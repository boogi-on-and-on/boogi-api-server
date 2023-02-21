package boogi.apiserver.domain.hashtag.community.domain;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.model.TimeBaseEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "COMMUNITY_HASHTAG")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CommunityHashtag extends TimeBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "community_hashtag_id")
    private Long id;

    @JoinColumn(name = "community_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Community community;

    private String tag;

    @Builder
    private CommunityHashtag(final Long id, final Community community, final String tag) {
        this.id = id;
        this.community = community;
        this.tag = tag;
    }

    private CommunityHashtag(String tag, Community community) {
        this.community = community;
        this.tag = tag;
//        community.getHashtags().add(this); todo
    }

    public static CommunityHashtag of(String tag, Community community) {
        return new CommunityHashtag(tag, community);
    }

}
