package boogi.apiserver.domain.community.community.application;

import boogi.apiserver.builder.TestCommunity;
import boogi.apiserver.builder.TestMember;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.domain.CommunityCategory;
import boogi.apiserver.domain.community.community.dto.request.CommunitySettingRequest;
import boogi.apiserver.domain.community.community.dto.request.CreateCommunityRequest;
import boogi.apiserver.domain.community.community.exception.AlreadyExistsCommunityNameException;
import boogi.apiserver.domain.community.community.exception.CanNotDeleteCommunityException;
import boogi.apiserver.domain.member.application.MemberCommandService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.user.dao.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class CommunityCommandServiceTest {

    @Mock
    CommunityRepository communityRepository;

    @Mock
    MemberRepository memberRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    MemberQueryService memberQueryService;

    @Mock
    MemberCommandService memberCommandService;

    @InjectMocks
    CommunityCommandService communityCommandService;

    @Nested
    @DisplayName("커뮤니티 생성 테스트")
    class CreateCommunity {
        final CreateCommunityRequest request = new CreateCommunityRequest("커뮤니티 이름", CommunityCategory.ACADEMIC.toString(),
                "커뮤니티 소개란 입니다", List.of("태그1"), true, true);

        @Test
        @DisplayName("AlreadyExistsCommunityNameException 리턴")
        void AlreadyExistsCommunityName() {
            final Community community = TestCommunity.builder().build();
            given(communityRepository.findByCommunityNameEquals(any()))
                    .willReturn(Optional.of(community));

            assertThatThrownBy(() -> {
                communityCommandService.createCommunity(request, anyLong());
            }).isInstanceOf(AlreadyExistsCommunityNameException.class);
        }

        @Test
        @DisplayName("성공")
        void success() {
            communityCommandService.createCommunity(request, anyLong());

            then(communityRepository).should(times(1)).save(any());
            then(memberCommandService).should(times(1)).joinMember(anyLong(), any(), any());
        }
    }

    @Test
    @DisplayName("커뮤니티 업데이트 테스트 성공")
    void updateCommunity() {
        final Community community = mock(Community.class);
        given(communityRepository.findByCommunityId(anyLong())).willReturn(community);

        communityCommandService.updateCommunity(1L, anyLong(), "커뮤니티의 소개란입니다", List.of("태그1", "태그2"));

        then(community).should(times(1)).updateCommunity("커뮤니티의 소개란입니다", List.of("태그1", "태그2"));
    }

    @Nested
    @DisplayName("커뮤니티 폐쇄 테스트")
    class ShutdownCommunityTest {

        @Test
        @DisplayName("CanNotDeleteCommunityException 리턴")
        void notOnlyOneMember() {
            given(memberRepository.findAnyMemberExceptManager(anyLong()))
                    .willReturn(Optional.of(TestMember.builder().build()));

            assertThatThrownBy(() -> {
                communityCommandService.shutdown(1L, anyLong());
            }).isInstanceOf(CanNotDeleteCommunityException.class);
        }

        @Test
        @DisplayName("성공")
        void success() {
            final Community community = mock(Community.class);
            given(communityRepository.findByCommunityId(any()))
                    .willReturn(community);

            given(memberRepository.findAnyMemberExceptManager(any()))
                    .willReturn(Optional.empty());

            communityCommandService.shutdown(1L, anyLong());
            then(community).should(times(1)).shutdown();
        }
    }

    @Test
    @DisplayName("커뮤니티 설정정보 변경 테스트")
    void changeSetting() {
        final Member member = TestMember.builder()
                .memberType(MemberType.MANAGER)
                .build();
        given(memberQueryService.getMember(any(), anyLong()))
                .willReturn(member);

        final Community community = mock(Community.class);
        given(communityRepository.findByCommunityId(anyLong()))
                .willReturn(community);

        final CommunitySettingRequest request = new CommunitySettingRequest(true, false);
        communityCommandService.changeSetting(anyLong(), anyLong(), request);

        then(community).should(times(1))
                .switchPrivate(true, MemberType.MANAGER);
        then(community).should(times(1))
                .switchAutoApproval(false, MemberType.MANAGER);
    }
}