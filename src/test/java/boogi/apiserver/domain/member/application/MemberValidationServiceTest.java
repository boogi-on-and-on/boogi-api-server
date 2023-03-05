package boogi.apiserver.domain.member.application;

import boogi.apiserver.builder.TestMember;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.exception.AlreadyJoinedMemberException;
import boogi.apiserver.domain.member.exception.NotAuthorizedMemberException;
import boogi.apiserver.domain.member.exception.NotJoinedMemberException;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.error.exception.InvalidValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberValidationServiceTest {

    @InjectMocks
    MemberValidationService memberValidationService;

    @Mock
    MemberRepository memberRepository;

    @Nested
    @DisplayName("멤버의 가입여부 테스트")
    class CheckMemberJoinTest {
        @Test
        @DisplayName("이미 가입한 멤버인 경우")
        void alreadyJoined() {
            //given

            final Member member = TestMember.builder().build();

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member));

            //then
            assertThatThrownBy(() -> {
                //when
                memberValidationService.checkAlreadyJoinedMember(anyLong(), anyLong());
            }).isInstanceOf(AlreadyJoinedMemberException.class);
        }

        @Test
        @DisplayName("가입하지 않은 경우")
        void yetJoined() {
            //given
            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> {
                //when
                memberValidationService.checkMemberJoinedCommunity(anyLong(), anyLong() + 1);
            }).isInstanceOf(NotJoinedMemberException.class);
        }
    }


    @Nested
    @DisplayName("커뮤니티 멤버의 권한 체크 테스트")
    class MemberTypeTest {
        @Test
        @DisplayName("멤버가 관리자 권한 없는 경우")
        void NoMemberAuth() {
            final Member member = TestMember.builder()
                    .memberType(MemberType.NORMAL)
                    .build();

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member));

            assertThatThrownBy(() -> {
                memberValidationService.hasAuth(anyLong(), anyLong(), MemberType.SUB_MANAGER);
            }).isInstanceOf(NotAuthorizedMemberException.class);
        }

        @Test
        @DisplayName("부매니저이상의 권한이 있는 경우")
        void greaterThanSubManagerAuth() {
            final Member member = TestMember.builder()
                    .memberType(MemberType.MANAGER)
                    .build();

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member));

            boolean isSupervisor = memberValidationService.hasAuth(anyLong(), anyLong(), MemberType.SUB_MANAGER);

            assertThat(isSupervisor).isTrue();
        }

        @Test
        @DisplayName("매니저 권한이 있는 경우")
        void hasManagerAuth() {
            final Member member = TestMember.builder()
                    .memberType(MemberType.MANAGER)
                    .build();

            given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                    .willReturn(Optional.of(member));

            boolean isSupervisor = memberValidationService.hasAuth(anyLong(), anyLong(), MemberType.MANAGER);

            assertThat(isSupervisor).isTrue();
        }
    }

    @Test
    @DisplayName("이미 가입한 멤버가 있는경우(배치 가입)")
    void alreadyJoinedMemberInBatch() {
        final User user = TestUser.builder()
                .id(2L)
                .build();

        final Member member = TestMember.builder()
                .id(1L)
                .user(user)
                .build();

        given(memberRepository.findAlreadyJoinedMemberByUserId(any(), anyLong()))
                .willReturn(List.of(member));

        assertThatThrownBy(() -> {
            memberValidationService.checkAlreadyJoinedMemberInBatch(List.of(2L), 1L);
        }).isInstanceOf(InvalidValueException.class);
    }
}