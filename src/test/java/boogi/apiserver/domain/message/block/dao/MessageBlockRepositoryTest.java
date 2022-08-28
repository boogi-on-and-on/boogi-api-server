package boogi.apiserver.domain.message.block.dao;

import boogi.apiserver.annotations.CustomDataJpaTest;
import boogi.apiserver.domain.message.block.domain.MessageBlock;
import boogi.apiserver.domain.message.block.dto.MessageBlockedUserDto;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CustomDataJpaTest
class MessageBlockRepositoryTest {

    @Autowired
    MessageBlockRepository messageBlockRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("차단한 멤버 목록 조회하기")
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
    @DisplayName("userId로 messageBlock 로우 가져오기")
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

    @Test
    @DisplayName("2개 이상 userId로 messageBlock 로우 가져오기")
    void getMessageBlocksByUserIds() {
        //given
        User user = User.builder().build();
        User blockedUser1 = User.builder().build();
        User blockedUser2 = User.builder().build();
        userRepository.saveAll(List.of(user, blockedUser1, blockedUser2));

        MessageBlock block1 = MessageBlock.builder()
                .user(user)
                .blockedUser(blockedUser1)
                .build();
        MessageBlock block2 = MessageBlock.builder()
                .user(user)
                .blockedUser(blockedUser2)
                .build();
        messageBlockRepository.saveAll(List.of(block1, block2));

        //when
        List<MessageBlock> blocks = messageBlockRepository.getMessageBlocksByUserIds(user.getId(), List.of(blockedUser1.getId(), blockedUser2.getId()));

        //then
        assertThat(blocks).containsExactlyInAnyOrderElementsOf(List.of(block1, block2));
    }

    @Test
    @DisplayName("messageBlock의 block update bulk")
    void updateBulkBlockedStatus() {
        //given
        User blockedUser1 = User.builder().build();
        User blockedUser2 = User.builder().build();
        userRepository.saveAll(List.of(blockedUser1, blockedUser2));

        MessageBlock block1 = MessageBlock.builder()
                .blocked(false)
                .blockedUser(blockedUser1)
                .build();
        MessageBlock block2 = MessageBlock.builder()
                .blocked(null)
                .blockedUser(blockedUser2)
                .build();
        messageBlockRepository.saveAll(List.of(block1, block2));

        //when
        messageBlockRepository.updateBulkBlockedStatus(List.of(block1.getBlockedUser().getId(), block2.getBlockedUser().getId()));

        em.flush();
        em.clear();

        //then
        MessageBlock b1 = messageBlockRepository.findById(block1.getId()).get();
        MessageBlock b2 = messageBlockRepository.findById(block2.getId()).get();

        assertThat(b1.getBlocked()).isEqualTo(true);
        assertThat(b2.getBlocked()).isEqualTo(true);
    }
}