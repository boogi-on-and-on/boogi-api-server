package boogi.apiserver.domain.user.controller;

import boogi.apiserver.domain.member.application.MemberCoreService;
import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.user.application.UserCoreService;
import boogi.apiserver.domain.user.application.UserQueryService;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.UserDetailInfoResponse;
import boogi.apiserver.domain.user.dto.UserJoinedCommunity;
import boogi.apiserver.global.argument_resolver.session.Session;
import boogi.apiserver.global.constant.SessionInfoConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/users")
public class UserApiController {
    private final UserQueryService userQueryService;
    private final MemberQueryService memberQueryService;

    @PostMapping("/token/{email}")
    public ResponseEntity<Object> issueToken(HttpServletRequest request, @PathVariable String email) {
        // TODO: email 기반으로 User 찾고 그에 대한 userId로 세션만들어주기

        HttpSession preSession = request.getSession(false);
        if (preSession != null) {
            preSession.invalidate();
        }

        HttpSession session = request.getSession();
        session.setAttribute(SessionInfoConst.USER_ID, 1L);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDetailInfoResponse> getUserProfileInfo(
            @PathVariable Long userId, @Session Long sessionUserId) {
        UserDetailInfoResponse userDetailDto = userQueryService.getUserDetailInfo(userId);
        return ResponseEntity.status(HttpStatus.OK).body(userDetailDto);
    }

    @GetMapping("/communities/joined")
    public ResponseEntity<Object> getUserJoinedCommunitiesInfo(@Session Long userId) {
        List<UserJoinedCommunity> communities = memberQueryService.getJoinedMemberInfo(userId);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "communities", communities
        ));
    }
}
