package boogi.apiserver.global.util.time;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class CustomDateTimeFormatter {

    public final static Map<TimePatternType, DateTimeFormatter> formatterMap = new HashMap<>();

    static {
        Stream.of(TimePatternType.values())
                .forEach(format -> formatterMap.put(format, DateTimeFormatter.ofPattern(format.getFormat())));
    }

    public static String toString(LocalDateTime localDateTime, TimePatternType type) {
        DateTimeFormatter formatter = formatterMap.get(type);
        if (Objects.isNull(formatter)) {
            throw new RuntimeException("invalid pattern type");
        }

        return formatter.format(localDateTime);
    }
}
