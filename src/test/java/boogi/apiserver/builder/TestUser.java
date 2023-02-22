package boogi.apiserver.builder;

import boogi.apiserver.domain.user.domain.User;

public class TestUser {

    public static User.UserBuilder builder() {
        return User.builder()
                .email("ABC@gmail.com")
                .username("USERNAME")
                .department("DEPARTMENT")
                .tagNumber("#9999")
                .introduce("THIS IS INTRODUCE");
    }
}
