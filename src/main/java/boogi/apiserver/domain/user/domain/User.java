package boogi.apiserver.domain.user.domain;

import boogi.apiserver.domain.Installation.domain.Installation;
import boogi.apiserver.domain.model.TimeBaseEntity;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "USER")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User extends TimeBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String email;

    private String username;

    private String department;

    @Column(name = "tag_num")
    private String tagNumber;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    private String introduce;

    private boolean messageNotAllowed;

    @OneToMany(mappedBy = "user")
    private List<Installation> installations = new ArrayList<>();
}
