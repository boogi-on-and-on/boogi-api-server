package boogi.apiserver.domain.member.application;

import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.exception.AlreadyJoinedMemberException;
import boogi.apiserver.domain.member.exception.NotAuthorizedMemberException;
import boogi.apiserver.domain.member.exception.NotJoinedMemberException;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.error.exception.InvalidValueException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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

    @Test
    void 이미_가입한_멤버() {
        //given
        given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                .willReturn(Member.builder().build());

        //then
        assertThatThrownBy(() -> {
            //when
            memberValidationService.checkAlreadyJoinedMember(anyLong(), anyLong());
        }).isInstanceOf(AlreadyJoinedMemberException.class);
    }

    @Test
    void 멤버의_해당_커뮤니티_가입여부() {
        //given
        given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                .willReturn(null);

        assertThatThrownBy(() -> {
            //when
            memberValidationService.checkMemberJoinedCommunity(anyLong(), anyLong() + 1);
        }).isInstanceOf(NotJoinedMemberException.class);
    }

    void 가입하지_않은_멤버가_접근() {
        given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                .willReturn(null);

        assertThatThrownBy(() -> {
            memberValidationService.hasAuth(anyLong(), anyLong(), MemberType.SUB_MANAGER);
        }).isInstanceOf(InvalidValueException.class);
    }

    @Test
    void 멤버가_권한이_없을경우() {
        Member member = Member
                .builder()
                .memberType(MemberType.NORMAL)
                .build();

        given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                .willReturn(member);

        assertThatThrownBy(() -> {
            memberValidationService.hasAuth(anyLong(), anyLong(), MemberType.SUB_MANAGER);
        }).isInstanceOf(NotAuthorizedMemberException.class);
    }

    @Test
    void 부매니저권한이_있는경우() {
        Member member = Member
                .builder()
                .memberType(MemberType.MANAGER)
                .build();


        given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                .willReturn(member);

        boolean isSupervisor = memberValidationService.hasAuth(anyLong(), anyLong(), MemberType.SUB_MANAGER);

        assertThat(isSupervisor).isTrue();
    }

    @Test
    void 매니저권한이_있는경우() {
        Member member = Member
                .builder()
                .memberType(MemberType.MANAGER)
                .build();


        given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                .willReturn(member);

        boolean isSupervisor = memberValidationService.hasAuth(anyLong(), anyLong(), MemberType.MANAGER);

        assertThat(isSupervisor).isTrue();
    }

    @Test
    void 이미_가입한_멤버_배치_이미_있는경우() {
        Member member = Member.builder()
                .id(1L)
                .user(User.builder().id(2L).build())
                .build();
        given(memberRepository.findAlreadyJoinedMemberByUserId(any(), anyLong()))
                .willReturn(List.of(member));

        assertThatThrownBy(() -> {
            memberValidationService.checkAlreadyJoinedMemberInBatch(List.of(2L), 1L);
        }).isInstanceOf(InvalidValueException.class);
    }
}