package boogi.apiserver.domain.user.application;

import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.UserDetailInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserValidationService userValidationService;

    public UserDetailInfoResponse getUserDetailInfo(Long userId) {
        User user = userValidationService.getUser(userId);
        return UserDetailInfoResponse.of(user);
    }
}
