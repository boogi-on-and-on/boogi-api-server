package boogi.apiserver.utils.fixture;

import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.utils.TestTimeReflection;

import java.time.LocalDateTime;

import static boogi.apiserver.utils.fixture.TimeFixture.STANDARD;

public enum UserFixture {

    SUNDO("sdcodebase@gmail.com", "김선도", "컴퓨터공학과", "#0001", "boogi.com/image/sd",
            "안녕하세요 저는 컴공과 김선도입니다", false, STANDARD),

    YONGJIN("yjlee0235@gmail.com", "이용진", "컴퓨터공학과", "#0001", "boogi.com/image/yj",
            "안녕하세요 저는 컴공과 이용진입니다", false, STANDARD.minusDays(1)),

    DEOKHWAN("tiger@gmail.com", "김덕환", "컴퓨터공학과", "#0001", "boogi.com/imgage/dh",
            "안녕하세요 저는 컴공과 김덕환입니다", false, STANDARD.minusDays(2)),
    ;

    public final String email;
    public final String username;
    public final String department;
    public final String tagNumber;
    public final String profileImage;
    public final String introduce;
    public final boolean messageNotAllowed;
    public final LocalDateTime createdAt;

    public static final Long SUNDO_ID = 1L;
    public static final Long YONGJIN_ID = 2L;
    public static final Long DEOKHWAN_ID = 3L;

    UserFixture(String email, String username, String department, String tagNumber, String profileImage, String introduce, boolean messageNotAllowed, LocalDateTime createdAt) {
        this.email = email;
        this.username = username;
        this.department = department;
        this.tagNumber = tagNumber;
        this.profileImage = profileImage;
        this.introduce = introduce;
        this.messageNotAllowed = messageNotAllowed;
        this.createdAt = createdAt;
    }

    public User toUser() {
        return toUser(null);
    }

    public User toUser(Long id) {
        User user = User.builder()
                .id(id)
                .username(username)
                .email(email)
                .department(department)
                .tagNumber(tagNumber)
                .profileImageUrl(profileImage)
                .introduce(introduce)
                .messageNotAllowed(messageNotAllowed)
                .build();
        TestTimeReflection.setCreatedAt(user, this.createdAt);
        return user;
    }
}
