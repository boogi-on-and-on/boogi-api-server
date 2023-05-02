package boogi.apiserver.utils.controller;

import boogi.apiserver.domain.alarm.alarm.application.AlarmCommandService;
import boogi.apiserver.domain.alarm.alarm.application.AlarmQueryService;
import boogi.apiserver.domain.alarm.alarm.controller.AlarmApiController;
import boogi.apiserver.domain.alarm.alarmconfig.application.AlarmConfigCommandService;
import boogi.apiserver.domain.comment.application.CommentCommandService;
import boogi.apiserver.domain.comment.application.CommentQueryService;
import boogi.apiserver.domain.comment.controller.CommentApiController;
import boogi.apiserver.domain.community.community.application.CommunityCommandService;
import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.controller.CommunityApiController;
import boogi.apiserver.domain.community.community.repository.CommunityRepository;
import boogi.apiserver.domain.community.joinrequest.application.JoinRequestCommandService;
import boogi.apiserver.domain.community.joinrequest.application.JoinRequestQueryService;
import boogi.apiserver.domain.like.application.LikeCommandService;
import boogi.apiserver.domain.like.application.LikeQueryService;
import boogi.apiserver.domain.like.controller.LikeApiController;
import boogi.apiserver.domain.member.application.MemberCommandService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.member.controller.MemberApiController;
import boogi.apiserver.domain.message.block.application.MessageBlockCommandService;
import boogi.apiserver.domain.message.block.application.MessageBlockQueryService;
import boogi.apiserver.domain.message.message.application.MessageCommandService;
import boogi.apiserver.domain.message.message.application.MessageQueryService;
import boogi.apiserver.domain.message.message.controller.MessageApiController;
import boogi.apiserver.domain.notice.application.NoticeCommandService;
import boogi.apiserver.domain.notice.application.NoticeQueryService;
import boogi.apiserver.domain.notice.controller.NoticeApiController;
import boogi.apiserver.domain.post.post.application.PostCommandService;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.controller.PostApiController;
import boogi.apiserver.domain.report.application.ReportCommandService;
import boogi.apiserver.domain.report.controller.ReportApiController;
import boogi.apiserver.domain.user.application.UserQueryService;
import boogi.apiserver.domain.user.controller.UserApiController;
import boogi.apiserver.domain.user.repository.UserRepository;
import boogi.apiserver.global.webclient.push.SendPushNotification;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith({MockitoExtension.class, RestDocumentationExtension.class})
@WebMvcTest(controllers = {
        AlarmApiController.class, CommentApiController.class, CommunityApiController.class,
        LikeApiController.class, MemberApiController.class, MessageApiController.class,
        NoticeApiController.class, PostApiController.class, ReportApiController.class,
        UserApiController.class
})
public class ControllerTest {

    protected static final MockHttpSession dummySession = MockHttpSessionCreator.dummySession();
    protected static final String TOKEN = "AUTH_TOKEN";

    @MockBean
    protected CommunityRepository communityRepository;

    @MockBean
    protected UserRepository userRepository;

    @MockBean
    protected AlarmCommandService alarmCommandService;

    @MockBean
    protected AlarmQueryService alarmQueryService;

    @MockBean
    protected AlarmConfigCommandService alarmConfigCommandService;

    @MockBean
    protected CommentCommandService commentCommandService;

    @MockBean
    protected CommentQueryService commentQueryService;

    @MockBean
    protected LikeCommandService likeCommandService;

    @MockBean
    protected LikeQueryService likeQueryService;

    @MockBean
    protected ReportCommandService reportCommandService;

    @MockBean
    protected JoinRequestCommandService joinRequestCommandService;

    @MockBean
    protected JoinRequestQueryService joinRequestQueryService;

    @MockBean
    protected CommunityCommandService communityCommandService;

    @MockBean
    protected CommunityQueryService communityQueryService;

    @MockBean
    protected MemberCommandService memberCommandService;

    @MockBean
    protected MemberQueryService memberQueryService;

    @MockBean
    protected NoticeCommandService noticeCommandService;

    @MockBean
    protected NoticeQueryService noticeQueryService;

    @MockBean
    protected PostCommandService postCommandService;

    @MockBean
    protected PostQueryService postQueryService;

    @MockBean
    protected MessageCommandService messageCommandService;

    @MockBean
    protected MessageQueryService messageQueryService;

    @MockBean
    protected MessageBlockCommandService messageBlockCommandService;

    @MockBean
    protected MessageBlockQueryService messageBlockQueryService;

    @MockBean
    protected UserQueryService userQueryService;

    @MockBean
    protected SendPushNotification sendPushNotification;

    protected ObjectMapper mapper = new ObjectMapper();

    protected MockMvc mvc;

    @BeforeEach
    public void setUp(WebApplicationContext context, RestDocumentationContextProvider provider) {
        this.mvc =
                MockMvcBuilders.webAppContextSetup(context)
                        .addFilter(new CharacterEncodingFilter("UTF-8", true))
                        .apply(documentationConfiguration(provider)
                                .operationPreprocessors()
                                .withRequestDefaults(prettyPrint())
                                .withResponseDefaults(prettyPrint()))
                        .alwaysDo(print())
                        .build();
    }
}
