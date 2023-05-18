package boogi.apiserver.domain.user.application;

import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.response.UserDetailInfoDto;
import boogi.apiserver.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static boogi.apiserver.utils.fixture.UserFixture.YONGJIN;
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
        final User user = YONGJIN.toUser(1L);

        given(userRepository.findUserById(anyLong())).willReturn(user);
        //when

        UserDetailInfoDto dto = userQuery.getUserDetailInfo(user.getId());

        //then
        assertThat(dto.getId()).isEqualTo(user.getId());
        assertThat(dto.getProfileImageUrl()).isEqualTo(YONGJIN.profileImage);
        assertThat(dto.getName()).isEqualTo(YONGJIN.username);
        assertThat(dto.getTagNum()).isEqualTo(YONGJIN.tagNumber);
        assertThat(dto.getIntroduce()).isEqualTo(YONGJIN.introduce);
        assertThat(dto.getDepartment()).isEqualTo(YONGJIN.department);
    }

}