package boogi.apiserver.utils.fixture;

import java.time.LocalDateTime;

public class TimeFixture {
    public static final LocalDateTime STANDARD = LocalDateTime.of(2023, 3, 16, 0, 0);
    public static final LocalDateTime NEXT_DAY = STANDARD.plusDays(1);
}
