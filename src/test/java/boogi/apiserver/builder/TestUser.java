package boogi.apiserver.builder;

import boogi.apiserver.domain.user.domain.User;

public class TestUser {

    public static User.UserBuilder builder() {
        return User.builder()
                .email("ABC@gmail.com")
                .username("테스트이름")
                .department("테스트학과")
                .tagNumber("#9999")
                .introduce("테스트 자기소개입니다. 반갑습니다.");
    }
}
