package boogi.apiserver.domain.user.application;

import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.global.error.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;

    @Test
    void 유저_상세정보_없는경우() throws Exception {
        //given
        given(userRepository.findById(anyLong())).willThrow(EntityNotFoundException.class);

        //then
        assertThatThrownBy(() -> {
            //when
            userService.getUserInfo(anyLong());
        }).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void 유저_상세정보() throws Exception {

    }
}