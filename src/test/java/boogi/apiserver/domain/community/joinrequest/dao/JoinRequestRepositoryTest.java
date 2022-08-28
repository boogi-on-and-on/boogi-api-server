package boogi.apiserver.domain.community.joinrequest.dao;

import boogi.apiserver.annotations.CustomDataJpaTest;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequestStatus;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
        Community community = Community.builder().build();
        communityRepository.save(community);

        User user1 = User.builder().build();
        User user2 = User.builder().build();
        userRepository.saveAll(List.of(user1, user2));

        JoinRequest r1 = JoinRequest.builder()
                .community(community)
                .status(JoinRequestStatus.PENDING)
                .user(user1)
                .build();
        JoinRequest r2 = JoinRequest.builder()
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
}