package boogi.apiserver.domain.message.block.dao;

import boogi.apiserver.domain.message.block.domain.MessageBlock;
import boogi.apiserver.domain.message.block.dto.MessageBlockedUserDto;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MessageBlockRepositoryTest {

    @Autowired
    MessageBlockRepository messageBlockRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void getBlockedUsers() {
        //given
        User u1 = User.builder()
                .username("탈퇴당한유저1")
                .tagNumber("#0001")
                .build();
        User u2 = User.builder()
                .username("탈퇴당한유저2")
                .tagNumber("#0001")
                .build();
        User user = User.builder()
                .username("유저")
                .build();
        userRepository.saveAll(List.of(u1, u2, user));

        MessageBlock block1 = MessageBlock.builder()
                .user(user)
                .blockedUser(u1)
                .blocked(true)
                .build();
        MessageBlock block2 = MessageBlock.builder()
                .user(user)
                .blockedUser(u2)
                .blocked(false)
                .build();
        messageBlockRepository.saveAll(List.of(block1, block2));

        //when
        List<MessageBlockedUserDto> blockedUsersDto = messageBlockRepository.getBlockedUsers(user.getId());

        //then
        assertThat(blockedUsersDto.size()).isEqualTo(1);

        MessageBlockedUserDto first = blockedUsersDto.get(0);
        assertThat(first.getUserId()).isEqualTo(u1.getId());
        assertThat(first.getNameTag()).isEqualTo(u1.getUsername() + u1.getTagNumber());
    }

    @Test
    void getMessageBlockByUserId() {
        //given
        User user = User.builder().build();
        User blockedUser = User.builder().build();
        userRepository.saveAll(List.of(user, blockedUser));

        MessageBlock block = MessageBlock.builder()
                .user(user)
                .blockedUser(blockedUser)
                .build();
        messageBlockRepository.save(block);

        //when
        MessageBlock findBlock = messageBlockRepository.getMessageBlockByUserId(user.getId(), blockedUser.getId());

        //then
        assertThat(findBlock).isEqualTo(block);
    }
}