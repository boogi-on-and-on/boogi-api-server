package boogi.apiserver.builder;

import boogi.apiserver.domain.notice.domain.Notice;

public class TestNotice {
    public static Notice.NoticeBuilder builder() {
        return Notice.builder()
                .title("테스트 공지사항 제목")
                .content("테스트 공지사항 내용");
    }
}
