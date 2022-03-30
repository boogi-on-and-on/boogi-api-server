package boogi.apiserver.domain.user.application;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

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