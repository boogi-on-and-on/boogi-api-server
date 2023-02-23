package boogi.apiserver.utils;

import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

public class TestTimeReflection {

    public static <T> void setCreatedAt(T t, LocalDateTime createdAt) {
        ReflectionTestUtils.setField(t, "createdAt", createdAt);
    }

    public static <T> void setUpdatedAt(T t, LocalDateTime updatedAt) {
        ReflectionTestUtils.setField(t, "updatedAt", updatedAt);
    }
}
