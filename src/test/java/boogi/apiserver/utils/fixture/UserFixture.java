package boogi.apiserver.utils.fixture;

import boogi.apiserver.domain.user.domain.User;

public enum UserFixture {

    SUNDO("sdcodebase@gmail.com","김선도", "컴퓨터공학과", "#0001", "boogi.com/image/sd",
            "안녕하세요 저는 컴공과 김선도입니다", false),

    YONGJIN("yjlee0235@gmail.com", "이용진", "컴퓨터공학과", "#0001", "boogi.com/image/yj",
            "안녕하세요 저는 컴공과 이용진입니다", false),

    DEOKHWAN("tiger@gmail.com", "김덕환", "컴퓨터공학과", "#0001", "boogi.com/imgage/dh",
            "안녕하세요 저는 컴공과 김덕환입니다", false),
    ;

    public final String email;
    public final String username;
    public final String department;
    public final String tagNumber;
    public final String profileImage;
    public final String introduce;
    public final boolean messageNotAllowed;

    public static final Long SUNDO_ID = 1L;
    public static final Long YONGJIN_ID = 2L;
    public static final Long DEOKHWAN_ID = 3L;

    UserFixture(String email, String username, String department, String tagNumber, String profileImage, String introduce, boolean messageNotAllowed) {
        this.email = email;
        this.username = username;
        this.department = department;
        this.tagNumber = tagNumber;
        this.profileImage = profileImage;
        this.introduce = introduce;
        this.messageNotAllowed = messageNotAllowed;
    }

    public User toUser() {
        return toUser(null);
    }

    public User toUser(Long id) {
        return User.builder()
                .id(id)
                .username(username)
                .email(email)
                .department(department)
                .tagNumber(tagNumber)
                .profileImageUrl(profileImage)
                .introduce(introduce)
                .messageNotAllowed(messageNotAllowed)
                .build();
    }
}
