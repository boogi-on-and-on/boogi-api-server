package boogi.apiserver.global.webclient;

import boogi.apiserver.global.webclient.push.SendPushNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class HttpInvocation {

    @Autowired
    public SendPushNotification sendPushNotification;


}
