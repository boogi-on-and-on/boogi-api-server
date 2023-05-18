package boogi.apiserver.utils.controller;

import boogi.apiserver.domain.alarm.alarm.application.AlarmCommand;
import boogi.apiserver.domain.alarm.alarm.application.AlarmQuery;
import boogi.apiserver.domain.alarm.alarm.controller.AlarmApiController;
import boogi.apiserver.domain.alarm.alarmconfig.application.AlarmConfigCommand;
import boogi.apiserver.domain.comment.application.CommentCommand;
import boogi.apiserver.domain.comment.application.CommentQuery;
import boogi.apiserver.domain.comment.controller.CommentApiController;
import boogi.apiserver.domain.community.community.application.CommunityCommand;
import boogi.apiserver.domain.community.community.application.CommunityQuery;
import boogi.apiserver.domain.community.community.controller.CommunityApiController;
import boogi.apiserver.domain.community.community.repository.CommunityRepository;
import boogi.apiserver.domain.community.joinrequest.application.JoinRequestCommand;
import boogi.apiserver.domain.community.joinrequest.application.JoinRequestQuery;
import boogi.apiserver.domain.like.application.LikeCommand;
import boogi.apiserver.domain.like.application.LikeQuery;
import boogi.apiserver.domain.like.controller.LikeApiController;
import boogi.apiserver.domain.member.application.MemberCommand;
import boogi.apiserver.domain.member.application.MemberQuery;
import boogi.apiserver.domain.member.controller.MemberApiController;
import boogi.apiserver.domain.message.block.application.MessageBlockCommand;
import boogi.apiserver.domain.message.block.application.MessageBlockQuery;
import boogi.apiserver.domain.message.message.application.MessageCommand;
import boogi.apiserver.domain.message.message.application.MessageQuery;
import boogi.apiserver.domain.message.message.controller.MessageApiController;
import boogi.apiserver.domain.notice.application.NoticeCommand;
import boogi.apiserver.domain.notice.application.NoticeQuery;
import boogi.apiserver.domain.notice.controller.NoticeApiController;
import boogi.apiserver.domain.post.post.application.PostCommand;
import boogi.apiserver.domain.post.post.application.PostQuery;
import boogi.apiserver.domain.post.post.controller.PostApiController;
import boogi.apiserver.domain.report.application.ReportCommand;
import boogi.apiserver.domain.report.controller.ReportApiController;
import boogi.apiserver.domain.user.application.UserQuery;
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
    protected AlarmCommand alarmCommand;

    @MockBean
    protected AlarmQuery alarmQuery;

    @MockBean
    protected AlarmConfigCommand alarmConfigCommand;

    @MockBean
    protected CommentCommand commentCommand;

    @MockBean
    protected CommentQuery commentQuery;

    @MockBean
    protected LikeCommand likeCommand;

    @MockBean
    protected LikeQuery likeQuery;

    @MockBean
    protected ReportCommand reportCommand;

    @MockBean
    protected JoinRequestCommand joinRequestCommand;

    @MockBean
    protected JoinRequestQuery joinRequestQuery;

    @MockBean
    protected CommunityCommand communityCommand;

    @MockBean
    protected CommunityQuery communityQuery;

    @MockBean
    protected MemberCommand memberCommand;

    @MockBean
    protected MemberQuery memberQuery;

    @MockBean
    protected NoticeCommand noticeCommand;

    @MockBean
    protected NoticeQuery noticeQuery;

    @MockBean
    protected PostCommand postCommand;

    @MockBean
    protected PostQuery postQuery;

    @MockBean
    protected MessageCommand messageCommand;

    @MockBean
    protected MessageQuery messageQuery;

    @MockBean
    protected MessageBlockCommand messageBlockCommand;

    @MockBean
    protected MessageBlockQuery messageBlockQuery;

    @MockBean
    protected UserQuery userQuery;

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
