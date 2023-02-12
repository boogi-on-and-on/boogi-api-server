package boogi.apiserver.domain.community.joinrequest.application;

import boogi.apiserver.domain.community.joinrequest.dao.JoinRequestRepository;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequestStatus;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.response.UserBasicProfileDto;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class JoinRequestQueryServiceTest {

    @Mock
    JoinRequestRepository joinRequestRepository;

    @InjectMocks
    JoinRequestQueryService joinRequestQueryService;


    @Test
    @DisplayName("커뮤니티 가입요청 목록 조회")
    void communityJoinRequestList() {
        User user = User.builder()
                .id(1L)
                .profileImageUrl("image")
                .tagNumber("#0001")
                .username("이름")
                .build();

        final JoinRequest joinRequest = TestEmptyEntityGenerator.JoinRequest();
        ReflectionTestUtils.setField(joinRequest, "id", 2L);
        ReflectionTestUtils.setField(joinRequest, "user", user);

        given(joinRequestRepository.getAllRequests(anyLong()))
                .willReturn(List.of(joinRequest));

        List<Map<String, Object>> allRequests = joinRequestQueryService.getAllRequests(anyLong());

        Map<String, Object> req = allRequests.get(0);
        assertThat(req.get("id")).isEqualTo(2L);

        UserBasicProfileDto userDto = (UserBasicProfileDto) req.get("user");
        assertThat(userDto.getName()).isEqualTo("이름");
        assertThat(userDto.getId()).isEqualTo(1L);
        assertThat(userDto.getProfileImageUrl()).isEqualTo("image");
        assertThat(userDto.getTagNum()).isEqualTo("#0001");
    }
}