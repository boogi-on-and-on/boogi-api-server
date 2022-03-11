package boogi.apiserver.domain.comment.dao;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.member.dao.MemberRepository;
import boogi.apiserver.domain.user.dao.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private MemberRepository memberRepository;

}
