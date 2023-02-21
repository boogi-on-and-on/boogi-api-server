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

    @Embedded
    private Title title;

    @Embedded
    private Content content;


    @Builder
    private Notice(Long id, Community community, Member member, String title, String content) {
        this.id = id;
        this.community = community;
        this.member = member;
        this.title = new Title(title);
        this.content = new Content(content);
    }

    public static Notice of(String content, String title, Member member, Community community) {
        return Notice.builder()
                .content(content)
                .title(title)
                .member(member)
                .community(community)
                .build();
    }

    public Long getId() {
        return id;
    }

    public Community getCommunity() {
        return community;
    }

    public Member getMember() {
        return member;
    }

    public String getTitle() {
        return title.getValue();
    }

    public String getContent() {
        return content.getValue();
    }
}
