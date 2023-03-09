package boogi.apiserver.domain.member.dao;

import boogi.apiserver.annotations.CustomDataJpaTest;
import boogi.apiserver.builder.TestCommunity;
import boogi.apiserver.builder.TestMember;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.dto.dto.BannedMemberDto;
import boogi.apiserver.domain.member.exception.MemberNotFoundException;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import boogi.apiserver.utils.PersistenceUtil;
import boogi.apiserver.utils.TestTimeReflection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;

@CustomDataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private EntityManager em;

    private PersistenceUtil persistenceUtil;

    @BeforeEach
    void init() {
        persistenceUtil = new PersistenceUtil(em);
    }

    @Test
    void getMemberIdsByUserId() {
        final User user = TestUser.builder().build();

        final Member member1 = TestMember.builder()
                .user(user)
                .build();

        final Member member2 = TestMember.builder()
                .user(user)
                .build();

        userRepository.save(user);
        memberRepository.saveAll(List.of(member1, member2));

        persistenceUtil.cleanPersistenceContext();

        List<Long> memberIds = memberRepository.findByUserId(user.getId())
                .stream()
                .map(Member::getId)
                .collect(Collectors.toList());

        assertThat(memberIds).containsOnlyOnce(member1.getId(), member2.getId());
    }

    @Test
    void findMembersWhatIJoined() {
        final User user = TestUser.builder().build();
        userRepository.save(user);

        final Community community1 = TestCommunity.builder().build();
        final Community community2 = TestCommunity.builder().build();

        communityRepository.saveAll(List.of(community1, community2));

        final Member member1 = TestMember.builder()
                .user(user)
                .community(community1)
                .bannedAt(LocalDateTime.now())
                .build();

        final Member member2 = TestMember.builder()
                .user(user)
                .community(community2)
                .build();

        memberRepository.saveAll(List.of(member1, member2));

        persistenceUtil.cleanPersistenceContext();

        List<Member> members = memberRepository.findWhatIJoined(user.getId());

        assertThat(members.size()).isEqualTo(1);
        assertThat(members.get(0).getId()).isEqualTo(member2.getId());
    }

    @Test
    void findJoinedMember() {
        //given
        final Community community = TestCommunity.builder().build();
        communityRepository.save(community);

        final User user = TestUser.builder().build();
        userRepository.save(user);

        final Member manager = TestMember.builder()
                .user(user)
                .community(community)
                .memberType(MemberType.MANAGER)
                .build();

        final Member submanager1 = TestMember.builder()
                .user(user)
                .community(community)
                .memberType(MemberType.SUB_MANAGER)
                .build();
        TestTimeReflection.setCreatedAt(submanager1, LocalDateTime.now());

        final Member submanager2 = TestMember.builder()
                .user(user)
                .community(community)
                .memberType(MemberType.SUB_MANAGER)
                .build();
        TestTimeReflection.setCreatedAt(submanager2, LocalDateTime.now().minusDays(1));

        final Member normalMember = TestMember.builder()
                .user(user)
                .community(community)
                .memberType(MemberType.NORMAL)
                .build();

        memberRepository.saveAll(List.of(normalMember, submanager1, submanager2, manager));

        persistenceUtil.cleanPersistenceContext();

        //when
        PageRequest pageable = PageRequest.of(0, 4);
        Slice<Member> slice = memberRepository.findJoinedMembers(pageable, community.getId());
        List<Member> members = slice.getContent();

        //then
        assertThat(members.size()).isEqualTo(4);
        assertThat(members.get(0).getId()).isEqualTo(manager.getId());
        assertThat(members.get(1).getId()).isEqualTo(submanager2.getId());
        assertThat(members.get(2).getId()).isEqualTo(submanager1.getId());
        assertThat(members.get(3).getId()).isEqualTo(normalMember.getId());
    }

    @Test
    void findAnyMemberExceptManager() {
        //given
        final Community community = TestCommunity.builder().build();
        communityRepository.save(community);

        final Member manager = TestMember.builder()
                .community(community)
                .memberType(MemberType.MANAGER)
                .build();

        final Member normalUser = TestMember.builder()
                .community(community)
                .memberType(MemberType.NORMAL)
                .build();

        memberRepository.saveAll(List.of(manager, normalUser));

        persistenceUtil.cleanPersistenceContext();

        //when
        Member member = memberRepository.findAnyMemberExceptManager(community.getId()).orElse(null);
        if (member == null) {
            fail();
        }

        //then
        assertThat(member.getId()).isEqualTo(normalUser.getId());
    }

    @Test
    void findBannedMembers() {
        final Community community = TestCommunity.builder().build();
        communityRepository.save(community);

        final User user1 = TestUser.builder()
                .username("홍길동")
                .tagNumber("#0001")
                .build();

        final User user2 = TestUser.builder()
                .username("가나다")
                .tagNumber("#0001")
                .build();
        userRepository.saveAll(List.of(user1, user2));

        final Member m1 = TestMember.builder()
                .user(user1)
                .community(community)
                .bannedAt(LocalDateTime.now())
                .build();

        final Member m2 = TestMember.builder()
                .user(user2)
                .community(community)
                .build();

        memberRepository.saveAll(List.of(m1, m2));

        persistenceUtil.cleanPersistenceContext();

        List<BannedMemberDto> bannedMemberDtos = memberRepository.findBannedMembers(community.getId());

        assertThat(bannedMemberDtos.size()).isEqualTo(1);
        BannedMemberDto first = bannedMemberDtos.get(0);
        assertThat(first.getMemberId()).isEqualTo(m1.getId());
        assertThat(first.getUser().getId()).isEqualTo(m1.getUser().getId());
    }

    @Test
    void findAlreadyJoinedUser() {
        final Community community = TestCommunity.builder().build();
        communityRepository.save(community);

        final User u1 = TestUser.builder().build();
        final User u2 = TestUser.builder().build();

        userRepository.saveAll(List.of(u1, u2));

        final Member m1 = TestMember.builder()
                .user(u1)
                .community(community)
                .build();
        memberRepository.save(m1);

        persistenceUtil.cleanPersistenceContext();

        List<Member> alreadyJoinedMember = memberRepository.findAlreadyJoinedMemberByUserId(List.of(u1.getId(), u2.getId()), community.getId());

        assertThat(alreadyJoinedMember.size()).isEqualTo(1);

        Member first = alreadyJoinedMember.get(0);
        assertThat(first.getUser().getId()).isEqualTo(u1.getId());
    }

    @Test
    @DisplayName("해당 커뮤니티에 가입된 삭제되지 않은 모든 멤버를 User와 함께 조회한다.")
    void testFindJoinedMembersAllWithUserByCommunityId() {
        final User user1 = TestUser.builder().build();
        final User user2 = TestUser.builder().build();
        userRepository.saveAll(List.of(user1, user2));

        final Community community = TestCommunity.builder().build();
        communityRepository.save(community);

        final Member member1 = TestMember.builder()
                .user(user1)
                .community(community)
                .build();

        final Member member2 = TestMember.builder()
                .user(user2)
                .community(community)
                .build();

        member2.ban();
        memberRepository.saveAll(List.of(member1, member2));

        persistenceUtil.cleanPersistenceContext();

        List<Member> joinedMembers = memberRepository.findJoinedMembersAllWithUser(community.getId());

        assertThat(joinedMembers.size()).isEqualTo(1);

        assertThat(joinedMembers.get(0).getId()).isEqualTo(member1.getId());
        assertThat(persistenceUtil.isLoaded(joinedMembers.get(0).getUser())).isTrue();
        assertThat(joinedMembers.get(0).getUser().getId()).isEqualTo(member1.getUser().getId());
        assertThat(joinedMembers.get(0).getCommunity().getId()).isEqualTo(community.getId());
    }

    @Test
    void findMentionMember() {
        //given
        final Community community = TestCommunity.builder().build();
        communityRepository.save(community);

        final User user1 = TestUser.builder()
                .username("김가나")
                .build();

        final User user2 = TestUser.builder()
                .username("김가가")
                .profileImageUrl("abc.xyz")
                .tagNumber("#1234")
                .build();

        final User user3 = TestUser.builder()
                .username("박가나")
                .build();

        userRepository.saveAll(List.of(user1, user2, user3));

        final Member member1 = TestMember.builder()
                .user(user1)
                .community(community)
                .build();

        final Member member2 = TestMember.builder()
                .user(user2)
                .community(community)
                .build();

        final Member member3 = TestMember.builder()
                .user(user3)
                .community(community)
                .build();

        memberRepository.saveAll(List.of(member1, member2, member3));

        persistenceUtil.cleanPersistenceContext();

        //when
        PageRequest pageable = PageRequest.of(0, 3);
        Slice<UserBasicProfileDto> slice = memberRepository.findMentionMember(pageable, community.getId(), "김");

        //then
        List<UserBasicProfileDto> members = slice.getContent();
        assertThat(members.stream().map(UserBasicProfileDto::getId)).containsExactly(member2.getUser().getId(), member1.getUser().getId());
        assertThat(slice.hasNext()).isFalse();

        UserBasicProfileDto first = members.get(0);
        assertThat(first.getId()).isEqualTo(member2.getUser().getId());
        assertThat(first.getProfileImageUrl()).isEqualTo("abc.xyz");
        assertThat(first.getTagNum()).isEqualTo("#1234");
        assertThat(first.getName()).isEqualTo("김가가");
    }

    @Nested
    @DisplayName("findByMemberId 디폴트 메서드 테스트")
    class findByMemberId {
        @DisplayName("성공")
        @Test
        void success() {
            final Member member = TestMember.builder().build();
            memberRepository.save(member);

            persistenceUtil.cleanPersistenceContext();

            final Member findMember = memberRepository.findByMemberId(member.getId());
            assertThat(findMember.getId()).isEqualTo(member.getId());
        }

        @DisplayName("throw MemberNotFoundException")
        @Test
        void throwException() {
            assertThatThrownBy(() -> {
                memberRepository.findByMemberId(1L);
            }).isInstanceOf(MemberNotFoundException.class);
        }
    }
}