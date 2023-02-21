package boogi.apiserver.domain.comment.dto.response;

import boogi.apiserver.domain.comment.domain.Comment;
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
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
public class CommentsAtPostResponse {

    private List<ParentCommentInfo> comments;

    private PaginationDto pageInfo;

    public CommentsAtPostResponse(List<ParentCommentInfo> comments, PaginationDto pageInfo) {
        this.comments = comments;
        this.pageInfo = pageInfo;
    }

    public static CommentsAtPostResponse of(List<ParentCommentInfo> comments, Slice page) {
        List<ParentCommentInfo> commentInfos = (comments == null) ? new ArrayList<>() : comments;
        PaginationDto pageInfo = PaginationDto.of(page);
        return new CommentsAtPostResponse(commentInfos, pageInfo);
    }

    @Getter
    public static class ParentCommentInfo extends BaseCommentInfo {

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private List<ChildCommentInfo> child;

        @Builder(access = AccessLevel.PRIVATE)
        public ParentCommentInfo(Long id, UserBasicProfileDto user, MemberInfo member,
                                 Long likeId, LocalDateTime createdAt, String content,
                                 Long likeCount, Boolean me, List<ChildCommentInfo> child) {
            super(id, user, member, likeId, createdAt, content, likeCount, me);
            this.child = child;
        }

        public static ParentCommentInfo of(Comment comment, Long likeId, boolean me,
                                           List<ChildCommentInfo> child, Long likeCount) {
            Member member = comment.getMember();

            return ParentCommentInfo.builder()
                    .id(comment.getId())
                    .user(UserBasicProfileDto.of(member))
                    .member(MemberInfo.from(member))
                    .likeId(likeId)
                    .createdAt(comment.getCreatedAt())
                    .content(comment.getContent())
                    .likeCount(likeCount)
                    .me(me)
                    .child(child)
                    .build();
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

        public static ChildCommentInfo of(Comment comment, Long likeId, boolean me, Long parentId, Long likeCount) {
            Member member = comment.getMember();

            return ChildCommentInfo.builder()
                    .id(comment.getId())
                    .user(UserBasicProfileDto.of(member.getUser()))
                    .member(MemberInfo.from(member))
                    .likeId(likeId)
                    .createdAt(comment.getCreatedAt())
                    .content(comment.getContent())
                    .likeCount(likeCount)
                    .me(me)
                    .parentId(parentId)
                    .build();
        }
    }

    @Getter
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
    }

    @Getter
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
