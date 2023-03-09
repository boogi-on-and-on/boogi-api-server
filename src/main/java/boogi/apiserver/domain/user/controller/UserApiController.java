package boogi.apiserver.domain.user.controller;

import boogi.apiserver.domain.alarm.alarmconfig.application.AlarmConfigCommandService;
import boogi.apiserver.domain.alarm.alarmconfig.domain.AlarmConfig;
import boogi.apiserver.domain.alarm.alarmconfig.dto.request.AlarmConfigSettingRequest;
import boogi.apiserver.domain.community.community.application.CommunityCommandService;
import boogi.apiserver.domain.community.community.application.CommunityQueryService;
import boogi.apiserver.domain.community.community.dto.dto.JoinedCommunitiesDto;
import boogi.apiserver.domain.message.block.application.MessageBlockQueryService;
import boogi.apiserver.domain.message.block.application.MessageBlockCommandService;
import boogi.apiserver.domain.message.block.dto.dto.MessageBlockedUserDto;
import boogi.apiserver.domain.user.application.UserQueryService;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.request.BlockMessageUsersRequest;
import boogi.apiserver.domain.user.dto.request.BlockedUserIdRequest;
import boogi.apiserver.domain.user.dto.response.AlarmConfigSettingInfoResponse;
import boogi.apiserver.domain.user.dto.response.MessageBlockedUsesResponse;
import boogi.apiserver.domain.user.dto.response.UserDetailInfoDto;
import boogi.apiserver.domain.user.dto.response.UserProfileDetailResponse;
import boogi.apiserver.global.argument_resolver.session.Session;
import boogi.apiserver.global.constant.SessionInfoConst;
import boogi.apiserver.global.dto.ValidStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/users")
public class UserApiController {
    private final UserQueryService userQueryService;
    private final MessageBlockQueryService messageBlockQueryService;
    private final CommunityQueryService communityQueryService;

    private final MessageBlockCommandService messageBlockCommandService;
    private final AlarmConfigCommandService alarmConfigCommandService;

    @PostMapping("/token/{email}")
    public void issueToken(HttpServletRequest request, @PathVariable String email) {
        User user = userQueryService.getUserByEmail(email);

        HttpSession preSession = request.getSession(false);
        if (preSession != null) {
            preSession.invalidate();
        }

        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(60 * 60 * 24 * 30 * 3); // 3개월
        session.setAttribute(SessionInfoConst.USER_ID, user.getId());
    }

    @PostMapping("/token/validation")
    public ValidStatusResponse validateToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        Boolean isValid = Objects.nonNull(session);

        return ValidStatusResponse.from(isValid);
    }

    @GetMapping
    public UserProfileDetailResponse getUserProfileInfo(@RequestParam(required = false) Long userId, @Session Long sessionUserId) {
        Long id = Objects.requireNonNullElse(userId, sessionUserId);
        UserDetailInfoDto userDetailDto = userQueryService.getUserDetailInfo(id);

        return UserProfileDetailResponse.of(userDetailDto, sessionUserId);
    }

    @GetMapping("/communities/joined")
    public JoinedCommunitiesDto getUserJoinedCommunitiesInfo(@Session Long userId) {
        return communityQueryService.getJoinedCommunitiesWithLatestPost(userId);
    }

    @GetMapping("/messages/blocked")
    public MessageBlockedUsesResponse getBlockedUsers(@Session Long userId) {
        List<MessageBlockedUserDto> blockedUserDtos = messageBlockQueryService.getBlockedMembers(userId);

        return MessageBlockedUsesResponse.from(blockedUserDtos);
    }

    @PostMapping("/messages/unblock")
    public void releaseUser(@Session Long userId, @RequestBody BlockedUserIdRequest request) {
        messageBlockCommandService.unblockUser(userId, request.getBlockerUserId());
    }

    @PostMapping("/messages/block")
    public void blockUsers(@Session Long userId, @Validated @RequestBody BlockMessageUsersRequest request) {
        messageBlockCommandService.blockUsers(userId, request.getBlockUserIds());
    }

    @GetMapping("/config/notifications")
    public AlarmConfigSettingInfoResponse getAlarmConfig(@Session Long userId) {
        AlarmConfig alarmConfig = alarmConfigCommandService.findOrElseCreateAlarmConfig(userId);

        return AlarmConfigSettingInfoResponse.from(alarmConfig);
    }

    @PostMapping("/config/notifications")
    public AlarmConfigSettingInfoResponse configureAlarm(@Session Long userId, @RequestBody @Validated AlarmConfigSettingRequest request) {
        AlarmConfig alarmConfig = alarmConfigCommandService.configureAlarm(userId, request);

        return AlarmConfigSettingInfoResponse.from(alarmConfig);
    }
}
