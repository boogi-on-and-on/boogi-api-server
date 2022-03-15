package boogi.apiserver.domain.community.community.application;

import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.hashtag.community.application.CommunityHashtagCoreService;
import boogi.apiserver.domain.member.application.MemberCoreService;
import boogi.apiserver.domain.user.application.UserCoreService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommunityCoreServiceTest {

    @Mock
    CommunityRepository communityRepository;

    @Mock
    UserCoreService userCoreService;

    @Mock
    MemberCoreService memberCoreService;

    @Mock
    CommunityHashtagCoreService communityHashtagCoreService;

    @InjectMocks
    CommunityCoreService communityCoreService;


    //    @Test
    void 커뮤니티생성_성공() {
    }
}