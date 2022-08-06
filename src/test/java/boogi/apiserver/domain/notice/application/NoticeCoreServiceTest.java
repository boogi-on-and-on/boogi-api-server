package boogi.apiserver.domain.notice.application;


import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.notice.dao.NoticeRepository;
import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.global.error.exception.InvalidValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class NoticeCoreServiceTest {
    @Mock
    NoticeRepository noticeRepository;

    @Mock
    CommunityQueryService communityQueryService;

    @Mock
    MemberQueryService memberQueryService;

    @InjectMocks
    NoticeCoreService noticeCoreService;

    @Nested
    @DisplayName("공지사항 생성 테스트")
    class CreateNoticeTest {

        @Test
        @DisplayName("생성 권한 없는 경우")
        void hasNoAuth() {
            given(memberQueryService.getMemberOfTheCommunity(anyLong(), anyLong()))
                    .willReturn(Member.builder().memberType(MemberType.NORMAL).build());

            assertThatThrownBy(() -> {
                noticeCoreService.create(Map.of(), anyLong(), anyLong());
            })
                    .isInstanceOf(InvalidValueException.class)
                    .hasMessage("관리자가 아닙니다.");
        }

        @Test
        @DisplayName("생성 성공")
        void success() {
            Member member = Member.builder().memberType(MemberType.SUB_MANAGER).build();
            given(memberQueryService.getMemberOfTheCommunity(anyLong(), anyLong()))
                    .willReturn(member);

            Community community = Community.builder().build();
            given(communityQueryService.getCommunity(anyLong()))
                    .willReturn(community);

            Notice notice = noticeCoreService.create(
                    Map.of("content", "내용",
                            "title", "제목"
                    ),
                    anyLong(),
                    anyLong()
            );

            assertThat(notice.getContent()).isEqualTo("내용");
            assertThat(notice.getTitle()).isEqualTo("제목");
            assertThat(notice.getCommunity()).isEqualTo(community);
            assertThat(notice.getMember()).isEqualTo(member);
        }
    }
}