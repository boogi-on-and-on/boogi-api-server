package boogi.apiserver.global.util.time;

public enum TimePatternType {
    BASIC_FORMAT(TimePattern.BASIC_FORMAT);

    public final String format;

    TimePatternType(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }
}