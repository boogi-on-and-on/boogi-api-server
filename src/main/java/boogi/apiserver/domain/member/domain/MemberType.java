package boogi.apiserver.domain.member.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum MemberType {
    MANAGER, SUB_MANAGER, NORMAL;

    @JsonCreator
    public static MemberType from(String s) {
        return MemberType.valueOf(s.toUpperCase());
    }
}
