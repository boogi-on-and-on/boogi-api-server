package boogi.apiserver.domain.alarm.alarmconfig.domain;

import boogi.apiserver.builder.TestAlarmConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

class AlarmConfigTest {

    @DisplayName("알람 설정 변경 테스트")
    static class SwitchAlarmConfigTest {

        static Stream<Arguments> switchAlarmConfig() {
            return Stream.of(
                    Arguments.of(true, true),
                    Arguments.of(null, false),
                    Arguments.of(false, false)
            );
        }

        @DisplayName("메세지 수신 알람 스위치")
        @MethodSource("switchAlarmConfig")
        @ParameterizedTest(name = "{0}로 메시지 알람 변경 시 {1}로 변경")
        void message(Boolean state, Boolean result) {
            final AlarmConfig alarmConfig = TestAlarmConfig.builder().build();
            alarmConfig.switchMessage(state);
            assertThat(alarmConfig.getMessage()).isEqualTo(result);
        }

        @DisplayName("공지사항 수신 알람 스위치")
        @MethodSource("switchAlarmConfig")
        @ParameterizedTest(name = "{0}로 공지사항 알람 변경 시 {1}로 변경")
        void notice(Boolean state, Boolean result) {
            final AlarmConfig alarmConfig = TestAlarmConfig.builder().build();
            alarmConfig.switchNotice(state);
            assertThat(alarmConfig.getNotice()).isEqualTo(result);
        }

        @DisplayName("가입 요청 수신 알람 스위치")
        @MethodSource("switchAlarmConfig")
        @ParameterizedTest(name = "{0}로 가입요청 알람 변경 시 {1}로 변경")
        void joinRequest(Boolean state, Boolean result) {
            final AlarmConfig alarmConfig = TestAlarmConfig.builder().build();
            alarmConfig.switchJoinRequest(state);
            assertThat(alarmConfig.getJoinRequest()).isEqualTo(result);
        }

        @DisplayName("댓글 요청 수신 알람 스위치")
        @MethodSource("switchAlarmConfig")
        @ParameterizedTest(name = "{0}로 댓글 알람 상태 변경 시 {1}로 변경")
        void comment(Boolean state, Boolean result) {
            final AlarmConfig alarmConfig = TestAlarmConfig.builder().build();
            alarmConfig.switchComment(state);
            assertThat(alarmConfig.getComment()).isEqualTo(result);
        }

        @DisplayName("맨션 수신 알람 스위치")
        @MethodSource("switchAlarmConfig")
        @ParameterizedTest(name = "{0}로 맨션 알람 변경 시 {1}로 변경")
        void mention(Boolean state, Boolean result) {
            final AlarmConfig alarmConfig = TestAlarmConfig.builder().build();
            alarmConfig.switchMention(state);
            assertThat(alarmConfig.getMention()).isEqualTo(result);
        }
    }
}