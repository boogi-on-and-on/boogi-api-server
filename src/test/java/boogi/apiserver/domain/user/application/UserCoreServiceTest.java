package boogi.apiserver.domain.user.application;

import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.exception.WithdrawnOrCanceledUserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserCoreServiceTest {

//    @InjectMocks
//    UserCoreService userCoreService;
//
//    @Mock
//    UserQueryService userQueryService;
//
//
//    @Test
//    void 유저_상세정보_삭제_탈퇴() throws Exception {
//        //given
//        User user = User.builder().build();
//        user.setCanceledAt(LocalDateTime.now());
//        given(userQueryService.getUserDetailInfo(anyLong())).willReturn(Optional.of(user));
//
//        //then
//        assertThatThrownBy(() -> {
//            //when
//            userQueryService.getUserDetailInfo(anyLong());
//        }).isInstanceOf(WithdrawnOrCanceledUserException.class);
//    }
}