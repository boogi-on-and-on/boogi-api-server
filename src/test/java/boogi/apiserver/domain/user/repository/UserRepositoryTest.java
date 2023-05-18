package boogi.apiserver.domain.user.repository;

import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.exception.UserNotFoundException;
import boogi.apiserver.utils.RepositoryTest;
import boogi.apiserver.utils.fixture.UserFixture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static boogi.apiserver.utils.fixture.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserRepositoryTest extends RepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Email로 유저를 조회한다.")
    void findByEmailValue() {

        final User user = SUNDO.toUser();
        userRepository.save(user);

        cleanPersistenceContext();

        User findUser = userRepository.findByEmailValue(SUNDO.email)
                .orElseGet(Assertions::fail);

        assertThat(findUser.getId()).isEqualTo(user.getId());
        assertThat(findUser.getEmail()).isEqualTo(SUNDO.email);
    }

    @Nested
    @DisplayName("ID로 유저 조회시")
    class findUserById {
        @Test
        @DisplayName("성공")
        void success() {
            final User user = SUNDO.toUser();

            userRepository.save(user);

            cleanPersistenceContext();

            final User findUser = userRepository.findUserById(user.getId());
            assertThat(findUser.getId()).isEqualTo(user.getId());
        }

        @Test
        @DisplayName("throw UserNotFoundException")
        void throwException() {
            assertThatThrownBy(() -> {
                userRepository.findUserById(1L);
            }).isInstanceOf(UserNotFoundException.class);
        }
    }

    @Test
    @DisplayName("유저 ID들로 유저들을 조회한다.")
    void findUsersByIds() {
        final List<User> users = Stream.of(SUNDO, DEOKHWAN, YONGJIN)
                .map(UserFixture::toUser)
                .collect(Collectors.toList());
        userRepository.saveAll(users);

        cleanPersistenceContext();

        List<Long> userIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toList());

        List<User> findUsers = userRepository.findUsersByIds(userIds);

        assertThat(findUsers).hasSize(3)
                .extracting(User::getId)
                .isEqualTo(userIds);
    }
}