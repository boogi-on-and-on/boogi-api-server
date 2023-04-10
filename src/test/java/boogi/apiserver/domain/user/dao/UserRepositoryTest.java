package boogi.apiserver.domain.user.dao;

import boogi.apiserver.annotations.CustomDataJpaTest;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.exception.UserNotFoundException;
import boogi.apiserver.utils.PersistenceUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CustomDataJpaTest
class UserRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    private UserRepository userRepository;

    private PersistenceUtil persistenceUtil;

    @BeforeEach
    void init() {
        persistenceUtil = new PersistenceUtil(em);
    }

    @Test
    @DisplayName("Email로 유저를 조회한다.")
    void findByEmailValue() {
        final String EMAIL = "abcdef123@gmail.com";

        User user = TestUser.builder().email(EMAIL).build();
        userRepository.save(user);

        persistenceUtil.cleanPersistenceContext();

        User findUser = userRepository.findByEmailValue(EMAIL)
                .orElseGet(Assertions::fail);

        assertThat(findUser.getId()).isEqualTo(user.getId());
        assertThat(findUser.getEmail()).isEqualTo(EMAIL);
    }

    @Nested
    @DisplayName("ID로 유저 조회시")
    class findUserById {
        @Test
        @DisplayName("성공")
        void success() {
            final User user = TestUser.builder().build();
            userRepository.save(user);

            persistenceUtil.cleanPersistenceContext();

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
        List<User> users = IntStream.range(0, 10)
                .mapToObj(i -> TestUser.builder().build())
                .collect(Collectors.toList());
        userRepository.saveAll(users);

        persistenceUtil.cleanPersistenceContext();

        List<Long> userIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toList());

        List<User> findUsers = userRepository.findUsersByIds(userIds);

        assertThat(findUsers).hasSize(10);
        assertThat(findUsers).extracting("id").containsExactlyElementsOf(userIds);
    }
}