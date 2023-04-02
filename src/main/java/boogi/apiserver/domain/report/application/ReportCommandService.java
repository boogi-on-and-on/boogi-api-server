package boogi.apiserver.domain.report.application;


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
import boogi.apiserver.domain.report.domain.ReportTarget;
import boogi.apiserver.domain.report.dto.request.CreateReportRequest;
import boogi.apiserver.domain.user.dao.UserRepository;
import boogi.apiserver.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportCommandService {

    private final CommunityRepository communityRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    private final MemberQueryService memberQueryService;

    public void createReport(CreateReportRequest request, Long userId) {
        User reportUser = userRepository.findByUserId(userId);

        Object targetObject = getReportTarget(userId, request.getTarget(), request.getId());
        Report newReport = Report.of(
                targetObject,
                reportUser,
                request.getContent(),
                request.getReason()
        );

        reportRepository.save(newReport);
    }

    private Object getReportTarget(final Long userId, final ReportTarget target, final Long id) {
        switch (target) {
            case COMMUNITY:
                return communityRepository.findCommunityById(id);
            case POST:
                return getReportedPost(userId, id);
            case COMMENT:
                return getReportedComment(userId, id);
            case MESSAGE:
                return getReportedMessage(userId, id);
        }
        throw new IllegalArgumentException();
    }

    private Post getReportedPost(Long userId, final Long id) {
        Post findPost = postRepository.findByPostId(id);

        Community community = findPost.getCommunity();
        if (community.isPrivate()) {
            memberQueryService.getMember(userId, community.getId());
        }
        return findPost;
    }

    private Comment getReportedComment(Long userId, final Long id) {
        Comment findComment = commentRepository.findCommentById(id);

        Community community = findComment.getPost().getCommunity();
        if (community.isPrivate()) {
            memberQueryService.getMember(userId, community.getId());
        }
        return findComment;
    }

    private Message getReportedMessage(Long userId, Long id) {
        Message findMessage = messageRepository.findMessageById(id);

        if (!findMessage.isMyMessage(userId)) {
            throw new NotParticipatedUserException();
        }
        return findMessage;
    }
}
