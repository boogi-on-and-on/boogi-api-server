package boogi.apiserver.domain.user.domain;

import boogi.apiserver.domain.model.TimeBaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "USER")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends TimeBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Embedded
    private Email email;

    @Embedded
    private Username username;

    @Embedded
    private Department department;

    @Column(name = "tag_num")
    private TagNumber tagNumber;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Embedded
    private Introduce introduce;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private boolean messageNotAllowed;


    @Builder
    private User(Long id, String email, String username, String department, String tagNumber,
                 String profileImageUrl, String introduce, Role role, boolean messageNotAllowed) {
        this.id = id;
        this.email = new Email(email);
        this.username = new Username(username);
        this.department = new Department(department);
        this.tagNumber = new TagNumber(tagNumber);
        this.profileImageUrl = profileImageUrl;
        this.introduce = new Introduce(introduce);
        this.role = role;
        this.messageNotAllowed = messageNotAllowed;
    }

    public static User of(String email, String name, String department, int sameNameUserNum) {
        final String DEFAULT_INTRODUCE = "안녕하세요. 저는 " + name + " 입니다.";

        return User.builder()
                .email(email)
                .username(name)
                .department(department)
                .role(Role.USER)
                .tagNumber(makeTagNumber(sameNameUserNum))
                .introduce(DEFAULT_INTRODUCE)
                .build();
    }

    public static String makeTagNumber(int sameNameUserNum) {
        if (sameNameUserNum < 0 || sameNameUserNum >= 9999) {
            throw new IllegalArgumentException("동일 이름의 유저수는 0 ~ 9998 사이의 수여야 합니다.");
        }
        return String.format("#%04d", sameNameUserNum + 1);
    }

    public User update(String name, String department) {
        this.username = new Username(name);
        this.department = new Department(department);
        return this;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email.getValue();
    }

    public String getUsername() {
        return username.getValue();
    }

    public String getDepartment() {
        return department.getValue();
    }

    public String getTagNumber() {
        return tagNumber.getValue();
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getIntroduce() {
        return introduce.getValue();
    }

    public Role getRole() {
        return role;
    }

    public boolean isMessageNotAllowed() {
        return messageNotAllowed;
    }
}
