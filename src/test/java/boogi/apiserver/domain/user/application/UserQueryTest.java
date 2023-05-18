package boogi.apiserver.domain.user.application;

import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.user.repository.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.response.UserDetailInfoDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserQueryTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserQuery userQuery;

    @Test
    @DisplayName("유저 상세정보 조회")
    void userBasicInfo() {
        // given
        final User user = TestUser.builder()
                .id(1L)
                .username("김선도")
                .department("컴퓨터공학부")
                .introduce("반갑습니다 반갑습니다")
                .tagNumber("#0001")
                .build();


        given(userRepository.findUserById(anyLong())).willReturn(user);
        //when

        UserDetailInfoDto dto = userQuery.getUserDetailInfo(user.getId());

        //then
        assertThat(dto.getId()).isEqualTo(user.getId());
        assertThat(dto.getName()).isEqualTo("김선도");
        assertThat(dto.getTagNum()).isEqualTo(user.getTagNumber());
    }

}