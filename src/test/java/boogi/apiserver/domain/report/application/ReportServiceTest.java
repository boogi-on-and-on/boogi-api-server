package boogi.apiserver.domain.report.application;

import boogi.apiserver.domain.comment.dao.CommentRepository;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.dao.CommunityRepository;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.member.application.MemberValidationService;
import boogi.apiserver.domain.member.exception.NotJoinedMemberException;
import boogi.apiserver.domain.message.message.dao.MessageRepository;
import boogi.apiserver.domain.message.message.domain.Message;
import boogi.apiserver.domain.post.post.dao.PostRepository;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.report.dao.ReportRepository;
import boogi.apiserver.domain.report.domain.Report;
import boogi.apiserver.domain.report.domain.ReportReason;
import boogi.apiserver.domain.report.domain.ReportTarget;
import boogi.apiserver.domain.report.dto.request.CreateReport;
import boogi.apiserver.domain.user.application.UserQueryService;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.error.exception.InvalidValueException;
import boogi.apiserver.utils.TestEmptyEntityGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @InjectMocks
    private ReportService reportService;

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private CommunityQueryService communityQueryService;

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
    private MemberValidationService memberValidationService;


    @Nested
    @DisplayName("신고 생성시")
    class CreateReportTest {
        @Test
        @DisplayName("댓글 신고 생성에 성공한다.")
        void createCommentReportSuccess() {
            final User user = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user, "id", 1L);

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);
            ReflectionTestUtils.setField(community, "isPrivate", false);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 1L);
            ReflectionTestUtils.setField(post, "community", community);

            final Comment comment = TestEmptyEntityGenerator.Comment();
            ReflectionTestUtils.setField(comment, "id", 1L);
            ReflectionTestUtils.setField(comment, "post", post);

            given(userQueryService.getUser(anyLong()))
                    .willReturn(user);

            given(commentRepository.findCommentById(anyLong()))
                    .willReturn(Optional.of(comment));

            given(communityQueryService.getCommunity(anyLong()))
                    .willReturn(community);

            CreateReport createReport = new CreateReport(1L, ReportTarget.COMMENT, ReportReason.SWEAR, "신고");
            reportService.createReport(createReport, 1L);

            verify(reportRepository, times(1)).save(any(Report.class));
        }

        @Test
        @DisplayName("신고할 댓글 및 글이 달린 비공개 커뮤니티에 멤버가 아닐 경우 NotJoinedMemberException가 발생한다.")
        void createReportAtCommentOrCommunityWhenNotJoinedPrivateCommunityFail() {
            final User user = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user, "id", 1L);

            given(userQueryService.getUser(anyLong()))
                    .willReturn(user);

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);
            ReflectionTestUtils.setField(community, "isPrivate", true);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 1L);
            ReflectionTestUtils.setField(post, "community", community);

            given(postRepository.findById(anyLong()))
                    .willReturn(Optional.of(post));

            final Comment comment = TestEmptyEntityGenerator.Comment();
            ReflectionTestUtils.setField(comment, "id", 1L);
            ReflectionTestUtils.setField(comment, "post", post);

            given(commentRepository.findCommentById(anyLong()))
                    .willReturn(Optional.of(comment));

            given(communityQueryService.getCommunity(anyLong()))
                    .willReturn(community);

            doThrow(NotJoinedMemberException.class).when(memberValidationService).checkMemberJoinedCommunity(anyLong(), anyLong());

            CreateReport createCommentReport = new CreateReport(1L, ReportTarget.COMMENT, ReportReason.SWEAR, "신고");
            CreateReport createPostReport = new CreateReport(1L, ReportTarget.POST, ReportReason.SWEAR, "신고");

            assertThatThrownBy(() -> reportService.createReport(createCommentReport, 1L))
                    .isInstanceOf(NotJoinedMemberException.class);
            assertThatThrownBy(() -> reportService.createReport(createPostReport, 1L))
                    .isInstanceOf(NotJoinedMemberException.class);
        }

        @Test
        @DisplayName("글 신고 생성에 성공한다.")
        void createPostReportSuccess() {
            final User user = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user, "id", 1L);

            given(userQueryService.getUser(anyLong()))
                    .willReturn(user);

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);
            ReflectionTestUtils.setField(community, "isPrivate", false);

            final Post post = TestEmptyEntityGenerator.Post();
            ReflectionTestUtils.setField(post, "id", 1L);
            ReflectionTestUtils.setField(post, "community", community);

            given(postRepository.findById(anyLong()))
                    .willReturn(Optional.of(post));

            given(communityQueryService.getCommunity(anyLong()))
                    .willReturn(community);
            given(communityQueryService.getCommunity(anyLong()))
                    .willReturn(community);

            CreateReport createReport = new CreateReport(1L, ReportTarget.POST, ReportReason.SWEAR, "신고");
            reportService.createReport(createReport, 1L);

            verify(reportRepository, times(1)).save(any(Report.class));
        }

        @Test
        @DisplayName("커뮤니티 신고 생성에 성공한다.")
        void createCommunityReportSuccess() {
            final User user = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user, "id", 1L);

            given(userQueryService.getUser(anyLong()))
                    .willReturn(user);

            final Community community = TestEmptyEntityGenerator.Community();
            ReflectionTestUtils.setField(community, "id", 1L);

            given(communityRepository.findCommunityById(anyLong()))
                    .willReturn(Optional.of(community));

            CreateReport createReport = new CreateReport(1L, ReportTarget.COMMUNITY, ReportReason.SWEAR, "신고");
            reportService.createReport(createReport, 1L);

            verify(reportRepository, times(1)).save(any(Report.class));
        }

        @Test
        @DisplayName("쪽지 신고 생성에 성공한다.")
        void createMessageReportSuccess() {
            final User user1 = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user1, "id", 1L);
            given(userQueryService.getUser(anyLong()))
                    .willReturn(user1);

            final User user2 = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user2, "id", 2L);

            final Message message = TestEmptyEntityGenerator.Message();
            ReflectionTestUtils.setField(message, "id", 1L);
            ReflectionTestUtils.setField(message, "sender", user1);
            ReflectionTestUtils.setField(message, "receiver", user2);

            given(messageRepository.findById(anyLong()))
                    .willReturn(Optional.of(message));

            CreateReport createReport = new CreateReport(1L, ReportTarget.MESSAGE, ReportReason.SWEAR, "신고");
            reportService.createReport(createReport, 1L);

            verify(reportRepository, times(1)).save(any(Report.class));
        }

        @Test
        @DisplayName("본인이 포함되어 있는 쪽지가 아닐때 쪽지 신고시 InvalidValueException 발생한다.")
        void createMessageReportNotMyMessageFail() {
            final User user1 = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user1, "id", 1L);
            given(userQueryService.getUser(anyLong()))
                    .willReturn(user1);

            final User user2 = TestEmptyEntityGenerator.User();
            ReflectionTestUtils.setField(user2, "id", 2L);

            final Message message = TestEmptyEntityGenerator.Message();
            ReflectionTestUtils.setField(message, "id", 1L);
            ReflectionTestUtils.setField(message, "sender", user1);
            ReflectionTestUtils.setField(message, "receiver", user2);

            given(messageRepository.findById(anyLong()))
                    .willReturn(Optional.of(message));

            CreateReport createReport = new CreateReport(1L, ReportTarget.MESSAGE, ReportReason.SWEAR, "신고");
            assertThatThrownBy(() -> reportService.createReport(createReport, 3L))
                    .isInstanceOf(InvalidValueException.class);
        }
    }
}