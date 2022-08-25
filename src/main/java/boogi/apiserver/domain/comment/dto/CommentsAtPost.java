package boogi.apiserver.domain.comment.dto;

import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.member.domain.MemberType;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.global.dto.PaginationDto;
import boogi.apiserver.global.util.time.TimePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
public class CommentsAtPost {

    private List<ParentCommentInfo> comments;

    private PaginationDto pageInfo;

    private CommentsAtPost(List<ParentCommentInfo> comments, Slice page) {
        this.comments = (comments == null) ? new ArrayList<>() : comments;
        this.pageInfo = PaginationDto.of(page);
    }

    public static CommentsAtPost of(List<ParentCommentInfo> comments, Slice page) {
        return new CommentsAtPost(comments, page);
    }

    @Getter
    @SuperBuilder
    public static class ParentCommentInfo extends BaseCommentInfo {

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private List<ChildCommentInfo> child;

        public static ParentCommentInfo toDto(Comment comment, Long likeId, boolean me, List<ChildCommentInfo> child, Long likeCount) {
            MemberInfo memberInfo;
            UserInfo userInfo;

            if (comment.getMember() == null) {
                memberInfo = null;
                userInfo = null;
            } else {
                Member member = comment.getMember();
                memberInfo = MemberInfo.toDto(member);
                userInfo = UserInfo.toDto(member.getUser());
            }

            return ParentCommentInfo.builder()
                    .id(comment.getId())
                    .user(userInfo)
                    .member(memberInfo)
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
    @SuperBuilder
    public static class ChildCommentInfo extends BaseCommentInfo {

        @JsonIgnore
        private Long parentId;

        public static ChildCommentInfo toDto(Comment comment, Long likeId, boolean me, Long parentId, Long likeCount) {
            return ChildCommentInfo.builder()
                    .id(comment.getId())
                    .user(UserInfo.toDto(comment.getMember().getUser()))
                    .member(MemberInfo.toDto(comment.getMember()))
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
    @SuperBuilder
    public abstract static class BaseCommentInfo {
        private Long id;

        private UserInfo user;

        private MemberInfo member;

        private Long likeId;

        @JsonFormat(pattern = TimePattern.BASIC_FORMAT_STRING)
        private LocalDateTime createdAt;

        private String content;

        private Long likeCount;

        private Boolean me;
    }

    @Getter
    @Builder
    public static class UserInfo {
        private Long id;

        private String name;

        private String tagNum;

        private String profileImageUrl;

        public static UserInfo toDto(User user) {
            return UserInfo.builder()
                    .id(user.getId())
                    .name(user.getUsername())
                    .tagNum(user.getTagNumber())
                    .profileImageUrl(user.getProfileImageUrl())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class MemberInfo {
        private Long id;

        private MemberType memberType;

        public static MemberInfo toDto(Member member) {
            return MemberInfo.builder()
                    .id(member.getId())
                    .memberType(member.getMemberType())
                    .build();
        }
    }
}
