package boogi.apiserver.domain.message.block.dao;

import boogi.apiserver.annotations.CustomDataJpaTest;
import boogi.apiserver.domain.message.block.domain.MessageBlock;
import boogi.apiserver.domain.message.block.dto.dto.MessageBlockedUserDto;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

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
        final User u1 = TestEmptyEntityGenerator.User();
        ReflectionTestUtils.setField(u1, "username", "탈퇴당한유저1");
        ReflectionTestUtils.setField(u1, "tagNumber", "#0001");

        final User u2 = TestEmptyEntityGenerator.User();
        ReflectionTestUtils.setField(u2, "username", "탈퇴당한유저2");
        ReflectionTestUtils.setField(u2, "tagNumber", "#0001");

        final User user = TestEmptyEntityGenerator.User();
        ReflectionTestUtils.setField(user, "username", "유저");

        userRepository.saveAll(List.of(u1, u2, user));

        final MessageBlock block1 = TestEmptyEntityGenerator.MessageBlock();
        ReflectionTestUtils.setField(block1, "user", user);
        ReflectionTestUtils.setField(block1, "blockedUser", u1);
        ReflectionTestUtils.setField(block1, "blocked", true);

        final MessageBlock block2 = TestEmptyEntityGenerator.MessageBlock();
        ReflectionTestUtils.setField(block2, "user", user);
        ReflectionTestUtils.setField(block2, "blockedUser", u2);
        ReflectionTestUtils.setField(block2, "blocked", false);

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
        final User user = TestEmptyEntityGenerator.User();
        final User blockedUser = TestEmptyEntityGenerator.User();

        userRepository.saveAll(List.of(user, blockedUser));

        final MessageBlock block = TestEmptyEntityGenerator.MessageBlock();
        ReflectionTestUtils.setField(block, "user", user);
        ReflectionTestUtils.setField(block, "blockedUser", blockedUser);

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
        final User user = TestEmptyEntityGenerator.User();
        final User blockedUser1 = TestEmptyEntityGenerator.User();
        final User blockedUser2 = TestEmptyEntityGenerator.User();

        userRepository.saveAll(List.of(user, blockedUser1, blockedUser2));

        final MessageBlock block1 = TestEmptyEntityGenerator.MessageBlock();
        ReflectionTestUtils.setField(block1, "user", user);
        ReflectionTestUtils.setField(block1, "blockedUser", blockedUser1);

        final MessageBlock block2 = TestEmptyEntityGenerator.MessageBlock();
        ReflectionTestUtils.setField(block2, "user", user);
        ReflectionTestUtils.setField(block2, "blockedUser", blockedUser2);

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
        final User blockedUser1 = TestEmptyEntityGenerator.User();
        final User blockedUser2 = TestEmptyEntityGenerator.User();

        userRepository.saveAll(List.of(blockedUser1, blockedUser2));

        final MessageBlock block1 = TestEmptyEntityGenerator.MessageBlock();
        ReflectionTestUtils.setField(block1, "blocked", false);
        ReflectionTestUtils.setField(block1, "blockedUser", blockedUser1);

        final MessageBlock block2 = TestEmptyEntityGenerator.MessageBlock();
        ReflectionTestUtils.setField(block2, "blocked", null);
        ReflectionTestUtils.setField(block2, "blockedUser", blockedUser2);

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