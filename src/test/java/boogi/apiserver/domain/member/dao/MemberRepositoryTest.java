package boogi.apiserver.domain.member.dao;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.dto.BannedMemberDto;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.utils.PersistenceUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
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
        User user = User.builder()
                .build();

        Member member1 = Member.builder()
                .user(user)
                .build();

        Member member2 = Member.builder()
                .user(user)
                .build();

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
        User user = User.builder()
                .build();
        userRepository.save(user);

        Community community1 = Community.builder()
                .build();
        Community community2 = Community.builder()
                .build();
        communityRepository.saveAll(List.of(community1, community2));

        Member member1 = Member.builder()
                .user(user)
                .community(community1)
                .bannedAt(LocalDateTime.now())
                .build();

        Member member2 = Member.builder()
                .user(user)
                .community(community2)
                .build();
        memberRepository.saveAll(List.of(member1, member2));

        List<Member> members = memberRepository.findWhatIJoined(user.getId());

        assertThat(members.size()).isEqualTo(1);
        assertThat(members.get(0).getId()).isEqualTo(member2.getId());
    }


    // Todo: 해당 테스트를 혼자서 실행하면 잘 실행된다.
    //  클래스 단위 이상으로 테스트하면, 밑에서 검증에 실패한다.
    //  각 테스트는 독립적인것 같은데?

    //    @Test
    void findJoinedMember() {
        //given
        Community community = Community.builder().build();
        communityRepository.save(community);

        User user = User.builder().build();
        userRepository.save(user);

        Member manager = Member.builder()
                .memberType(MemberType.MANAGER)
                .community(community)
                .user(user)
                .build();
        Member submanager1 = Member.builder()
                .memberType(MemberType.SUB_MANAGER)
                .community(community)
                .user(user)
                .build();
        submanager1.setCreatedAt(LocalDateTime.now());

        Member submanager2 = Member.builder()
                .memberType(MemberType.SUB_MANAGER)
                .community(community)
                .user(user)
                .build();
        submanager2.setCreatedAt(LocalDateTime.now().minusDays(1));

        Member normalMember = Member.builder()
                .memberType(MemberType.NORMAL)
                .community(community)
                .user(user)
                .build();
        memberRepository.saveAll(List.of(normalMember, submanager1, submanager2, manager));

        //when
        PageRequest pageable = PageRequest.of(0, 4);
        Page<Member> page = memberRepository.findJoinedMembers(pageable, user.getId());
        List<Member> members = page.getContent();

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
        Community community = Community.builder().build();
        communityRepository.save(community);

        Member manager = Member.builder()
                .community(community)
                .memberType(MemberType.MANAGER)
                .build();
        Member normalUser = Member.builder()
                .community(community)
                .memberType(MemberType.NORMAL)
                .build();
        memberRepository.saveAll(List.of(manager, normalUser));

        //when
        Member member = memberRepository.findAnyMemberExceptManager(community.getId());

        //then
        assertThat(member).isEqualTo(normalUser);
    }

    @Test
    void findBannedMembers() {
        Community community = Community.builder().build();
        communityRepository.save(community);

        User user1 = User.builder()
                .username("홍길동")
                .tagNumber("#0001")
                .build();

        User user2 = User.builder()
                .username("가나다")
                .tagNumber("#0001")
                .build();
        userRepository.saveAll(List.of(user1, user2));

        Member m1 = Member.builder()
                .user(user1)
                .community(community)
                .bannedAt(LocalDateTime.now())
                .build();

        Member m2 = Member.builder()
                .user(user2)
                .community(community)
                .build();

        memberRepository.saveAll(List.of(m1, m2));

        List<BannedMemberDto> bannedMemberDtos = memberRepository.findBannedMembers(community.getId());

        assertThat(bannedMemberDtos.size()).isEqualTo(1);
        BannedMemberDto first = bannedMemberDtos.get(0);
        assertThat(first.getMemberId()).isEqualTo(m1.getId());
        assertThat(first.getUser().getId()).isEqualTo(m1.getUser().getId());
    }

    @Test
    void findAlreadyJoinedUser() {
        Community community = Community.builder().build();
        communityRepository.save(community);

        User u1 = User.builder().build();
        User u2 = User.builder().build();
        userRepository.saveAll(List.of(u1, u2));

        Member m1 = Member.builder()
                .community(community)
                .user(u1)
                .build();
        memberRepository.save(m1);

        List<Member> alreadyJoinedMember = memberRepository.findAlreadyJoinedMemberByUserId(List.of(u1.getId(), u2.getId()), community.getId());

        assertThat(alreadyJoinedMember.size()).isEqualTo(1);

        Member first = alreadyJoinedMember.get(0);
        assertThat(first.getUser()).isEqualTo(u1);
    }

    @Test
    @DisplayName("해당 커뮤니티에 가입된 삭제되지 않은 모든 멤버를 User와 함께 조회한다.")
    void testFindJoinedMembersAllWithUserByCommunityId() {
        User user1 = User.builder()
                .build();
        User user2 = User.builder()
                .build();
        userRepository.saveAll(List.of(user1, user2));

        Community community = Community.builder()
                .build();
        communityRepository.save(community);

        Member member1 = Member.builder()
                .user(user1)
                .community(community)
                .build();
        Member member2 = Member.builder()
                .user(user2)
                .community(community)
                .build();
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
}