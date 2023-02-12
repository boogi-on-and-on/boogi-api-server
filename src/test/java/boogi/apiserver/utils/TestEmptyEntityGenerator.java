package boogi.apiserver.utils;

import boogi.apiserver.domain.Installation.domain.Installation;
import boogi.apiserver.domain.alarm.alarm.domain.Alarm;
import boogi.apiserver.domain.alarm.alarmconfig.domain.AlarmConfig;
import boogi.apiserver.domain.comment.domain.Comment;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.joinrequest.domain.JoinRequest;
import boogi.apiserver.domain.hashtag.community.domain.CommunityHashtag;
import boogi.apiserver.domain.hashtag.post.domain.PostHashtag;
import boogi.apiserver.domain.like.domain.Like;
import boogi.apiserver.domain.member.domain.Member;
import boogi.apiserver.domain.message.block.domain.MessageBlock;
import boogi.apiserver.domain.message.delete.domain.MessageDelete;
import boogi.apiserver.domain.message.message.domain.Message;
import boogi.apiserver.domain.notice.domain.Notice;
import boogi.apiserver.domain.post.post.domain.Post;
import boogi.apiserver.domain.post.postmedia.domain.PostMedia;
import boogi.apiserver.domain.report.domain.Report;
import boogi.apiserver.domain.user.domain.User;

import java.lang.reflect.Constructor;

public class TestEmptyEntityGenerator {

    private static final String path = "boogi.apiserver.domain.";

    private static Object reflect(String classPath) {
        try {
            Constructor<?> constructor =
                    Class.forName(path + classPath)
                            .getDeclaredConstructor(null);
            constructor.setAccessible(true);
            return constructor.newInstance();

        } catch (Exception e) {
            String message = e.getMessage();
            throw new RuntimeException(message);
        }
    }

    public static Alarm Alarm() {
        return (Alarm) reflect("alarm.alarm.domain.Alarm");
    }

    public static AlarmConfig AlarmConfig() {
        return (AlarmConfig) reflect("alarm.alarmconfig.domain.AlarmConfig");
    }

    public static Comment Comment() {
        return (Comment) reflect("comment.domain.Comment");
    }

    public static Community Community() {
        return (Community) reflect("community.community.domain.Community");
    }

    public static JoinRequest JoinRequest() {
        return (JoinRequest) reflect("community.joinrequest.domain.JoinRequest");
    }

    public static CommunityHashtag CommunityHashtag() {
        return (CommunityHashtag) reflect("hashtag.community.domain.CommunityHashtag");
    }

    public static PostHashtag PostHashtag() {
        return (PostHashtag) reflect("hashtag.post.domain.PostHashtag");
    }

    public static Installation Installation() {
        return (Installation) reflect("installation.domain.Installation");
    }

    public static Like Like() {
        return (Like) reflect("like.domain.Like");
    }

    public static Member Member() {
        return (Member) reflect("member.domain.member");
    }

    public static MessageBlock MessageBlock() {
        return (MessageBlock) reflect("message.block.domain.MessageBlock");
    }

    public static MessageDelete MessageDelete() {
        return (MessageDelete) reflect("message.delete.domain.MessageDelete");
    }

    public static Message Message() {
        return (Message) reflect("message.message.domain.Message");
    }

    public static Notice Notice() {
        return (Notice) reflect("notice.domain.Notice");
    }

    public static Post Post() {
        return (Post) reflect("post.post.domain.Post");
    }

    public static PostMedia PostMedia() {
        return (PostMedia) reflect("post.postmedia.domain.PostMedia");
    }

    public static Report Report() {
        return (Report) reflect("report.domain.Report");
    }

    public static User User() {
        return (User) reflect("user.domain.User");
    }
}