package boogi.apiserver.domain.report.application;

import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.comment.repository.CommentRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.repository.CommunityRepository;
import boogi.apiserver.domain.member.application.MemberQuery;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.message.message.domain.Message;
import boogi.apiserver.domain.message.message.exception.NotParticipatedUserException;
import boogi.apiserver.domain.message.message.repository.MessageRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.post.repository.PostRepository;
import boogi.apiserver.domain.report.domain.Report;
import boogi.apiserver.domain.report.domain.ReportReason;
import boogi.apiserver.domain.report.domain.ReportTarget;
import boogi.apiserver.domain.report.dto.request.CreateReportRequest;
import boogi.apiserver.domain.report.repository.ReportRepository;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static boogi.apiserver.utils.fixture.CommentFixture.COMMENT1;
import static boogi.apiserver.utils.fixture.CommunityFixture.ENGLISH;
import static boogi.apiserver.utils.fixture.CommunityFixture.POCS;
import static boogi.apiserver.utils.fixture.MemberFixture.SUNDO_POCS;
import static boogi.apiserver.utils.fixture.MessageFixture.MESSAGE1;
import static boogi.apiserver.utils.fixture.PostFixture.POST1;
import static boogi.apiserver.utils.fixture.UserFixture.SUNDO;
import static boogi.apiserver.utils.fixture.UserFixture.YONGJIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReportCommandTest {
    @InjectMocks
    private ReportCommand reportCommand;

    @Mock
    private MemberQuery memberQuery;

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    private final User user = SUNDO.toUser(1L);
    private final Community community = POCS.toCommunity(2L, List.of());
    private final Member member = SUNDO_POCS.toMember(3L, user, community);
    private final Post post = POST1.toPost(4L, member, community, List.of(), List.of());

    @Nested
    @DisplayName("신고 생성시")
    class CreateReportRequestTest {
        private static final String REPORT_CONTENT = "테스트용 신고내용입니다.";

        @Captor
        ArgumentCaptor<Report> reportCaptor;

        @Test
        @DisplayName("공개 커뮤니티에 달린 댓글 신고시 성공한다.")
        void createCommentReportSuccess() {
            Comment comment = COMMENT1.toComment(5L, post, member, null);

            given(userRepository.findUserById(anyLong())).willReturn(user);
            given(commentRepository.findCommentById(anyLong())).willReturn(comment);

            CreateReportRequest request =
                    new CreateReportRequest(5L, ReportTarget.COMMENT, ReportReason.SWEAR, REPORT_CONTENT);

            reportCommand.createReport(request, 1L);

            verify(reportRepository, times(1)).save(reportCaptor.capture());

            Report newReport = reportCaptor.getValue();
            assertThat(newReport.getCommunity()).isNull();
            assertThat(newReport.getMessage()).isNull();
            assertThat(newReport.getPost()).isNull();
            assertThat(newReport.getComment()).isNotNull();
            assertThat(newReport.getComment().getId()).isEqualTo(5L);
            assertThat(newReport.getReason()).isEqualTo(ReportReason.SWEAR);
            assertThat(newReport.getContent()).isEqualTo(REPORT_CONTENT);
        }

        @Test
        @DisplayName("비공개 커뮤니티에 달린 댓글 신고시 세션 유저의 멤버를 확인한다.")
        void createCommentReportAtPrivateCommunitySuccess() {
            Community community = ENGLISH.toCommunity(5L, List.of());
            Post post = POST1.toPost(6L, member, community, List.of(), List.of());
            Comment comment = COMMENT1.toComment(7L, post, member, null);

            given(userRepository.findUserById(anyLong())).willReturn(user);
            given(commentRepository.findCommentById(anyLong())).willReturn(comment);

            CreateReportRequest request =
                    new CreateReportRequest(7L, ReportTarget.COMMENT, ReportReason.SWEAR, REPORT_CONTENT);

            reportCommand.createReport(request, 1L);

            verify(memberQuery, times(1)).getMember(anyLong(), anyLong());
            verify(reportRepository, times(1)).save(reportCaptor.capture());

            Report newReport = reportCaptor.getValue();
            assertThat(newReport.getCommunity()).isNull();
            assertThat(newReport.getMessage()).isNull();
            assertThat(newReport.getPost()).isNull();
            assertThat(newReport.getComment()).isNotNull();
            assertThat(newReport.getComment().getId()).isEqualTo(7L);
            assertThat(newReport.getReason()).isEqualTo(ReportReason.SWEAR);
            assertThat(newReport.getContent()).isEqualTo(REPORT_CONTENT);
        }

        @Test
        @DisplayName("공개 커뮤니티에 달린 게시글 신고시 성공한다.")
        void createPostReportSuccess() {
            given(userRepository.findUserById(anyLong())).willReturn(user);
            given(postRepository.findPostById(anyLong())).willReturn(post);

            CreateReportRequest request =
                    new CreateReportRequest(4L, ReportTarget.POST, ReportReason.SWEAR, REPORT_CONTENT);

            reportCommand.createReport(request, 1L);

            verify(reportRepository, times(1)).save(reportCaptor.capture());

            Report newReport = reportCaptor.getValue();
            assertThat(newReport.getCommunity()).isNull();
            assertThat(newReport.getMessage()).isNull();
            assertThat(newReport.getComment()).isNull();
            assertThat(newReport.getPost()).isNotNull();
            assertThat(newReport.getPost().getId()).isEqualTo(4L);
            assertThat(newReport.getReason()).isEqualTo(ReportReason.SWEAR);
            assertThat(newReport.getContent()).isEqualTo(REPORT_CONTENT);
        }

        @Test
        @DisplayName("비공개 커뮤니티에 달린 게시글 신고시 세션 유저의 멤버를 확인한다.")
        void createPostReportAtPrivateCommunitySuccess() {
            Community community = ENGLISH.toCommunity(5L, List.of());
            Post post = POST1.toPost(6L, member, community, List.of(), List.of());

            given(userRepository.findUserById(anyLong())).willReturn(user);
            given(postRepository.findPostById(anyLong())).willReturn(post);

            CreateReportRequest request =
                    new CreateReportRequest(6L, ReportTarget.POST, ReportReason.SWEAR, REPORT_CONTENT);

            reportCommand.createReport(request, 1L);

            verify(memberQuery, times(1)).getMember(anyLong(), anyLong());
            verify(reportRepository, times(1)).save(reportCaptor.capture());

            Report newReport = reportCaptor.getValue();
            assertThat(newReport.getCommunity()).isNull();
            assertThat(newReport.getMessage()).isNull();
            assertThat(newReport.getComment()).isNull();
            assertThat(newReport.getPost()).isNotNull();
            assertThat(newReport.getPost().getId()).isEqualTo(6L);
            assertThat(newReport.getReason()).isEqualTo(ReportReason.SWEAR);
            assertThat(newReport.getContent()).isEqualTo(REPORT_CONTENT);
        }

        @Test
        @DisplayName("커뮤니티 신고 생성에 성공한다.")
        void createCommunityReportSuccess() {
            given(userRepository.findUserById(anyLong())).willReturn(user);
            given(communityRepository.findCommunityById(anyLong())).willReturn(community);

            CreateReportRequest request =
                    new CreateReportRequest(2L, ReportTarget.COMMUNITY, ReportReason.SWEAR, REPORT_CONTENT);

            reportCommand.createReport(request, 1L);

            verify(reportRepository, times(1)).save(reportCaptor.capture());

            Report newReport = reportCaptor.getValue();
            assertThat(newReport.getMessage()).isNull();
            assertThat(newReport.getComment()).isNull();
            assertThat(newReport.getPost()).isNull();
            assertThat(newReport.getCommunity()).isNotNull();
            assertThat(newReport.getCommunity().getId()).isEqualTo(2L);
            assertThat(newReport.getReason()).isEqualTo(ReportReason.SWEAR);
            assertThat(newReport.getContent()).isEqualTo(REPORT_CONTENT);
        }

        @Test
        @DisplayName("쪽지 신고 생성에 성공한다.")
        void createMessageReportSuccess() {
            User user2 = YONGJIN.toUser(2L);
            Message message = MESSAGE1.toMessage(5L, user, user2);

            given(userRepository.findUserById(anyLong())).willReturn(user);
            given(messageRepository.findMessageById(anyLong())).willReturn(message);

            CreateReportRequest request =
                    new CreateReportRequest(3L, ReportTarget.MESSAGE, ReportReason.SWEAR, REPORT_CONTENT);

            reportCommand.createReport(request, 1L);

            verify(reportRepository, times(1)).save(reportCaptor.capture());

            Report newReport = reportCaptor.getValue();
            assertThat(newReport.getCommunity()).isNull();
            assertThat(newReport.getComment()).isNull();
            assertThat(newReport.getPost()).isNull();
            assertThat(newReport.getMessage()).isNotNull();
            assertThat(newReport.getMessage().getId()).isEqualTo(5L);
            assertThat(newReport.getReason()).isEqualTo(ReportReason.SWEAR);
            assertThat(newReport.getContent()).isEqualTo(REPORT_CONTENT);
        }

        @Test
        @DisplayName("본인이 포함되어 있는 쪽지가 아닐때 쪽지 신고시 NotParticipatedUserException 발생한다.")
        void createMessageReportNotMyMessageFail() {
            User user2 = YONGJIN.toUser(2L);
            Message message = MESSAGE1.toMessage(5L, user, user2);

            given(userRepository.findUserById(anyLong())).willReturn(user);
            given(messageRepository.findMessageById(anyLong())).willReturn(message);

            CreateReportRequest request =
                    new CreateReportRequest(5L, ReportTarget.MESSAGE, ReportReason.SWEAR, REPORT_CONTENT);

            assertThatThrownBy(() -> reportCommand.createReport(request, 3L))
                    .isInstanceOf(NotParticipatedUserException.class);
        }
    }
}