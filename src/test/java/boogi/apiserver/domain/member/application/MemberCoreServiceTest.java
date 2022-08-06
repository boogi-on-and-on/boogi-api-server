package boogi.apiserver.domain.member.application;

import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.user.application.UserQueryService;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.error.exception.InvalidValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberCoreServiceTest {

    @InjectMocks
    MemberCoreService memberCoreService;

    @Mock
    MemberRepository memberRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    MemberQueryService memberQueryService;

    @Mock
    MemberValidationService memberValidationService;

    @Mock
    CommunityQueryService communityQueryService;

    @Mock
    UserQueryService userQueryService;

    @Nested
    @DisplayName("멤버 가입 테스트")
    class MemberJoinTest {
        @Test
        @DisplayName("멤버 가입 1명")
        void joinOne() {
            //given
            User user = User.builder()
                    .id(1L)
                    .build();
            given(userQueryService.getUser(anyLong()))
                    .willReturn(user);

            Community community = Community.builder()
                    .id(2L)
                    .build();
            given(communityQueryService.getCommunity(anyLong()))
                    .willReturn(community);

            //when
            Member member = memberCoreService.joinMember(user.getId(), community.getId(), MemberType.MANAGER);

            //then
            assertThat(member.getCommunity().getId()).isEqualTo(community.getId());
            assertThat(member.getUser().getId()).isEqualTo(user.getId());
            assertThat(member.getCommunity().getMemberCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("멤버 2명이상 추가")
        void joinMany() {
            //given
            User u1 = User.builder()
                    .id(1L)
                    .build();
            User u2 = User.builder()
                    .id(2L)
                    .build();

            given(userRepository.findUsersByIds(any()))
                    .willReturn(List.of(u1, u2));

            Community community = Community.builder()
                    .id(1L)
                    .build();
            given(communityQueryService.getCommunity(anyLong()))
                    .willReturn(community);

            //when
            List<Member> members = memberCoreService.joinMemberInBatch(List.of(u1.getId(), u2.getId()), community.getId(), MemberType.NORMAL);

            //then
            assertThat(members.stream().map(m -> m.getUser().getId()).collect(Collectors.toList()))
                    .containsExactlyInAnyOrderElementsOf(List.of(u1.getId(), u2.getId()));

            assertThat(members.stream().map(Member::getMemberType).collect(Collectors.toList()))
                    .containsOnly(MemberType.NORMAL);
        }

    }

    @Nested
    @DisplayName("멤버 차단")
    class BanMember {

        @Test
        @DisplayName("이미 차단한 멤버인 경우")
        void alreadyBanned() {
            Member member = Member.builder()
                    .id(1L)
                    .bannedAt(LocalDateTime.now())
                    .build();


            given(memberQueryService.getMember(anyLong()))
                    .willReturn(member);

            assertThatThrownBy(() -> {
                memberCoreService.banMember(member.getId());
            })
                    .isInstanceOf(InvalidValueException.class)
                    .hasMessage("이미 차단된 멤버입니다.");

        }

        @Test
        @DisplayName("차단 안된 멤버를 차단해제 하는경우")
        void failRelease() {
            //given
            Member member = Member.builder()
                    .id(1L)
                    .build();

            given(memberQueryService.getMember(anyLong()))
                    .willReturn(member);

            //then
            assertThatThrownBy(() -> {
                //when
                memberCoreService.releaseMember(member.getId());
            }).isInstanceOf(InvalidValueException.class);
        }
    }
}