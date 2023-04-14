package boogi.apiserver.domain.member.repository;

import boogi.apiserver.builder.TestCommunity;
import boogi.apiserver.builder.TestMember;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.community.community.repository.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.member.dto.dto.BannedMemberDto;
import boogi.apiserver.domain.member.exception.MemberNotFoundException;
import boogi.apiserver.domain.user.repository.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import boogi.apiserver.utils.RepositoryTest;
import boogi.apiserver.utils.TestTimeReflection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;

class MemberRepositoryTest extends RepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Nested
    @DisplayName("ID로 멤버 조회시")
    class findMemberById {
        @DisplayName("성공")
        @Test
        void success() {
            final Member member = TestMember.builder().build();
            memberRepository.save(member);

            cleanPersistenceContext();

            final Member findMember = memberRepository.findMemberById(member.getId());
            assertThat(findMember.getId()).isEqualTo(member.getId());
        }

        @DisplayName("throw MemberNotFoundException")
        @Test
        void throwException() {
            assertThatThrownBy(() -> memberRepository.findMemberById(1L))
                    .isInstanceOf(MemberNotFoundException.class);
        }
    }

    @Test
    @DisplayName("유저 ID로 가입한 멤버들을 조회한다.")
    void findByUserId() {
        final User user = TestUser.builder().build();
        userRepository.save(user);

        List<Member> members = IntStream.range(0, 5)
                .mapToObj(i -> TestMember.builder().user(user).build())
                .collect(Collectors.toList());
        memberRepository.saveAll(members);

        cleanPersistenceContext();

        List<Member> findMembers = memberRepository.findByUserId(user.getId());

        List<Long> memberIds = members.stream()
                .map(Member::getId)
                .collect(Collectors.toList());

        assertThat(findMembers).extracting("id").isEqualTo(memberIds);
        assertThat(findMembers).extracting("user").extracting("id").containsOnly(user.getId());
    }

    @Test
    @DisplayName("유저 ID로 가입된 멤버를 community와 fetch join해서 조회한다.")
    void findMembersWithCommunity() {
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

        cleanPersistenceContext();

        List<Member> members = memberRepository.findMembersWithCommunity(user.getId());

        assertThat(members).hasSize(1);
        assertThat(members).extracting("id").containsOnly(member2.getId());
        assertThat(isLoaded(members.get(0).getCommunity())).isTrue();
    }

    @Test
    @DisplayName("해당 유저가 해당 커뮤니티에 가입된 가장 최근의 멤버를 조회한다.")
    void findByUserIdAndCommunityId() {
        User user = TestUser.builder().build();
        userRepository.save(user);

        Community community = TestCommunity.builder().build();
        communityRepository.save(community);

        List<MemberType> memberTypes = List.of(MemberType.NORMAL, MemberType.SUB_MANAGER, MemberType.MANAGER);

        List<Member> members = IntStream.range(0, 3)
                .mapToObj(i -> TestMember.builder()
                        .user(user)
                        .community(community)
                        .memberType(memberTypes.get(i))
                        .build()
                ).collect(Collectors.toList());
        members.forEach(m -> TestTimeReflection.setCreatedAt(m, LocalDateTime.now()));
        memberRepository.saveAll(members);

        cleanPersistenceContext();

        Member findMember = memberRepository.findByUserIdAndCommunityId(user.getId(), community.getId())
                .orElseGet(() -> fail());

        assertThat(findMember.getUser().getId()).isEqualTo(user.getId());
        assertThat(findMember.getCommunity().getId()).isEqualTo(community.getId());
        assertThat(findMember.getMemberType()).isEqualTo(MemberType.MANAGER);
    }

    @Test
    @DisplayName("커뮤니티에 가입된 멤버들을 페이지네이션해서 조회한다.")
    void findJoinedMembers() {
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

        cleanPersistenceContext();

        //when
        PageRequest pageable = PageRequest.of(0, 4);
        Slice<Member> memberPage = memberRepository.findJoinedMembers(pageable, community.getId());

        //then
        List<Member> members = memberPage.getContent();
        assertThat(members).hasSize(4);
        assertThat(members).extracting("id")
                .containsExactly(manager.getId(), submanager2.getId(), submanager1.getId(), normalMember.getId());
    }

    @Test
    @DisplayName("해당 커뮤니티에 가입된 모든 멤버를 user를 fetch join해서 조회한다.")
    void findAllJoinedMembersWithUser() {
        User user = TestUser.builder().build();
        userRepository.save(user);

        Community community = TestCommunity.builder().build();
        communityRepository.save(community);

        List<Member> members = IntStream.range(0, 3)
                .mapToObj(i -> TestMember.builder()
                        .community(community)
                        .user(user)
                        .build()
                ).collect(Collectors.toList());
        members.get(0).ban();
        memberRepository.saveAll(members);

        cleanPersistenceContext();

        List<Member> findMembers = memberRepository.findAllJoinedMembersWithUser(community.getId());

        assertThat(findMembers).hasSize(2);
        assertThat(isLoaded(findMembers.get(0).getUser())).isTrue();
        assertThat(findMembers).extracting("community").extracting("id")
                .containsOnly(community.getId());
        assertThat(findMembers).extracting("user").extracting("id")
                .containsOnly(user.getId());
    }

    @Test
    @DisplayName("해당 커뮤니티의 매니저를 제외한 아무 유저 1명을 조회한다.")
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

        cleanPersistenceContext();

        //when
        Member member = memberRepository.findAnyMemberExceptManager(community.getId())
                .orElseGet(() -> fail());

        //then
        assertThat(member.getId()).isEqualTo(normalUser.getId());
    }

    @Test
    @DisplayName("해당 커뮤니티에서 차단된 모든 멤버를 조회한다.")
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

        cleanPersistenceContext();

        List<BannedMemberDto> bannedMemberDtos = memberRepository.findBannedMembers(community.getId());

        assertThat(bannedMemberDtos).hasSize(1);
        BannedMemberDto first = bannedMemberDtos.get(0);
        assertThat(first.getMemberId()).isEqualTo(m1.getId());
        assertThat(first.getUser().getId()).isEqualTo(user1.getId());
    }

    @Test
    @DisplayName("유저 ID들로 해당 커뮤니티에 가입한 멤버들을 모두 조회한다.")
    void findAlreadyJoinedMember() {
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

        cleanPersistenceContext();

        List<Member> alreadyJoinedMember =
                memberRepository.findAlreadyJoinedMember(List.of(u1.getId(), u2.getId()), community.getId());

        assertThat(alreadyJoinedMember).hasSize(1);

        Member first = alreadyJoinedMember.get(0);
        assertThat(first.getUser().getId()).isEqualTo(u1.getId());
    }

    @Nested
    @DisplayName("유저가 작성한 글을 조회하기 위한 멤버 ID들을 조회할 시")
    class findMemberIdsForQueryUserPost {
        @Test
        @DisplayName("세션 유저가 가입한 모든 멤버들을 가져온다.")
        void findSessionMemberSuccess() {
            User sessionUser = TestUser.builder().build();
            userRepository.save(sessionUser);

            Community community = TestCommunity.builder().build();
            communityRepository.save(community);

            List<Member> members = IntStream.range(0, 3)
                    .mapToObj(i -> TestMember.builder()
                            .community(community)
                            .user(sessionUser)
                            .build()
                    ).collect(Collectors.toList());
            members.get(0).ban();
            memberRepository.saveAll(members);

            cleanPersistenceContext();

            List<Long> memberIds = memberRepository.findMemberIdsForQueryUserPost(sessionUser.getId());

            assertThat(memberIds).hasSize(2);
            assertThat(memberIds).containsExactly(members.get(1).getId(), members.get(2).getId());
        }

        @Test
        @DisplayName("세션 유저가 동시에 비공개 커뮤니티에 가입되지 않은 커뮤니티의 멤버는 제외하고 조회한다.")
        void findMemberSuccess() {
            User sessionUser = TestUser.builder().build();
            User user = TestUser.builder().build();
            userRepository.saveAll(List.of(sessionUser, user));

            Community publicCommunity = TestCommunity.builder().isPrivate(false).build();
            Community privateCommunity = TestCommunity.builder().isPrivate(true).build();
            communityRepository.saveAll(List.of(publicCommunity, privateCommunity));

            Member member1 = TestMember.builder()
                    .user(user)
                    .community(publicCommunity)
                    .build();
            Member member2 = TestMember.builder()
                    .user(user)
                    .community(privateCommunity)
                    .build();
            memberRepository.saveAll(List.of(member1, member2));

            cleanPersistenceContext();

            List<Long> memberIds = memberRepository.findMemberIdsForQueryUserPost(user.getId(), sessionUser.getId());

            assertThat(memberIds).hasSize(1);
            assertThat(memberIds.get(0)).isEqualTo(member1.getId());
        }
    }

    @Test
    @DisplayName("해당 커뮤니티의 매니저를 조회한다.")
    void findManager() {
        Community community = TestCommunity.builder().build();
        communityRepository.save(community);

        List<MemberType> memberTypes = List.of(MemberType.MANAGER, MemberType.SUB_MANAGER, MemberType.NORMAL);
        List<Member> members = IntStream.range(0, memberTypes.size())
                .mapToObj(i -> TestMember.builder()
                        .community(community)
                        .memberType(memberTypes.get(i))
                        .build()
                ).collect(Collectors.toList());
        memberRepository.saveAll(members);

        cleanPersistenceContext();

        Member manager = memberRepository.findManager(community.getId());

        assertThat(manager).isNotNull();
        assertThat(manager.getId()).isEqualTo(members.get(0).getId());
        assertThat(manager.getCommunity().getId()).isEqualTo(community.getId());
        assertThat(manager.getMemberType()).isEqualTo(MemberType.MANAGER);
    }

    @Test
    @DisplayName("해당 커뮤니티에서 멘션할 멤버를 유저 이름으로 검색해 페이지네이션으로 조회한다.")
    void findMentionMember() {
        //given
        final Community community = TestCommunity.builder().build();
        communityRepository.save(community);

        final User user1 = TestUser.builder()
                .username("김가나")
                .profileImageUrl("url")
                .tagNumber("#9999")
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

        cleanPersistenceContext();

        //when
        PageRequest pageable = PageRequest.of(0, 3);
        Slice<UserBasicProfileDto> userPage = memberRepository.findMentionMember(pageable, community.getId(), "김");

        //then
        List<UserBasicProfileDto> users = userPage.getContent();

        assertThat(userPage.hasNext()).isFalse();

        assertThat(users).hasSize(2);
        assertThat(users).extracting("id")
                .containsExactly(member2.getUser().getId(), member1.getUser().getId());
        assertThat(users).extracting("profileImageUrl").containsExactlyInAnyOrder("abc.xyz", "url");
        assertThat(users).extracting("tagNum").containsExactlyInAnyOrder("#1234", "#9999");
        assertThat(users).extracting("name").containsExactlyInAnyOrder("김가가", "김가나");
    }
}