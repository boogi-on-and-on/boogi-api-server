package boogi.apiserver.domain.member.application;

import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.exception.AlreadyJoinedMemberException;
import boogi.apiserver.domain.member.exception.NotJoinedMemberException;
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
                .willReturn(List.of(Member.builder().build()));

        //then
        assertThatThrownBy(() -> {
            //when
            memberValidationService.checkAlreadyJoinedMember(anyLong(), anyLong());
        }).isInstanceOf(AlreadyJoinedMemberException.class);
    }

    @Test
    void 가입한_멤버_없는_경우() {
        //given
        given(memberRepository.findByUserIdAndCommunityId(any(), anyLong()))
                .willReturn(List.of());

        //when
        Member member = memberValidationService.checkAlreadyJoinedMember(anyLong(), anyLong());

        //then
        assertThat(member).isNull();
    }

    @Test
    void 멤버의_해당_커뮤니티_가입여부(){
        //given
        given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                .willReturn(List.of(Member.builder().build()));

        assertThatThrownBy(() -> {
            //when
            memberValidationService.checkMemberJoinedCommunity(anyLong(), anyLong() + 1);
        }).isInstanceOf(NotJoinedMemberException.class);
    }
}