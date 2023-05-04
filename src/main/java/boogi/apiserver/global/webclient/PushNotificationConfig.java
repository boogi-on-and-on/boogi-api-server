package boogi.apiserver.global.webclient;

import boogi.apiserver.global.webclient.push.AsyncHttpInvocationPushNotification;
import boogi.apiserver.global.webclient.push.DummyPushNotification;
import boogi.apiserver.global.webclient.push.SendPushNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Slf4j
public class PushNotificationConfig {

    @Bean
    @Profile({"dev", "local", "prod"})
    public SendPushNotification asyncHttpInvocationPushNotification() {
        log.info("create bean AsyncHttpInvocationPushNotification");
        return new AsyncHttpInvocationPushNotification();
    }

    @Bean
    @Profile("test")
    public SendPushNotification dummyPushNotification() {
        log.info("create bean DummyPushNotification");
        return new DummyPushNotification();
    }
}
