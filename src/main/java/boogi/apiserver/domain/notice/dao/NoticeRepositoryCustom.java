package boogi.apiserver.domain.notice.dao;

import boogi.apiserver.domain.notice.domain.Notice;

import java.util.List;

public interface NoticeRepositoryCustom {
    List<Notice> getLatestNotice(Long communityId);

    List<Notice> getLatestAppNotice();

    List<Notice> getAllNotices(Long communityId);

    List<Notice> getAllAppNotices();
}
