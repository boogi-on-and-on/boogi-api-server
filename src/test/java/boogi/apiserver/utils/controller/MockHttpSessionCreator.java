package boogi.apiserver.utils.controller;

import boogi.apiserver.global.constant.SessionInfoConst;
import org.springframework.mock.web.MockHttpSession;

public class MockHttpSessionCreator {

    public static MockHttpSession dummySession() {
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);
        return session;
    }

    public static MockHttpSession session(Long userId) {
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionInfoConst.USER_ID, userId);
        return session;
    }
}
