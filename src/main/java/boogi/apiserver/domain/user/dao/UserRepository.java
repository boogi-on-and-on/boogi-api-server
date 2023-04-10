package boogi.apiserver.domain.user.dao;

import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.exception.UserNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {

    Optional<User> findByEmailValue(String email);

    default User findUserById(Long userId) {
        return this.findById(userId).orElseThrow(UserNotFoundException::new);
    }
}
