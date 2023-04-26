package boogi.apiserver.domain.user.domain;

import boogi.apiserver.domain.model.TimeBaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
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

    private boolean messageNotAllowed;


    @Builder
    private User(Long id, String email, String username, String department, String tagNumber,
                 String profileImageUrl, String introduce, boolean messageNotAllowed) {
        this.id = id;
        this.email = new Email(email);
        this.username = new Username(username);
        this.department = new Department(department);
        this.tagNumber = new TagNumber(tagNumber);
        this.profileImageUrl = profileImageUrl;
        this.introduce = new Introduce(introduce);
        this.messageNotAllowed = messageNotAllowed;
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

    public boolean isMessageNotAllowed() {
        return messageNotAllowed;
    }
}
