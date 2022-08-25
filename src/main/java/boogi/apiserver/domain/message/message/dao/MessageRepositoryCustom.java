package boogi.apiserver.domain.message.message.dao;


import boogi.apiserver.domain.message.message.domain.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface MessageRepositoryCustom {

    Slice<Message> findMessagesByOpponentIdAndMyId(Long opponentId, Long myId, Pageable pageable);
}
