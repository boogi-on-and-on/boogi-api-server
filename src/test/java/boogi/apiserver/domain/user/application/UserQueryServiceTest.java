package boogi.apiserver.domain.user.application;

import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.response.UserDetailInfoResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserQueryService userQueryService;

    @Test
    @DisplayName("유저 상세정보 조회")
    void userBasicInfo() {
        // given
        User user = User.builder()
                .id(1L)
                .username("김선도")
                .department("컴퓨터공학부")
                .tagNumber("#0001")
                .introduce("반갑습니다")
                .build();


        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        //when

        UserDetailInfoResponse dto = userQueryService.getUserDetailInfo(user.getId());

        //then
        assertThat(dto.getId()).isEqualTo(user.getId());
        assertThat(dto.getName()).isEqualTo("김선도");
        assertThat(dto.getTagNum()).isEqualTo(user.getTagNumber());
    }

}