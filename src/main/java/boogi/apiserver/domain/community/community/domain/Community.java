package boogi.apiserver.domain.community.community.domain;

import boogi.apiserver.domain.hashtag.community.domain.CommunityHashtag;
import boogi.apiserver.domain.model.TimeBaseEntity;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "COMMUNITY")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Community extends TimeBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "community_id")
    private Long id;

    @Column(name = "community_name")
    private String communityName;

    private String description;

    @Column(name = "private")
    private boolean isPrivate;

    // 카테고리 --> enum
    @Enumerated(value = EnumType.STRING)
    private CommunityCategory category;

    @Column(name = "member_count")
    private int memberCount;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "auto_approval")
    private boolean autoApproval;

    //todo: 생성메서드 만들어서 커뮤니티 생성.
    @OneToMany(mappedBy = "community")
    private List<CommunityHashtag> hashtags = new ArrayList<>();

    private Community(String name, String description, boolean isPrivate, boolean autoApproval) {
        this.communityName = name;
        this.description = description;
        this.isPrivate = isPrivate;
        this.autoApproval = autoApproval;
    }

    public static Community of(String name, String description, boolean isPrivate, boolean autoApproval) {
        return new Community(name, description, isPrivate, autoApproval);
    }

    public void addMemberCount() {
        this.memberCount++;
    }
}
