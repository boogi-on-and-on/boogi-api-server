package boogi.apiserver.domain.Installation.domain;

import boogi.apiserver.domain.model.TimeBaseEntity;
import boogi.apiserver.domain.user.domain.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name = "INSTALLATION")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class Installation extends TimeBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "installation_id")
    private Long id;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(name = "device_token")
    private String deviceToken;

    // deviceInfo : JSON

    // installationId ( UUID ) : header에 항상 넣어서 호출
}
