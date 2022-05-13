package boogi.apiserver.domain.notice.domain;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.model.TimeBaseEntity;
import lombok.*;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "NOTICE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Notice extends TimeBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long id;

    @JoinColumn(name = "community_id")
    @ManyToOne(fetch = LAZY)
    private Community community;

    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = LAZY)
    private Member member;

    private String title;

    private String content;

    private Notice(String content, String title, Member member, Community community) {
        this.title = title;
        this.content = content;
        this.member = member;
        this.community = community;
    }

    public static Notice of(String content, String title, Member member, Community community) {
        return new Notice(content, title, member, community);
    }

}
