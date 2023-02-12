package boogi.apiserver.domain.member.application;

import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.user.application.UserQueryService;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.error.exception.InvalidValueException;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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


    @Mock
    CommunityRepository communityRepository;


    @Test
    void 멤버_가입_성공() {
        //given
        final User user = TestEmptyEntityGenerator.User();
        ReflectionTestUtils.setField(user, "id", 1L);

        given(userQueryService.getUser(anyLong()))
                .willReturn(user);

        final Community community = TestEmptyEntityGenerator.Community();
        ReflectionTestUtils.setField(community, "id", 2L);
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
        final User u1 = TestEmptyEntityGenerator.User();
        ReflectionTestUtils.setField(u1, "id", 1L);

        final User u2 = TestEmptyEntityGenerator.User();
        ReflectionTestUtils.setField(u2, "id", 2L);

        given(userRepository.findUsersByIds(any()))
                .willReturn(List.of(u1, u2));

        final Community community = TestEmptyEntityGenerator.Community();
        ReflectionTestUtils.setField(community, "id", 1L);

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


    @Nested
    @DisplayName("멤버 차단")
    class BanMember {

        @Test
        @DisplayName("이미 차단한 멤버인 경우")
        void alreadyBanned() {

            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "id", 1L);
            ReflectionTestUtils.setField(member, "bannedAt", LocalDateTime.now());

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
            final Member member = TestEmptyEntityGenerator.Member();
            ReflectionTestUtils.setField(member, "id", 1L);

            given(memberQueryService.getMember(anyLong()))
                    .willReturn(member);

            //then
            assertThatThrownBy(() -> {
                //when
                memberCoreService.releaseMember(member.getId());
            }).isInstanceOf(InvalidValueException.class);
        }

    }

    @Test
    @DisplayName("나를 제외한 커뮤니티에 가입된 모든 멤버들을 조회한다.")
    void testGetJoinedMembersAllWithoutMeSuccess() {
        final User user1 = TestEmptyEntityGenerator.User();
        ReflectionTestUtils.setField(user1, "id", 1L);

        final User user2 = TestEmptyEntityGenerator.User();
        ReflectionTestUtils.setField(user2, "id", 2L);

        final Community community = TestEmptyEntityGenerator.Community();
        ReflectionTestUtils.setField(community, "id", 3L);

        given(communityRepository.findCommunityById(anyLong()))
                .willReturn(Optional.of(community));

        final Member member1 = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(member1, "id", 4L);
        ReflectionTestUtils.setField(member1, "user", user1);
        ReflectionTestUtils.setField(member1, "community", community);

        final Member member2 = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(member2, "id", 5L);
        ReflectionTestUtils.setField(member2, "user", user2);
        ReflectionTestUtils.setField(member2, "community", community);

        given(memberRepository.findByUserIdAndCommunityId(anyLong(), anyLong()))
                .willReturn(Optional.of(member1));

        List<Member> members = new ArrayList<>(List.of(member1, member2));
        given(memberRepository.findJoinedMembersAllWithUserByCommunityId(anyLong()))
                .willReturn(members);

        List<Member> result = memberCoreService.getJoinedMembersAll(community.getId(), user1.getId());

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getId()).isEqualTo(member2.getId());
        assertThat(result.get(0).getCommunity()).isEqualTo(community);
        assertThat(result.get(0).getUser()).isEqualTo(user2);
    }
}