package boogi.apiserver.domain.community.joinrequest.dao;

import boogi.apiserver.annotations.CustomDataJpaTest;
import boogi.apiserver.builder.TestCommunity;
import boogi.apiserver.builder.TestJoinRequest;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.alarm.alarm.dao.AlarmRepository;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequestStatus;
import boogi.apiserver.domain.community.joinrequest.exception.JoinRequestNotFoundException;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.utils.PersistenceUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CustomDataJpaTest
class JoinRequestRepositoryTest {

    @Autowired
    private JoinRequestRepository joinRequestRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    EntityManager em;

    PersistenceUtil persistenceUtil;
    @Autowired
    private AlarmRepository alarmRepository;

    @BeforeEach
    void init() {
        persistenceUtil = new PersistenceUtil(em);
    }

    @Test
    @DisplayName("전체 요청 찾기")
    void getAllRequests() {
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

        //when
        List<JoinRequest> requests = joinRequestRepository.getAllRequests(community.getId());

        //then
        assertThat(requests.size()).isEqualTo(1);
        assertThat(requests.get(0)).isEqualTo(r1);
    }

    @Nested
    @DisplayName("findByJoinRequestId 디폴트 메서드 테스트")
    class findByJoinRequestId {

        @DisplayName("성공")
        @Test
        void success() {
            final JoinRequest joinRequest = TestJoinRequest.builder().build();
            joinRequestRepository.save(joinRequest);

            persistenceUtil.cleanPersistenceContext();

            final JoinRequest findJoinRequest = joinRequestRepository.findByJoinRequestId(joinRequest.getId());
            assertThat(findJoinRequest.getId()).isEqualTo(joinRequest.getId());
        }

        @DisplayName("throw JoinRequestNotFoundException")
        @Test
        void throwException() {
            assertThatThrownBy(() -> {
                joinRequestRepository.findByJoinRequestId(1L);
            }).isInstanceOf(JoinRequestNotFoundException.class);
        }

    }
}