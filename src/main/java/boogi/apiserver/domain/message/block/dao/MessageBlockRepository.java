package boogi.apiserver.domain.message.block.dao;

import boogi.apiserver.domain.message.block.domain.MessageBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageBlockRepository extends JpaRepository<MessageBlock, Long>, MessageBlockRepositoryCustom {
}