package boogi.apiserver.domain.community.community.domain;

import boogi.apiserver.domain.hashtag.community.domain.CommunityHashtag;
import boogi.apiserver.domain.model.TimeBaseEntity;
import boogi.apiserver.global.error.exception.InvalidValueException;
import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "COMMUNITY")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Where(clause = "deleted_at is null")
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

    public void updateDescription(String description) {
        if (description.length() < 10) {
            throw new InvalidValueException("10글자 이상의 소개란 입력이 필요합니다.");
        }
        this.description = description;
    }

    public void toPublic() {
        this.isPrivate = false;
    }

    public void toPrivate() {
        this.isPrivate = true;
    }

    public void openAutoApproval() {
        this.autoApproval = true;
    }

    public void closeAutoApproval() {
        this.autoApproval = false;
    }

    public void shutdown() {
        this.deletedAt = LocalDateTime.now();
    }

    private Community(String name, String description, boolean isPrivate, boolean autoApproval, CommunityCategory category) {
        this.communityName = name;
        this.description = description;
        this.isPrivate = isPrivate;
        this.autoApproval = autoApproval;
        this.category = category;
    }

    public static Community of(String name, String description, boolean isPrivate, boolean autoApproval, CommunityCategory category) {
        return new Community(name, description, isPrivate, autoApproval, category);
    }

    public void addMemberCount() {
        this.memberCount++;
    }
}
