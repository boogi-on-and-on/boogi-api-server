package boogi.apiserver.domain.message.message.dao;

import boogi.apiserver.domain.message.message.domain.Message;
import boogi.apiserver.domain.message.message.exception.MessageNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long>, MessageRepositoryCustom {

    @Query(value = "SELECT message_id, sender_id, receiver_id, content, canceled_at, created_at, updated_at, blocked_message " +
            "FROM (SELECT *, RANK() OVER (PARTITION BY m.sender_id, m.receiver_id ORDER BY m.created_at DESC) AS a " +
            "FROM message AS m " +
            "WHERE (m.sender_id = :userId AND m.receiver_id NOT IN (:blockedUserIds)) " +
            "OR (m.receiver_id = :userId AND m.sender_id NOT IN (:blockedUserIds))) AS rankrow " +
            "WHERE rankrow.a <= 1 ORDER BY created_at DESC",
            nativeQuery = true)
    List<Message> findMessageByUserIdWithoutBlockedUser(@Param("userId") Long userId, @Param("blockedUserIds") List<Long> blockedUserIds);

    default Message findByMessageId(Long messageId) {
        return this.findById(messageId).orElseThrow(MessageNotFoundException::new);

    }
}
