package boogi.apiserver.global.util;

public final class SessionUtil {

    public static Long transferObjectToLong(Object attr) {
        if (attr instanceof Integer) {
            return Long.valueOf((Integer) attr);
        } else if (attr instanceof Long) {
            return (Long) attr;
        }
        throw new IllegalStateException("Wrong Attribute Type Error");
    }
}
