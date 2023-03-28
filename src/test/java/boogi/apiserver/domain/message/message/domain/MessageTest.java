package boogi.apiserver.domain.message.message.domain;

import boogi.apiserver.builder.TestMessage;
import boogi.apiserver.builder.TestUser;
import boogi.apiserver.domain.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class MessageTest {

    @Nested
    @DisplayName("송수신 유저 중 내가 포함되었는지 판별")
    class IsMyMessage {
        private static final long MY_USER_ID = 1l;
        private static final long NOT_MY_USER_ID1 = 2l;
        private static final long NOT_MY_USER_ID2 = 3l;

        @Test
        @DisplayName("내가 송신 유저인 경우 true를 반환한다.")
        void senderSameSuccess() {
            User myUser = TestUser.builder().id(MY_USER_ID).build();
            User receiver = TestUser.builder().id(NOT_MY_USER_ID1).build();

            Message message = TestMessage.builder().sender(myUser).receiver(receiver).build();

            assertThat(message.isMyMessage(MY_USER_ID)).isTrue();
        }

        @Test
        @DisplayName("내가 수신 유저인 경우 true를 반환한다.")
        void receiverSameSuccess() {
            User myUser = TestUser.builder().id(MY_USER_ID).build();
            User sender = TestUser.builder().id(NOT_MY_USER_ID1).build();

            Message message = TestMessage.builder().sender(sender).receiver(myUser).build();

            assertThat(message.isMyMessage(MY_USER_ID)).isTrue();
        }

        @Test
        @DisplayName("내가 송수신 유저가 아닌 경우 false를 반환한다.")
        void notSameFail() {
            User sender = TestUser.builder().id(NOT_MY_USER_ID1).build();
            User receiver = TestUser.builder().id(NOT_MY_USER_ID2).build();

            Message message = TestMessage.builder().sender(sender).receiver(receiver).build();

            assertThat(message.isMyMessage(MY_USER_ID)).isFalse();
        }
    }
}