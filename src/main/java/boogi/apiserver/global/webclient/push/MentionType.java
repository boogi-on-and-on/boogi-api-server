package boogi.apiserver.global.webclient.push;

import lombok.Getter;

@Getter
public enum MentionType {
    POST("post"), COMMENT("comment");

    final String type;

    MentionType(String type) {
        this.type = type;
    }
}
