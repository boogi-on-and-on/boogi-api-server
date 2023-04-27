package boogi.apiserver.utils.fixture;

import boogi.apiserver.global.constant.HeaderConst;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenFixture {

    private static final Map<String, String> TOKENS = new ConcurrentHashMap<>();


    public static String getSundoToken() {
        return getToken(UserFixture.SUNDO.email);
    }

    public static String getYongjinToken() {
        return getToken(UserFixture.YONGJIN.email);
    }

    public static String getDeokHwanToken() {
        return getToken(UserFixture.DEOKHWAN.email);
    }

    public static String getToken(String email) {
        return TOKENS.computeIfAbsent(email, e ->
                HttpMethodFixture.httpPost("/users/token/" + e).header(HeaderConst.AUTH_TOKEN)
        );
    }
}
