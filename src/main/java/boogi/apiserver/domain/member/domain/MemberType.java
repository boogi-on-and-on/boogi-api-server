package boogi.apiserver.domain.member.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.List;

public enum MemberType {
    MANAGER, SUB_MANAGER, NORMAL;

    public boolean hasManagerAuth() {
        return MANAGER.equals(this);
    }

    public boolean hasSubManagerAuth() {
        return List.of(MemberType.MANAGER, MemberType.SUB_MANAGER).contains(this);
    }

    @JsonCreator
    public static MemberType from(String s) {
        return MemberType.valueOf(s.toUpperCase());
    }
}
