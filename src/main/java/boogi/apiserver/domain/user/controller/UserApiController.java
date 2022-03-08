package boogi.apiserver.domain.user.controller;

import boogi.apiserver.global.constant.SessionInfoConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/users")
public class UserApiController {

    @PostMapping("/token/{email}")
    public ResponseEntity<Object> issueToken(HttpServletRequest request, @PathVariable String email) {
        //TODO: email 기반으로 User 찾고 그에 대한 userId로 세션만들어주기

        HttpSession preSession = request.getSession(false);
        if (preSession != null) {
            preSession.invalidate();
        }

        HttpSession session = request.getSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
