package boogi.apiserver.domain.member.dao;

import boogi.apiserver.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

}
