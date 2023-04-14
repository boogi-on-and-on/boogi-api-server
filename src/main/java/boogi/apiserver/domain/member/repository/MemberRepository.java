package boogi.apiserver.domain.member.repository;

import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.exception.MemberNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    default Member findMemberById(Long memberId) {
        return this.findById(memberId).orElseThrow(MemberNotFoundException::new);
    }
}
