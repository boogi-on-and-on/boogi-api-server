package boogi.apiserver.global.util.time;

public enum TimePattern {
    BASIC_FORMAT(TimePattern.BASIC_FORMAT_STRING);

    public static final String BASIC_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS";

    public final String format;

    TimePattern(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }
}