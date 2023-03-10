package boogi.apiserver.domain.member.application;

import boogi.apiserver.builder.TestCommunity;
import boogi.apiserver.builder.TestMember;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
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
class MemberCommandServiceTest {

    @InjectMocks
    MemberCommandService memberCommandService;

    @Mock
    MemberRepository memberRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    UserQueryService userQueryService;


    @Mock
    CommunityRepository communityRepository;


    @Test
    void 멤버_가입_성공() {
        //given
        final User user = TestUser.builder().id(1L).build();

        given(userRepository.findByUserId(anyLong()))
                .willReturn(user);

        final Community community = TestCommunity.builder().id(2L).build();
        given(communityRepository.findByCommunityId(anyLong()))
                .willReturn(community);

        //when
        Member member = memberCommandService.joinMember(user.getId(), community.getId(), MemberType.MANAGER);

        //then
        assertThat(member.getCommunity().getId()).isEqualTo(community.getId());
        assertThat(member.getUser().getId()).isEqualTo(user.getId());
        assertThat(member.getCommunity().getMemberCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("멤버 2명이상 추가")
    void joinMany() {
        //given
        final User u1 = TestUser.builder().id(1L).build();
        final User u2 = TestUser.builder().id(2L).build();

        given(userRepository.findUsersByIds(any()))
                .willReturn(List.of(u1, u2));

        final Community community = TestCommunity.builder().id(1L).build();

        given(communityRepository.findByCommunityId(anyLong()))
                .willReturn(community);

        //when
        List<Member> members = memberCommandService.joinMembers(List.of(u1.getId(), u2.getId()), community.getId(), MemberType.NORMAL);

        //then
        assertThat(members.stream().map(m -> m.getUser().getId()).collect(Collectors.toList()))
                .containsExactlyInAnyOrderElementsOf(List.of(u1.getId(), u2.getId()));

        assertThat(members.stream().map(Member::getMemberType).collect(Collectors.toList()))
                .containsOnly(MemberType.NORMAL);
    }


    @Nested
    @DisplayName("멤버 차단")
    class BanMember {

        @Test
        @DisplayName("이미 차단한 멤버인 경우")
        void alreadyBanned() {
            final Member member = TestMember.builder()
                    .id(1L)
                    .bannedAt(LocalDateTime.now())
                    .build();

            given(memberRepository.findByMemberId(anyLong()))
                    .willReturn(member);

            assertThatThrownBy(() -> {
                memberCommandService.banMember(2L, member.getId());
            })
                    .isInstanceOf(InvalidValueException.class)
                    .hasMessage("이미 차단된 멤버입니다.");

        }

        @Test
        @DisplayName("차단 안된 멤버를 차단해제 하는경우")
        void failRelease() {
            //given
            final Member member = TestMember.builder()
                    .id(1L)
                    .build();

            given(memberRepository.findByMemberId(anyLong()))
                    .willReturn(member);

            //then
            assertThatThrownBy(() -> {
                //when
                memberCommandService.releaseMember(2L, member.getId());
            }).isInstanceOf(InvalidValueException.class);
        }
    }
}