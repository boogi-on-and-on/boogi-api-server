package boogi.apiserver.domain.report.application;

import boogi.apiserver.builder.*;
import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.message.message.dao.MessageRepository;
import boogi.apiserver.domain.message.message.domain.Message;
import boogi.apiserver.domain.message.message.exception.NotParticipatedUserException;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.report.dao.ReportRepository;
import boogi.apiserver.domain.report.domain.Report;
import boogi.apiserver.domain.report.domain.ReportReason;
import boogi.apiserver.domain.report.domain.ReportTarget;
import boogi.apiserver.domain.report.dto.request.CreateReportRequest;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReportCommandServiceTest {
    @InjectMocks
    private ReportCommandService reportCommandService;

    @Mock
    private MemberQueryService memberQueryService;

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


    @Nested
    @DisplayName("신고 생성시")
    class CreateReportRequestTest {
        private static final String REPORT_CONTENT = "테스트용 신고내용입니다.";

        @Captor
        ArgumentCaptor<Report> reportCaptor;

        @Test
        @DisplayName("공개 커뮤니티에 달린 댓글 신고시 성공한다.")
        void createCommentReportSuccess() {
            final User user = TestUser.builder().id(1L).build();

            final Community community = TestCommunity.builder().id(2L).isPrivate(false).build();

            final Post post = TestPost.builder().id(3L).community(community).build();

            final Comment comment = TestComment.builder().id(4L).post(post).build();

            given(userRepository.findByUserId(anyLong())).willReturn(user);
            given(commentRepository.findCommentById(anyLong())).willReturn(comment);

            CreateReportRequest request =
                    new CreateReportRequest(4L, ReportTarget.COMMENT, ReportReason.SWEAR, REPORT_CONTENT);

            reportCommandService.createReport(request, 1L);

            verify(reportRepository, times(1)).save(reportCaptor.capture());

            Report newReport = reportCaptor.getValue();
            assertThat(newReport.getCommunity()).isNull();
            assertThat(newReport.getMessage()).isNull();
            assertThat(newReport.getPost()).isNull();
            assertThat(newReport.getComment()).isNotNull();
            assertThat(newReport.getComment().getId()).isEqualTo(4L);
            assertThat(newReport.getReason()).isEqualTo(ReportReason.SWEAR);
            assertThat(newReport.getContent()).isEqualTo(REPORT_CONTENT);
        }

        @Test
        @DisplayName("비공개 커뮤니티에 달린 댓글 신고시 세션 유저의 멤버를 확인한다.")
        void createCommentReportAtPrivateCommunitySuccess() {
            final User user = TestUser.builder().id(1L).build();

            final Community community = TestCommunity.builder().id(2L).isPrivate(true).build();

            final Post post = TestPost.builder().id(3L).community(community).build();

            final Comment comment = TestComment.builder().id(4L).post(post).build();

            given(userRepository.findByUserId(anyLong())).willReturn(user);
            given(commentRepository.findCommentById(anyLong())).willReturn(comment);

            CreateReportRequest request =
                    new CreateReportRequest(4L, ReportTarget.COMMENT, ReportReason.SWEAR, REPORT_CONTENT);

            reportCommandService.createReport(request, 1L);

            verify(memberQueryService, times(1)).getMember(anyLong(), anyLong());
            verify(reportRepository, times(1)).save(reportCaptor.capture());

            Report newReport = reportCaptor.getValue();
            assertThat(newReport.getCommunity()).isNull();
            assertThat(newReport.getMessage()).isNull();
            assertThat(newReport.getPost()).isNull();
            assertThat(newReport.getComment()).isNotNull();
            assertThat(newReport.getComment().getId()).isEqualTo(4L);
            assertThat(newReport.getReason()).isEqualTo(ReportReason.SWEAR);
            assertThat(newReport.getContent()).isEqualTo(REPORT_CONTENT);
        }

        @Test
        @DisplayName("공개 커뮤니티에 달린 게시글 신고시 성공한다.")
        void createPostReportSuccess() {
            final User user = TestUser.builder().id(1L).build();

            final Community community = TestCommunity.builder().id(2L).isPrivate(false).build();

            final Post post = TestPost.builder().id(3L).community(community).build();

            given(userRepository.findByUserId(anyLong())).willReturn(user);
            given(postRepository.findByPostId(anyLong())).willReturn(post);

            CreateReportRequest request =
                    new CreateReportRequest(3L, ReportTarget.POST, ReportReason.SWEAR, REPORT_CONTENT);

            reportCommandService.createReport(request, 1L);

            verify(reportRepository, times(1)).save(reportCaptor.capture());

            Report newReport = reportCaptor.getValue();
            assertThat(newReport.getCommunity()).isNull();
            assertThat(newReport.getMessage()).isNull();
            assertThat(newReport.getComment()).isNull();
            assertThat(newReport.getPost()).isNotNull();
            assertThat(newReport.getPost().getId()).isEqualTo(3L);
            assertThat(newReport.getReason()).isEqualTo(ReportReason.SWEAR);
            assertThat(newReport.getContent()).isEqualTo(REPORT_CONTENT);
        }

        @Test
        @DisplayName("비공개 커뮤니티에 달린 게시글 신고시 세션 유저의 멤버를 확인한다.")
        void createPostReportAtPrivateCommunitySuccess() {
            final User user = TestUser.builder().id(1L).build();

            final Community community = TestCommunity.builder().id(2L).isPrivate(true).build();

            final Post post = TestPost.builder().id(3L).community(community).build();

            given(userRepository.findByUserId(anyLong())).willReturn(user);
            given(postRepository.findByPostId(anyLong())).willReturn(post);

            CreateReportRequest request =
                    new CreateReportRequest(1L, ReportTarget.POST, ReportReason.SWEAR, REPORT_CONTENT);

            reportCommandService.createReport(request, 1L);

            verify(memberQueryService, times(1)).getMember(anyLong(), anyLong());
            verify(reportRepository, times(1)).save(reportCaptor.capture());

            Report newReport = reportCaptor.getValue();
            assertThat(newReport.getCommunity()).isNull();
            assertThat(newReport.getMessage()).isNull();
            assertThat(newReport.getComment()).isNull();
            assertThat(newReport.getPost()).isNotNull();
            assertThat(newReport.getPost().getId()).isEqualTo(3L);
            assertThat(newReport.getReason()).isEqualTo(ReportReason.SWEAR);
            assertThat(newReport.getContent()).isEqualTo(REPORT_CONTENT);
        }

        @Test
        @DisplayName("커뮤니티 신고 생성에 성공한다.")
        void createCommunityReportSuccess() {
            final User user = TestUser.builder().id(1L).build();

            final Community community = TestCommunity.builder().id(2L).build();

            given(userRepository.findByUserId(anyLong())).willReturn(user);
            given(communityRepository.findCommunityById(anyLong())).willReturn(community);

            CreateReportRequest request =
                    new CreateReportRequest(2L, ReportTarget.COMMUNITY, ReportReason.SWEAR, REPORT_CONTENT);

            reportCommandService.createReport(request, 1L);

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
            final User user1 = TestUser.builder().id(1L).build();
            final User user2 = TestUser.builder().id(2L).build();

            final Message message = TestMessage.builder()
                    .id(3L)
                    .sender(user1)
                    .receiver(user2)
                    .build();

            given(userRepository.findByUserId(anyLong()))
                    .willReturn(user1);
            given(messageRepository.findMessageById(anyLong()))
                    .willReturn(message);

            CreateReportRequest request =
                    new CreateReportRequest(3L, ReportTarget.MESSAGE, ReportReason.SWEAR, REPORT_CONTENT);

            reportCommandService.createReport(request, 1L);

            verify(reportRepository, times(1)).save(reportCaptor.capture());

            Report newReport = reportCaptor.getValue();
            assertThat(newReport.getCommunity()).isNull();
            assertThat(newReport.getComment()).isNull();
            assertThat(newReport.getPost()).isNull();
            assertThat(newReport.getMessage()).isNotNull();
            assertThat(newReport.getMessage().getId()).isEqualTo(3L);
            assertThat(newReport.getReason()).isEqualTo(ReportReason.SWEAR);
            assertThat(newReport.getContent()).isEqualTo(REPORT_CONTENT);
        }

        @Test
        @DisplayName("본인이 포함되어 있는 쪽지가 아닐때 쪽지 신고시 NotParticipatedUserException 발생한다.")
        void createMessageReportNotMyMessageFail() {
            final User user1 = TestUser.builder().id(1L).build();
            final User user2 = TestUser.builder().id(2L).build();

            final Message message = TestMessage.builder()
                    .id(3L)
                    .sender(user1)
                    .receiver(user2)
                    .build();

            given(userRepository.findByUserId(anyLong()))
                    .willReturn(user1);
            given(messageRepository.findMessageById(anyLong()))
                    .willReturn(message);

            CreateReportRequest request =
                    new CreateReportRequest(3L, ReportTarget.MESSAGE, ReportReason.SWEAR, REPORT_CONTENT);

            assertThatThrownBy(() -> reportCommandService.createReport(request, 3L))
                    .isInstanceOf(NotParticipatedUserException.class);
        }
    }
}