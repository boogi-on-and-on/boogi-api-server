package boogi.apiserver.domain.community.joinrequest.application;

import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.community.community.dto.dto.UserJoinRequestInfoDto;
import boogi.apiserver.domain.community.joinrequest.dao.JoinRequestRepository;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        final User user = TestUser.builder()
                .id(1L)
                .username("이름")
                .tagNumber("#0001")
                .profileImageUrl("image")
                .build();

        final JoinRequest joinRequest = JoinRequest.builder()
                .id(2L)
                .user(user)
                .build();

        given(joinRequestRepository.getAllRequests(anyLong()))
                .willReturn(List.of(joinRequest));

//        List<UserJoinRequestInfoDto> allRequests = joinRequestQueryService.getAllRequests(anyLong());

//        UserJoinRequestInfoDto req = allRequests.get(0);
//        assertThat(req.getId()).isEqualTo(2L);
//
//        UserBasicProfileDto userDto = req.getUser();
//        assertThat(userDto.getName()).isEqualTo("이름");
//        assertThat(userDto.getId()).isEqualTo(1L);
//        assertThat(userDto.getProfileImageUrl()).isEqualTo("image");
//        assertThat(userDto.getTagNum()).isEqualTo("#0001");
    }
}