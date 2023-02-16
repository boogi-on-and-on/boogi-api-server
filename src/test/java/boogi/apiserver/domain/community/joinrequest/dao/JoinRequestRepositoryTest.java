package boogi.apiserver.domain.community.joinrequest.dao;

import boogi.apiserver.annotations.CustomDataJpaTest;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequestStatus;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CustomDataJpaTest
class JoinRequestRepositoryTest {

    @Autowired
    private JoinRequestRepository joinRequestRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("전체 요청 찾기")
    void getAllRequests() {
        //given
        final Community community = TestEmptyEntityGenerator.Community();
        communityRepository.save(community);

        final User user1 = TestEmptyEntityGenerator.User();
        final User user2 = TestEmptyEntityGenerator.User();

        userRepository.saveAll(List.of(user1, user2));

        final JoinRequest r1 = TestEmptyEntityGenerator.JoinRequest();
        ReflectionTestUtils.setField(r1, "community", community);
        ReflectionTestUtils.setField(r1, "status", JoinRequestStatus.PENDING);
        ReflectionTestUtils.setField(r1, "user", user1);

        final JoinRequest r2 = TestEmptyEntityGenerator.JoinRequest();
        ReflectionTestUtils.setField(r2, "community", community);
        ReflectionTestUtils.setField(r2, "status", JoinRequestStatus.CONFIRM);
        ReflectionTestUtils.setField(r2, "user", user2);

        joinRequestRepository.saveAll(List.of(r1, r2));

        //when
        List<JoinRequest> requests = joinRequestRepository.getAllRequests(community.getId());

        //then
        assertThat(requests.size()).isEqualTo(1);
        assertThat(requests.get(0)).isEqualTo(r1);
    }
}