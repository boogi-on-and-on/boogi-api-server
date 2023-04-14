package boogi.apiserver.domain.community.joinrequest.application;

import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.community.community.repository.CommunityRepository;
import boogi.apiserver.domain.community.community.dto.dto.UserJoinRequestInfoDto;
import boogi.apiserver.domain.community.joinrequest.repository.JoinRequestRepository;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;
import boogi.apiserver.domain.member.application.MemberQueryService;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class JoinRequestQueryServiceTest {
    @InjectMocks
    JoinRequestQueryService joinRequestQueryService;

    @Mock
    JoinRequestRepository joinRequestRepository;

    @Mock
    MemberQueryService memberQueryService;

    @Mock
    CommunityRepository communityRepository;

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

        given(joinRequestRepository.getAllPendingRequests(anyLong()))
                .willReturn(List.of(joinRequest));

        List<UserJoinRequestInfoDto> allRequests = joinRequestQueryService.getAllPendingRequests(user.getId(), 3L);

        verify(communityRepository, times(1)).findCommunityById(anyLong());
        verify(memberQueryService, times(1)).getOperator(anyLong(), anyLong());

        UserJoinRequestInfoDto request = allRequests.get(0);
        assertThat(request.getId()).isEqualTo(2L);

        UserBasicProfileDto userDto = request.getUser();
        assertThat(userDto.getName()).isEqualTo("이름");
        assertThat(userDto.getId()).isEqualTo(1L);
        assertThat(userDto.getProfileImageUrl()).isEqualTo("image");
        assertThat(userDto.getTagNum()).isEqualTo("#0001");
    }
}