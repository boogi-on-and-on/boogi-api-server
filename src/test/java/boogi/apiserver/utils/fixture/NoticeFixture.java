package boogi.apiserver.utils.fixture;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.utils.TestTimeReflection;

import java.time.LocalDateTime;

import static boogi.apiserver.utils.fixture.TimeFixture.STANDARD;

public enum NoticeFixture {
    NOTICE1("이번주 공지사항입니다", "공학관으로 모두 모여시기 바랍니다.", STANDARD),
    NOTICE2("신입생 환영회를 진행합니다.", "신입생 여러분은 필참하시기 바랍니다.", STANDARD.minusDays(1)),
    ;

    public final String title;
    public final String content;
    public final LocalDateTime createdAt;

    NoticeFixture(String title, String content, LocalDateTime createdAt) {
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Notice toNotice(Community community, Member member) {
        return toNotice(null, community, member);
    }

    public Notice toNotice(Long id, Community community, Member member) {
        Notice notice = Notice.builder()
                .id(id)
                .title(this.title)
                .content(this.content)
                .community(community)
                .member(member)
                .build();
        TestTimeReflection.setCreatedAt(notice, this.createdAt);
        return notice;
    }
}
