package boogi.apiserver.domain.notice.application;


import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.repository.CommunityRepository;
import boogi.apiserver.domain.member.application.MemberQuery;
import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.domain.notice.dto.request.NoticeCreateRequest;
import boogi.apiserver.domain.notice.repository.NoticeRepository;
import boogi.apiserver.utils.fixture.CommunityFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NoticeCommandTest {
    @Mock
    NoticeRepository noticeRepository;

    @Mock
    CommunityRepository communityRepository;

    @Mock
    MemberQuery memberQuery;

    @InjectMocks
    NoticeCommand noticeCommand;


    @Test
    @DisplayName("생성 성공")
    void success() {
        final Long userId = 1L;
        final Community community = CommunityFixture.POCS.toCommunity(1L, null);
        given(communityRepository.findCommunityById(anyLong()))
                .willReturn(community);

        NoticeCreateRequest request = new NoticeCreateRequest(community.getId(), "A".repeat(10), "B".repeat(10));

        Long noticeId = noticeCommand.createNotice(request, userId);

        then(memberQuery).should(times(1))
                .getOperator(userId, community.getId());

        verify(noticeRepository).save(any(Notice.class));
    }
}