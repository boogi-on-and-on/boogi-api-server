package boogi.apiserver.domain.comment.dto.response;

import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.user.dto.dto.UserBasicProfileDto;
import boogi.apiserver.global.dto.PaginationDto;
import boogi.apiserver.global.util.time.TimePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentsAtPostResponse {

    private List<ParentCommentInfo> comments;

    private PaginationDto pageInfo;

    public CommentsAtPostResponse(List<ParentCommentInfo> comments, PaginationDto pageInfo) {
        this.comments = comments;
        this.pageInfo = pageInfo;
    }

    public static CommentsAtPostResponse of(Map<Long, Like> sessionMemberCommentLikeMap,
                                            Map<Long, Long> commentLikeCountMap,
                                            List<Comment> childComments,
                                            Slice<Comment> commentPage,
                                            Long sessionMemberId) {

        List<Comment> parentComments = commentPage.getContent();

        Map<Long, List<ChildCommentInfo>> childCommentInfos = childComments.stream()
                .map(c -> ChildCommentInfo.of(c, sessionMemberId, sessionMemberCommentLikeMap, commentLikeCountMap))
                .collect(Collectors.groupingBy(
                        ChildCommentInfo::getParentId,
                        HashMap::new,
                        Collectors.toCollection(ArrayList::new)));

        List<ParentCommentInfo> commentInfos = parentComments.stream()
                .map(c -> ParentCommentInfo.of(
                        sessionMemberId,
                        sessionMemberCommentLikeMap,
                        c,
                        commentLikeCountMap,
                        childCommentInfos)
                ).collect(Collectors.toList());

        PaginationDto pageInfo = PaginationDto.of(commentPage);
        return new CommentsAtPostResponse(commentInfos, pageInfo);
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ParentCommentInfo extends BaseCommentInfo {

        private static final String DELETED_COMMENT_CONTENT = "삭제된 댓글입니다";

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private List<ChildCommentInfo> child;

        @Builder(access = AccessLevel.PRIVATE)
        public ParentCommentInfo(Long id, UserBasicProfileDto user, MemberInfo member,
                                 Long likeId, LocalDateTime createdAt, String content,
                                 Long likeCount, Boolean me, List<ChildCommentInfo> child) {
            super(id, user, member, likeId, createdAt, content, likeCount, me);
            this.child = child;
        }

        public static ParentCommentInfo of(Long sessionUserId,
                                           Map<Long, Like> commentLikes,
                                           Comment comment,
                                           Map<Long, Long> commentLikeCountMap,
                                           Map<Long, List<ChildCommentInfo>> childCommentInfos) {
            Member member = comment.getMember();
            Long likeCount = commentLikeCountMap.getOrDefault(comment.getId(), 0L);

            ParentCommentInfoBuilder commentBuilder = ParentCommentInfo.builder()
                    .id(comment.getId())
                    .user(UserBasicProfileDto.from(member))
                    .member(MemberInfo.from(member))
                    .likeId(getLikeId(commentLikes, comment.getId()))
                    .createdAt(comment.getCreatedAt())
                    .content(comment.getContent())
                    .likeCount(likeCount)
                    .me(getMe(sessionUserId, member))
                    .child(childCommentInfos.get(comment.getId()));

            if (comment.getDeletedAt() != null) {
                commentBuilder.content(DELETED_COMMENT_CONTENT);
            }

            return commentBuilder.build();
        }
    }

    @Getter
    public static class ChildCommentInfo extends BaseCommentInfo {

        @JsonIgnore
        private Long parentId;

        @Builder(access = AccessLevel.PRIVATE)
        public ChildCommentInfo(Long id, UserBasicProfileDto user, MemberInfo member,
                                Long likeId, LocalDateTime createdAt, String content,
                                Long likeCount, Boolean me, Long parentId) {
            super(id, user, member, likeId, createdAt, content, likeCount, me);
            this.parentId = parentId;
        }

        public static ChildCommentInfo of(Comment comment,
                                          Long joinedMemberId,
                                          Map<Long, Like> commentLikes,
                                          Map<Long, Long> commentLikeCountMap) {
            Member member = comment.getMember();
            Long likeCount = commentLikeCountMap.getOrDefault(comment.getId(), 0L);

            return ChildCommentInfo.builder()
                    .id(comment.getId())
                    .user(UserBasicProfileDto.from(member.getUser()))
                    .member(MemberInfo.from(member))
                    .likeId(getLikeId(commentLikes, comment.getId()))
                    .createdAt(comment.getCreatedAt())
                    .content(comment.getContent())
                    .likeCount(likeCount)
                    .me(getMe(joinedMemberId, member))
                    .parentId(comment.getParent().getId())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public abstract static class BaseCommentInfo {
        private Long id;

        private UserBasicProfileDto user;

        private MemberInfo member;

        private Long likeId;

        @JsonFormat(pattern = TimePattern.BASIC_FORMAT_STRING)
        private LocalDateTime createdAt;

        private String content;

        private Long likeCount;

        private Boolean me;

        public BaseCommentInfo(Long id, UserBasicProfileDto user, MemberInfo member,
                               Long likeId, LocalDateTime createdAt, String content,
                               Long likeCount, Boolean me) {
            this.id = id;
            this.user = user;
            this.member = member;
            this.likeId = likeId;
            this.createdAt = createdAt;
            this.content = content;
            this.likeCount = likeCount;
            this.me = me;
        }

        protected static Long getLikeId(Map<Long, Like> commentLikes, Long commentId) {
            Like like = commentLikes.get(commentId);
            return (like == null) ? null : like.getId();
        }

        protected static Boolean getMe(Long joinedMemberId, Member commentedMember) {
            return Boolean.valueOf(commentedMember.getId().equals(joinedMemberId));
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class MemberInfo {
        private Long id;

        private MemberType memberType;

        public MemberInfo(Long id, MemberType memberType) {
            this.id = id;
            this.memberType = memberType;
        }

        public static MemberInfo from(Member member) {
            if (member == null) {
                return null;
            }
            return new MemberInfo(member.getId(), member.getMemberType());
        }
    }
}
