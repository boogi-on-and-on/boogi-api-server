package boogi.apiserver.global.configuration;


import boogi.apiserver.domain.auth.service.GoogleOAuth2UserService;
import boogi.apiserver.domain.user.domain.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final GoogleOAuth2UserService googleOAuth2UserService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/api/**").hasRole(Role.USER.name())
                .anyRequest().authenticated()
                .and()
                .logout()
                .and()
                .oauth2Login()
                .userInfoEndpoint()
                .userService(googleOAuth2UserService);
    }
}
