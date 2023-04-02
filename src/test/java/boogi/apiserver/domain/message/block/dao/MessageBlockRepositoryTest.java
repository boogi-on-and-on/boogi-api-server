package boogi.apiserver.domain.message.block.dao;

import boogi.apiserver.annotations.CustomDataJpaTest;
import boogi.apiserver.builder.TestMessageBlock;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.message.block.domain.MessageBlock;
import boogi.apiserver.domain.message.block.dto.dto.MessageBlockedUserDto;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
    @DisplayName("해당 유저가 차단한 멤버 목록 조회하기")
    void getBlockedUsers() {
        //given
        final User u1 = TestUser.builder()
                .username("탈퇴당한유저하나")
                .tagNumber("#0001")
                .build();
        final User u2 = TestUser.builder()
                .username("탈퇴당한유저둘")
                .tagNumber("#0001")
                .build();
        final User user = TestUser.builder()
                .username("유저")
                .build();
        userRepository.saveAll(List.of(u1, u2, user));

        final MessageBlock block1 = TestMessageBlock.builder()
                .user(user)
                .blockedUser(u1)
                .blocked(true)
                .build();
        final MessageBlock block2 = TestMessageBlock.builder()
                .user(user)
                .blockedUser(u2)
                .blocked(false)
                .build();
        messageBlockRepository.saveAll(List.of(block1, block2));

        cleanPersistenceContext();

        //when
        List<MessageBlockedUserDto> blockedUsersDto = messageBlockRepository.getBlockedUsers(user.getId());

        //then
        assertThat(blockedUsersDto).hasSize(1);

        MessageBlockedUserDto first = blockedUsersDto.get(0);
        assertThat(first.getUserId()).isEqualTo(u1.getId());
        assertThat(first.getNameTag()).isEqualTo(u1.getUsername() + u1.getTagNumber());
    }

    @Test
    @DisplayName("유저 ID와 차단한 유저 ID로 messageBlock를 조회한다.")
    void getMessageBlockByUserId() {
        //given
        final User user = TestUser.builder().build();
        final User blockedUser = TestUser.builder().build();
        userRepository.saveAll(List.of(user, blockedUser));

        final MessageBlock block = TestMessageBlock.builder()
                .user(user)
                .blockedUser(blockedUser)
                .build();
        messageBlockRepository.save(block);

        cleanPersistenceContext();

        //when
        MessageBlock findBlock = messageBlockRepository.getMessageBlockByUserId(user.getId(), blockedUser.getId())
                .orElseGet(Assertions::fail);

        //then
        assertThat(findBlock.getId()).isEqualTo(block.getId());
        assertThat(findBlock.getUser().getId()).isEqualTo(user.getId());
        assertThat(findBlock.getBlockedUser().getId()).isEqualTo(blockedUser.getId());
    }

    @Test
    @DisplayName("유저 ID와 차단한 유저 ID들로 messageBlock들을 조회한다.")
    void getMessageBlocksByUserIds() {
        //given
        final User user = TestUser.builder().build();
        final User blockedUser1 = TestUser.builder().build();
        final User blockedUser2 = TestUser.builder().build();
        userRepository.saveAll(List.of(user, blockedUser1, blockedUser2));

        final MessageBlock block1 = TestMessageBlock.builder()
                .user(user)
                .blockedUser(blockedUser1)
                .build();
        final MessageBlock block2 = TestMessageBlock.builder()
                .user(user)
                .blockedUser(blockedUser2)
                .build();
        messageBlockRepository.saveAll(List.of(block1, block2));

        cleanPersistenceContext();

        //when
        List<Long> blockedUserIds = List.of(blockedUser1.getId(), blockedUser2.getId());
        List<MessageBlock> blocks = messageBlockRepository.getMessageBlocksByUserIds(user.getId(), blockedUserIds);

        //then
        assertThat(blocks).hasSize(2);
        assertThat(blocks).extracting("id").containsExactlyInAnyOrder(block1.getId(), block2.getId());
        assertThat(blocks).extracting("user").extracting("id").containsOnly(user.getId());
        assertThat(blocks).extracting("blockedUser").extracting("id")
                .containsExactlyInAnyOrder(blockedUser1.getId(), blockedUser2.getId());
    }

    @Test
    @DisplayName("유저 ID와 차단할 유저 ID들로 필터링한 messageBlock들의 blocked를 true로 변경한다.")
    void updateBulkBlockedStatus() {
        //given
        final User user = TestUser.builder().build();

        final User blockedUser1 = TestUser.builder().build();
        final User blockedUser2 = TestUser.builder().build();
        userRepository.saveAll(List.of(user, blockedUser1, blockedUser2));

        final MessageBlock block1 = TestMessageBlock.builder()
                .blocked(false)
                .user(user)
                .blockedUser(blockedUser1)
                .build();
        final MessageBlock block2 = TestMessageBlock.builder()
                .user(user)
                .blockedUser(blockedUser2)
                .build();
        messageBlockRepository.saveAll(List.of(block1, block2));

        //when
        List<Long> blockedUserIds = List.of(block1.getBlockedUser().getId(), block2.getBlockedUser().getId());
        messageBlockRepository.updateBulkBlockedStatus(user.getId(), blockedUserIds);

        cleanPersistenceContext();

        //then
        List<MessageBlock> messageBlocks = messageBlockRepository.findAll();
        assertThat(messageBlocks).hasSize(2);
        assertThat(messageBlocks).extracting("id")
                .containsExactlyInAnyOrder(block1.getId(), block2.getId());
        assertThat(messageBlocks).extracting("user").extracting("id")
                .containsOnly(user.getId());
        assertThat(messageBlocks).extracting("blockedUser").extracting("id")
                .containsExactlyInAnyOrder(blockedUser1.getId(), blockedUser2.getId());
    }

    @Nested
    @DisplayName("송신 유저 ID와 수신 유저 ID로 쪽지 차단 상태 확인시")
    class ExistsBlockedFromReceiver {
        @Test
        @DisplayName("쪽지 차단 상태인 경우 true로 성공한다.")
        void blockedSuccess() {
            User sender = TestUser.builder().build();
            User receiver = TestUser.builder().build();
            userRepository.saveAll(List.of(sender, receiver));

            MessageBlock messageBlock = TestMessageBlock.builder()
                    .user(sender)
                    .blockedUser(receiver)
                    .blocked(true)
                    .build();
            messageBlockRepository.save(messageBlock);

            cleanPersistenceContext();

            boolean isExists = messageBlockRepository.existsBlockedFromReceiver(sender.getId(), receiver.getId());

            assertThat(isExists).isTrue();
        }

        @Test
        @DisplayName("쪽지 차단 상태가 아닌 경우 false로 성공한다.")
        void notBlockedSuccess() {
            User sender = TestUser.builder().build();
            User receiver = TestUser.builder().build();
            userRepository.saveAll(List.of(sender, receiver));

            MessageBlock messageBlock = TestMessageBlock.builder()
                    .user(sender)
                    .blockedUser(receiver)
                    .blocked(false)
                    .build();
            messageBlockRepository.save(messageBlock);

            cleanPersistenceContext();

            boolean existMessageBlockResult =
                    messageBlockRepository.existsBlockedFromReceiver(sender.getId(), receiver.getId());
            boolean notExistMessageBlockResult =
                    messageBlockRepository.existsBlockedFromReceiver(receiver.getId(), sender.getId());

            assertThat(existMessageBlockResult).isFalse();
            assertThat(notExistMessageBlockResult).isFalse();
        }
    }

    @Test
    @DisplayName("유저 ID로 차단한 유저 정보들을 조회한다.")
    void findMessageBlocksByUserId() {
        final User u1 = TestUser.builder()
                .username("탈퇴당한유저하나")
                .tagNumber("#0001")
                .build();
        final User u2 = TestUser.builder()
                .username("탈퇴당한유저둘")
                .tagNumber("#0001")
                .build();
        final User user = TestUser.builder()
                .username("유저")
                .build();
        userRepository.saveAll(List.of(u1, u2, user));

        final MessageBlock block1 = TestMessageBlock.builder()
                .user(user)
                .blockedUser(u1)
                .blocked(true)
                .build();
        final MessageBlock block2 = TestMessageBlock.builder()
                .user(user)
                .blockedUser(u2)
                .blocked(false)
                .build();
        messageBlockRepository.saveAll(List.of(block1, block2));

        cleanPersistenceContext();

        List<MessageBlock> messageBlocks = messageBlockRepository.findMessageBlocksByUserId(user.getId());

        assertThat(messageBlocks).hasSize(1);

        MessageBlock first = messageBlocks.get(0);
        assertThat(first.getId()).isEqualTo(block1.getId());
        assertThat(first.getUser().getId()).isEqualTo(user.getId());
        assertThat(first.getBlockedUser().getId()).isEqualTo(u1.getId());
        assertThat(first.isBlocked()).isTrue();
    }

    private void cleanPersistenceContext() {
        em.flush();
        em.clear();
    }
}