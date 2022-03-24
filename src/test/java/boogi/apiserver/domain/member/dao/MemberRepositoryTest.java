package boogi.apiserver.domain.member.dao;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommunityRepository communityRepository;


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
}