package boogi.apiserver.global.webclient;

import boogi.apiserver.global.webclient.push.AsyncHttpInvocationPushNotification;
import boogi.apiserver.global.webclient.push.SendPushNotification;
import boogi.apiserver.global.webclient.push.SyncHttpInvocationPushNotification;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebClientConfig {

    @Bean
    public SendPushNotification sendPushNotification() {
        return new SyncHttpInvocationPushNotification();
    }

}
