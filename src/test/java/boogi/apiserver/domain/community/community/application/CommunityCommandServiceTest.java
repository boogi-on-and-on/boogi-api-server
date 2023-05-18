package boogi.apiserver.domain.community.community.application;

import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.domain.CommunityCategory;
import boogi.apiserver.domain.community.community.dto.request.CommunitySettingRequest;
import boogi.apiserver.domain.community.community.dto.request.CreateCommunityRequest;
import boogi.apiserver.domain.community.community.exception.AlreadyExistsCommunityNameException;
import boogi.apiserver.domain.community.community.exception.CanNotDeleteCommunityException;
import boogi.apiserver.domain.community.community.repository.CommunityRepository;
import boogi.apiserver.domain.member.application.MemberCommandService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.repository.MemberRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.repository.UserRepository;
import boogi.apiserver.utils.fixture.CommunityFixture;
import boogi.apiserver.utils.fixture.MemberFixture;
import boogi.apiserver.utils.fixture.UserFixture;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.BDDMockito.then;
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

    private Community community;
    private Member member;
    private User user;

    @BeforeEach
    public void init() {
        this.community = CommunityFixture.POCS.toCommunity(1L, List.of("태그1"));
        this.user = UserFixture.SUNDO.toUser(2L);
        this.member = MemberFixture.SUNDO_POCS.toMember(3L, user, community);
    }

    @Nested
    @DisplayName("커뮤니티 생성 테스트")
    class CreateCommunity {
        final CreateCommunityRequest request = new CreateCommunityRequest("커뮤니티 이름", CommunityCategory.ACADEMIC.toString(),
                "커뮤니티 소개란 입니다", List.of("태그1"), true, true);

        @Test
        @DisplayName("AlreadyExistsCommunityNameException 리턴")
        void AlreadyExistsCommunityName() {
            //given
            given(communityRepository.findByCommunityNameValueEquals(any()))
                    .willReturn(Optional.of(community));

            //then
            assertThatThrownBy(() -> {
                //when
                communityCommandService.createCommunity(request, user.getId());
            }).isInstanceOf(AlreadyExistsCommunityNameException.class);
        }

        @Test
        @DisplayName("성공")
        void success() {
            //when
            communityCommandService.createCommunity(request, user.getId());

            //then
            then(communityRepository).should(times(1)).save(any());
            then(memberCommandService).should(times(1)).joinMember(anyLong(), any(), any());
        }
    }

    @Test
    @DisplayName("커뮤니티 업데이트 테스트 성공")
    void updateCommunity() {
        //given
        given(communityRepository.findCommunityById(anyLong())).willReturn(community);

        //when
        communityCommandService.updateCommunity(user.getId(), community.getId(), "커뮤니티의 소개란입니다", List.of("태그1", "태그2"));

        //then
        assertThat(community.getDescription()).isEqualTo("커뮤니티의 소개란입니다");
        assertThat(community.getHashtags())
                .extracting("tag")
                .containsExactly("태그1", "태그2");
    }

    @Nested
    @DisplayName("커뮤니티 폐쇄 테스트")
    class ShutdownCommunityTest {

        @Test
        @DisplayName("CanNotDeleteCommunityException 리턴")
        void notOnlyOneMember() {
            //given
            given(memberRepository.findAnyMemberExceptManager(anyLong()))
                    .willReturn(Optional.of(member));

            //then
            assertThatThrownBy(() -> {
                //when
                communityCommandService.shutdown(user.getId(), community.getId());
            }).isInstanceOf(CanNotDeleteCommunityException.class);
        }

        @Test
        @DisplayName("성공")
        void success() {
            //given
            given(communityRepository.findCommunityById(any()))
                    .willReturn(community);

            given(memberRepository.findAnyMemberExceptManager(any()))
                    .willReturn(Optional.empty());

            //when
            communityCommandService.shutdown(user.getId(), community.getId());

            //then
            assertThat(community.getDeletedAt()).isNotNull();
        }
    }

    @Test
    @DisplayName("커뮤니티 설정정보 변경 테스트")
    void changeSetting() {
        //given
        given(memberQueryService.getMember(any(), anyLong()))
                .willReturn(member);

        given(communityRepository.findCommunityById(anyLong()))
                .willReturn(community);

        final CommunitySettingRequest request = new CommunitySettingRequest(true, false);

        //when
        communityCommandService.changeSetting(user.getId(), community.getId(), request);

        //then
        assertThat(community.isPrivate()).isTrue();
        assertThat(community.isAutoApproval()).isFalse();
    }
}