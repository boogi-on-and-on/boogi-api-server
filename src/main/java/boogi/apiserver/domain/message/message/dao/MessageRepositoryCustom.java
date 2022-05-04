package boogi.apiserver.domain.message.message.dao;


import boogi.apiserver.domain.message.message.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageRepositoryCustom {

    Page<Message> findMessagesByOpponentIdAndMyId(Long opponentId, Long myId, Pageable pageable);
}
