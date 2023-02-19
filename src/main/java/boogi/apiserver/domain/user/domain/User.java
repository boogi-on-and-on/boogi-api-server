package boogi.apiserver.domain.user.domain;

import boogi.apiserver.domain.model.TimeBaseEntity;
import lombok.AccessLevel;
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

    private boolean messageNotAllowed;

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
