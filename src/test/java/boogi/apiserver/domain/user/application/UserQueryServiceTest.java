package boogi.apiserver.domain.user.application;

import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.UserDetailInfoResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

    @Mock
    UserValidationService userValidationService;

    @InjectMocks
    UserQueryService userQueryService;

    @Test
    void 유저_상세정보_조회() {
        // given
        User user =
                User.builder()
                        .id(1L)
                        .username("김선도")
                        .department("컴퓨터공학부")
                        .tagNumber("#0001")
                        .introduce("반갑습니다")
                        .build();

        given(userValidationService.getUser(anyLong())).willReturn(user);
        //when

        UserDetailInfoResponse dto = userQueryService.getUserDetailInfo(user.getId());

        //then
        assertThat(dto.getId()).isEqualTo(user.getId().toString());
        assertThat(dto.getUsername()).isEqualTo("김선도");
        assertThat(dto.getTagNum()).isEqualTo(user.getTagNumber());
    }

}