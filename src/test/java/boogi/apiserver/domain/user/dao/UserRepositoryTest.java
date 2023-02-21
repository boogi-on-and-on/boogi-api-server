package boogi.apiserver.domain.user.dao;

import boogi.apiserver.annotations.CustomDataJpaTest;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.exception.UserNotFoundException;
import boogi.apiserver.utils.PersistenceUtil;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import static org.assertj.core.api.Assertions.*;

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

    @DisplayName("findByUserId 디폴트 메서드 테스트")
    @Nested
    class findByUserId {

        @Test
        @DisplayName("성공")
        void success() {
            final User user = TestEmptyEntityGenerator.User();
            userRepository.save(user);

            persistenceUtil.cleanPersistenceContext();

            final User findUser = userRepository.findByUserId(user.getId());
            assertThat(findUser.getId()).isEqualTo(user.getId());
        }

        @Test
        @DisplayName("throw UserNotFoundException")
        void throwException() {
            assertThatThrownBy(() -> {
                userRepository.findByUserId(1L);
            }).isInstanceOf(UserNotFoundException.class);
        }
    }

}