package boogi.apiserver.global.interceptor;

import boogi.apiserver.global.constant.HeaderConst;
import boogi.apiserver.global.constant.SessionInfoConst;
import boogi.apiserver.global.error.exception.SessionNotFoundException;
import boogi.apiserver.global.util.SessionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Slf4j
public class SessionValidationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authToken = request.getHeader(HeaderConst.AUTH_TOKEN);
        HttpSession session = request.getSession(false);
        if (session == null || authToken == null) {
            if (authToken == null) {
                log.error("{} is null", HeaderConst.AUTH_TOKEN);
            } else {
                log.error("{} is invalid token", authToken);
            }

            throw new SessionNotFoundException();
        }
        Long userId = SessionUtil.transferObjectToLong(session.getAttribute(SessionInfoConst.USER_ID));

        log.info("{} is valid token. {}={}", authToken, SessionInfoConst.USER_ID, userId);
        return true;
    }
}
