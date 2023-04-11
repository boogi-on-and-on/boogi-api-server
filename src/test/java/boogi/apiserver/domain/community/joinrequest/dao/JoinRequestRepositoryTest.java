package boogi.apiserver.domain.community.joinrequest.dao;

import boogi.apiserver.builder.TestCommunity;
import boogi.apiserver.builder.TestJoinRequest;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequestStatus;
import boogi.apiserver.domain.community.joinrequest.exception.JoinRequestNotFoundException;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.utils.RepositoryTest;
import boogi.apiserver.utils.TestTimeReflection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JoinRequestRepositoryTest extends RepositoryTest {

    @Autowired
    private JoinRequestRepository joinRequestRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("findJoinRequestById 디폴트 메서드 테스트")
    class findJoinRequestById {

        @DisplayName("성공")
        @Test
        void success() {
            final JoinRequest joinRequest = TestJoinRequest.builder().build();
            joinRequestRepository.save(joinRequest);

            cleanPersistenceContext();

            final JoinRequest findJoinRequest = joinRequestRepository.findJoinRequestById(joinRequest.getId());
            assertThat(findJoinRequest.getId()).isEqualTo(joinRequest.getId());
        }

        @DisplayName("throw JoinRequestNotFoundException")
        @Test
        void throwException() {
            assertThatThrownBy(() -> {
                joinRequestRepository.findJoinRequestById(1L);
            }).isInstanceOf(JoinRequestNotFoundException.class);
        }
    }

    @Test
    @DisplayName("해당 커뮤니티에서 대기중인 모든 요청 찾기")
    void getAllPendingRequests() {
        //given
        final Community community = TestCommunity.builder().build();
        communityRepository.save(community);

        final User user1 = TestUser.builder().build();
        final User user2 = TestUser.builder().build();

        userRepository.saveAll(List.of(user1, user2));

        final JoinRequest r1 = TestJoinRequest.builder()
                .community(community)
                .status(JoinRequestStatus.PENDING)
                .user(user1)
                .build();

        final JoinRequest r2 = TestJoinRequest.builder()
                .community(community)
                .status(JoinRequestStatus.CONFIRM)
                .user(user2)
                .build();

        joinRequestRepository.saveAll(List.of(r1, r2));

        cleanPersistenceContext();

        //when
        List<JoinRequest> requests = joinRequestRepository.getAllPendingRequests(community.getId());

        //then
        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getId()).isEqualTo(r1.getId());
        assertThat(requests.get(0).getStatus()).isEqualTo(JoinRequestStatus.PENDING);
        assertThat(requests.get(0).getConfirmedMember()).isNull();
        assertThat(requests.get(0).getAcceptor()).isNull();
    }

    @Test
    @DisplayName("해당 유저가 해당 커뮤니티에 한 가장 최근 가입 요청 1개를 조회한다.")
    void getLatestJoinRequest() {
        User user = TestUser.builder().build();
        userRepository.save(user);

        Community community = TestCommunity.builder().build();
        communityRepository.save(community);

        JoinRequest joinRequest1 = TestJoinRequest.builder().user(user).community(community).build();
        TestTimeReflection.setCreatedAt(joinRequest1, LocalDateTime.now());
        JoinRequest joinRequest2 = TestJoinRequest.builder().user(user).community(community).build();
        TestTimeReflection.setCreatedAt(joinRequest2, LocalDateTime.now());
        joinRequestRepository.saveAll(List.of(joinRequest1, joinRequest2));

        cleanPersistenceContext();

        Optional<JoinRequest> latestJoinRequest =
                joinRequestRepository.getLatestJoinRequest(user.getId(), community.getId());

        assertThat(latestJoinRequest.isPresent()).isTrue();
        assertThat(latestJoinRequest.get().getId()).isEqualTo(joinRequest2.getId());
    }

    @Test
    @DisplayName("가입 요청 ID들로 가입 요청을 조회한다.")
    void getRequestsByIds() {
        List<JoinRequest> joinRequests = IntStream.range(0, 10)
                .mapToObj(i -> TestJoinRequest.builder().build())
                .collect(Collectors.toList());
        joinRequestRepository.saveAll(joinRequests);

        cleanPersistenceContext();

        List<Long> joinRequestIds = joinRequests.stream()
                .map(JoinRequest::getId)
                .collect(Collectors.toList());

        List<JoinRequest> findJoinRequests = joinRequestRepository.getRequestsByIds(joinRequestIds);

        assertThat(findJoinRequests).hasSize(10);
    }
}