package boogi.apiserver.domain.community.joinrequest.application;

import boogi.apiserver.domain.community.community.dto.dto.UserJoinRequestInfoDto;
import boogi.apiserver.domain.community.joinrequest.dao.JoinRequestRepository;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class JoinRequestQueryServiceTest {

    @Mock
    JoinRequestRepository joinRequestRepository;

    @InjectMocks
    JoinRequestQueryService joinRequestQueryService;


    @Test
    @DisplayName("커뮤니티 가입요청 목록 조회")
    void communityJoinRequestList() {
        final User user = TestEmptyEntityGenerator.User();
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "username", "이름");
        ReflectionTestUtils.setField(user, "tagNumber", "#0001");
        ReflectionTestUtils.setField(user, "profileImageUrl", "image");

        final JoinRequest joinRequest = TestEmptyEntityGenerator.JoinRequest();
        ReflectionTestUtils.setField(joinRequest, "id", 2L);
        ReflectionTestUtils.setField(joinRequest, "user", user);

        given(joinRequestRepository.getAllRequests(anyLong()))
                .willReturn(List.of(joinRequest));

        List<UserJoinRequestInfoDto> allRequests = joinRequestQueryService.getAllRequests(anyLong());

        UserJoinRequestInfoDto req = allRequests.get(0);
        assertThat(req.getId()).isEqualTo(2L);

        UserBasicProfileDto userDto = req.getUser();
        assertThat(userDto.getName()).isEqualTo("이름");
        assertThat(userDto.getId()).isEqualTo(1L);
        assertThat(userDto.getProfileImageUrl()).isEqualTo("image");
        assertThat(userDto.getTagNum()).isEqualTo("#0001");
    }
}