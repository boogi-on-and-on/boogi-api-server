package boogi.apiserver.domain.member.dao;

import boogi.apiserver.annotations.CustomDataJpaTest;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.dto.response.BannedMemberDto;
import boogi.apiserver.domain.member.exception.MemberNotFoundException;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.response.UserBasicProfileDto;
import boogi.apiserver.utils.PersistenceUtil;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;

@CustomDataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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

    @BeforeAll
    void init() {
        persistenceUtil = new PersistenceUtil(em);
    }

    @Test
    void getMemberIdsByUserId() {
        final User user = TestEmptyEntityGenerator.User();

        final Member member1 = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(member1, "user", user);

        final Member member2 = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(member2, "user", user);

        userRepository.save(user);
        memberRepository.saveAll(List.of(member1, member2));

        List<Long> memberIds = memberRepository.findByUserId(user.getId())
                .stream()
                .map(Member::getId)
                .collect(Collectors.toList());

        assertThat(memberIds).containsOnlyOnce(member1.getId(), member2.getId());
    }

    @Test
    void findMembersWhatIJoined() {
        final User user = TestEmptyEntityGenerator.User();
        userRepository.save(user);

        final Community community1 = TestEmptyEntityGenerator.Community();
        final Community community2 = TestEmptyEntityGenerator.Community();

        communityRepository.saveAll(List.of(community1, community2));

        final Member member1 = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(member1, "user", user);
        ReflectionTestUtils.setField(member1, "community", community1);
        ReflectionTestUtils.setField(member1, "bannedAt", LocalDateTime.now());


        final Member member2 = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(member2, "user", user);
        ReflectionTestUtils.setField(member2, "community", community2);
        memberRepository.saveAll(List.of(member1, member2));

        List<Member> members = memberRepository.findWhatIJoined(user.getId());

        assertThat(members.size()).isEqualTo(1);
        assertThat(members.get(0).getId()).isEqualTo(member2.getId());
    }

    @Test
    void findJoinedMember() {
        //given
        final Community community = TestEmptyEntityGenerator.Community();
        communityRepository.save(community);

        final User user = TestEmptyEntityGenerator.User();
        userRepository.save(user);

        final Member manager = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(manager, "user", user);
        ReflectionTestUtils.setField(manager, "community", community);
        ReflectionTestUtils.setField(manager, "memberType", MemberType.MANAGER);

        final Member submanager1 = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(submanager1, "user", user);
        ReflectionTestUtils.setField(submanager1, "community", community);
        ReflectionTestUtils.setField(submanager1, "memberType", MemberType.SUB_MANAGER);
        ReflectionTestUtils.setField(submanager1, "createdAt", LocalDateTime.now());

        final Member submanager2 = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(submanager2, "user", user);
        ReflectionTestUtils.setField(submanager2, "community", community);
        ReflectionTestUtils.setField(submanager2, "memberType", MemberType.SUB_MANAGER);
        ReflectionTestUtils.setField(submanager2, "createdAt", LocalDateTime.now().minusDays(1));

        final Member normalMember = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(normalMember, "user", user);
        ReflectionTestUtils.setField(normalMember, "community", community);
        ReflectionTestUtils.setField(normalMember, "memberType", MemberType.NORMAL);

        memberRepository.saveAll(List.of(normalMember, submanager1, submanager2, manager));

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
        final Community community = TestEmptyEntityGenerator.Community();
        communityRepository.save(community);

        final Member manager = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(manager, "community", community);
        ReflectionTestUtils.setField(manager, "memberType", MemberType.MANAGER);

        final Member normalUser = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(normalUser, "community", community);
        ReflectionTestUtils.setField(normalUser, "memberType", MemberType.NORMAL);

        memberRepository.saveAll(List.of(manager, normalUser));

        //when
        Member member = memberRepository.findAnyMemberExceptManager(community.getId()).orElse(null);
        if (member == null) {
            fail();
        }

        //then
        assertThat(member).isEqualTo(normalUser);
    }

    @Test
    void findBannedMembers() {
        final Community community = TestEmptyEntityGenerator.Community();
        communityRepository.save(community);

        final User user1 = TestEmptyEntityGenerator.User();
        ReflectionTestUtils.setField(user1, "username", "홍길동");
        ReflectionTestUtils.setField(user1, "tagNumber", "#0001");

        final User user2 = TestEmptyEntityGenerator.User();
        ReflectionTestUtils.setField(user2, "username", "가나다");
        ReflectionTestUtils.setField(user2, "tagNumber", "#0001");

        userRepository.saveAll(List.of(user1, user2));

        final Member m1 = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(m1, "user", user1);
        ReflectionTestUtils.setField(m1, "community", community);
        ReflectionTestUtils.setField(m1, "bannedAt", LocalDateTime.now());

        final Member m2 = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(m2, "user", user2);
        ReflectionTestUtils.setField(m2, "community", community);

        memberRepository.saveAll(List.of(m1, m2));

        List<BannedMemberDto> bannedMemberDtos = memberRepository.findBannedMembers(community.getId());

        assertThat(bannedMemberDtos.size()).isEqualTo(1);
        BannedMemberDto first = bannedMemberDtos.get(0);
        assertThat(first.getMemberId()).isEqualTo(m1.getId());
        assertThat(first.getUser().getId()).isEqualTo(m1.getUser().getId());
    }

    @Test
    void findAlreadyJoinedUser() {
        final Community community = TestEmptyEntityGenerator.Community();
        communityRepository.save(community);

        final User u1 = TestEmptyEntityGenerator.User();
        final User u2 = TestEmptyEntityGenerator.User();

        userRepository.saveAll(List.of(u1, u2));

        final Member m1 = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(m1, "user", u1);
        ReflectionTestUtils.setField(m1, "community", community);
        memberRepository.save(m1);

        List<Member> alreadyJoinedMember = memberRepository.findAlreadyJoinedMemberByUserId(List.of(u1.getId(), u2.getId()), community.getId());

        assertThat(alreadyJoinedMember.size()).isEqualTo(1);

        Member first = alreadyJoinedMember.get(0);
        assertThat(first.getUser()).isEqualTo(u1);
    }

    @Test
    @DisplayName("해당 커뮤니티에 가입된 삭제되지 않은 모든 멤버를 User와 함께 조회한다.")
    void testFindJoinedMembersAllWithUserByCommunityId() {
        final User user1 = TestEmptyEntityGenerator.User();
        final User user2 = TestEmptyEntityGenerator.User();
        userRepository.saveAll(List.of(user1, user2));

        final Community community = TestEmptyEntityGenerator.Community();
        communityRepository.save(community);

        final Member member1 = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(member1, "user", user1);
        ReflectionTestUtils.setField(member1, "community", community);

        final Member member2 = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(member2, "user", user2);
        ReflectionTestUtils.setField(member2, "community", community);

        member2.ban();
        memberRepository.saveAll(List.of(member1, member2));

        persistenceUtil.cleanPersistenceContext();

        List<Member> joinedMembers = memberRepository.findJoinedMembersAllWithUserByCommunityId(community.getId());

        assertThat(joinedMembers.size()).isEqualTo(1);

        assertThat(joinedMembers.get(0).getId()).isEqualTo(member1.getId());
        assertThat(persistenceUtil.isLoaded(joinedMembers.get(0).getUser())).isTrue();
        assertThat(joinedMembers.get(0).getUser().getId()).isEqualTo(member1.getUser().getId());
        assertThat(joinedMembers.get(0).getCommunity().getId()).isEqualTo(community.getId());
    }

    @Test
    void findMentionMember() {
        //given
        final Community community = TestEmptyEntityGenerator.Community();
        communityRepository.save(community);

        final User user1 = TestEmptyEntityGenerator.User();
        ReflectionTestUtils.setField(user1, "username", "김가나");

        final User user2 = TestEmptyEntityGenerator.User();
        ReflectionTestUtils.setField(user2, "username", "김가가");
        ReflectionTestUtils.setField(user2, "profileImageUrl", "abc/xyz");
        ReflectionTestUtils.setField(user2, "tagNumber", "1234");

        final User user3 = TestEmptyEntityGenerator.User();
        ReflectionTestUtils.setField(user3, "username", "박가나");


        userRepository.saveAll(List.of(user1, user2, user3));

        final Member member1 = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(member1, "user", user1);
        ReflectionTestUtils.setField(member1, "community", community);

        final Member member2 = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(member2, "user", user2);
        ReflectionTestUtils.setField(member2, "community", community);

        final Member member3 = TestEmptyEntityGenerator.Member();
        ReflectionTestUtils.setField(member3, "user", user3);
        ReflectionTestUtils.setField(member3, "community", community);

        memberRepository.saveAll(List.of(member1, member2, member3));

        //when
        PageRequest pageable = PageRequest.of(0, 3);
        Slice<UserBasicProfileDto> slice = memberRepository.findMentionMember(pageable, community.getId(), "김");

        //then
        List<UserBasicProfileDto> members = slice.getContent();
        assertThat(members.stream().map(UserBasicProfileDto::getId)).containsExactly(member2.getUser().getId(), member1.getUser().getId());
        assertThat(slice.hasNext()).isFalse();

        UserBasicProfileDto first = members.get(0);
        assertThat(first.getId()).isEqualTo(member2.getUser().getId());
        assertThat(first.getProfileImageUrl()).isEqualTo("abc/xyz");
        assertThat(first.getTagNum()).isEqualTo("1234");
        assertThat(first.getName()).isEqualTo("김가가");
    }

    @Nested
    @DisplayName("findByMemberId 디폴트 메서드 테스트")
    class findByMemberId {
        @DisplayName("성공")
        @Test
        void success() {
            final Member member = TestEmptyEntityGenerator.Member();
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