package boogi.apiserver.builder;

import boogi.apiserver.domain.notice.domain.Notice;

public class TestNotice {
    public static Notice.NoticeBuilder builder() {
        return Notice.builder()
                .title("NOTICE TITLE")
                .content("NOTICE CONTENT");
    }
}
