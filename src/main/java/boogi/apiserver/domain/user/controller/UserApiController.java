package boogi.apiserver.domain.user.controller;

import boogi.apiserver.domain.user.application.UserService;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.UserDetailInfoDto;
import boogi.apiserver.global.argument_resolver.session.Session;
import boogi.apiserver.global.constant.SessionInfoConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/users")
public class UserApiController {

    private final UserService userService;

    @PostMapping("/token/{email}")
    public ResponseEntity<Object> issueToken(HttpServletRequest request, @PathVariable String email) {
        // TODO: email 기반으로 User 찾고 그에 대한 userId로 세션만들어주기

        HttpSession preSession = request.getSession(false);
        if (preSession != null) {
            preSession.invalidate();
        }

        HttpSession session = request.getSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDetailInfoDto> getUserProfileInfo(
            @PathVariable Long userId, @Session Long sessionUserId) {
        User user = userService.getUserInfo(userId);
        UserDetailInfoDto userDetailDto = UserDetailInfoDto.of(user);
        return ResponseEntity.status(HttpStatus.OK).body(userDetailDto);
    }
}
