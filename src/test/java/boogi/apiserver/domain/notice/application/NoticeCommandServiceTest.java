package boogi.apiserver.domain.notice.application;


import boogi.apiserver.builder.TestCommunity;
import boogi.apiserver.domain.community.community.repository.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.notice.repository.NoticeRepository;
import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.domain.notice.dto.request.NoticeCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NoticeCommandServiceTest {
    @Mock
    NoticeRepository noticeRepository;

    @Mock
    CommunityRepository communityRepository;

    @Mock
    MemberQueryService memberQueryService;

    @InjectMocks
    NoticeCommandService noticeCommandService;


    @Test
    @DisplayName("생성 성공")
    void success() {
        final Community community = TestCommunity.builder().id(1L).build();
        given(communityRepository.findCommunityById(anyLong()))
                .willReturn(community);

        NoticeCreateRequest request = new NoticeCreateRequest(1L, "A".repeat(10), "B".repeat(10));

        Long noticeId = noticeCommandService.createNotice(request, 2L);

        then(memberQueryService).should(times(1))
                .getOperator(2L, 1L);

        verify(noticeRepository).save(any(Notice.class));
    }
}