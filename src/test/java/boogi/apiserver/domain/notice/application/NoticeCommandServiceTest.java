package boogi.apiserver.domain.notice.application;


import boogi.apiserver.builder.TestCommunity;
import boogi.apiserver.builder.TestMember;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.notice.dao.NoticeRepository;
import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.domain.notice.dto.request.NoticeCreateRequest;
import boogi.apiserver.global.error.exception.InvalidValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

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

    @Nested
    @DisplayName("공지사항 생성 테스트")
    class CreateNoticeTest {

        @Test
        @DisplayName("생성 권한 없는 경우")
        void hasNoAuth() {
            final Member member = TestMember.builder().memberType(MemberType.NORMAL).build();

            given(memberQueryService.getMemberOfTheCommunity(anyLong(), anyLong()))
                    .willReturn(member);

            NoticeCreateRequest request = new NoticeCreateRequest(1L, null, null);

            assertThatThrownBy(() -> {
                noticeCommandService.create(request, 2L);
            })
                    .isInstanceOf(InvalidValueException.class)
                    .hasMessage("관리자가 아닙니다.");
        }

        @Test
        @DisplayName("생성 성공")
        void success() {
            final Member member = TestMember.builder().memberType(MemberType.SUB_MANAGER).build();

            given(memberQueryService.getMemberOfTheCommunity(anyLong(), anyLong()))
                    .willReturn(member);

            final Community community = TestCommunity.builder().id(1L).build();
            given(communityRepository.findByCommunityId(anyLong()))
                    .willReturn(community);

            NoticeCreateRequest request = new NoticeCreateRequest(1L, "A".repeat(10), "B".repeat(10));

            Notice notice = noticeCommandService.create(request, 2L);

            assertThat(notice.getTitle()).isEqualTo("A".repeat(10));
            assertThat(notice.getContent()).isEqualTo("B".repeat(10));
            assertThat(notice.getCommunity()).isEqualTo(community);
            assertThat(notice.getMember()).isEqualTo(member);
        }
    }
}