package boogi.apiserver.domain.user.controller;

import boogi.apiserver.domain.member.application.MemberQueryService;
import boogi.apiserver.domain.post.post.application.PostQueryService;
import boogi.apiserver.domain.post.post.dto.LatestPostOfUserJoinedCommunity;
import boogi.apiserver.domain.user.application.UserQueryService;
import boogi.apiserver.domain.user.application.UserValidationService;
import boogi.apiserver.domain.user.domain.User;
import boogi.apiserver.domain.user.dto.UserDetailInfoResponse;
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

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/users")
public class UserApiController {
    private final UserQueryService userQueryService;
    private final MemberQueryService memberQueryService;
    private final PostQueryService postQueryService;

    private final UserValidationService userValidationService;

    @PostMapping("/token/{email}")
    public ResponseEntity<Object> issueToken(HttpServletRequest request, @PathVariable String email) {
        User user = userValidationService.getUserByEmail(email);

        HttpSession preSession = request.getSession(false);
        if (preSession != null) {
            preSession.invalidate();
        }

        HttpSession session = request.getSession();
        session.setAttribute(SessionInfoConst.USER_ID, user.getId());

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUserProfileInfo(
            @PathVariable Long userId, @Session Long sessionUserId) {
        UserDetailInfoResponse userDetailDto = userQueryService.getUserDetailInfo(userId);
        Boolean me = userId.equals(sessionUserId);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "user", userDetailDto,
                "me", me
        ));
    }

    @GetMapping("/communities/joined")
    public ResponseEntity<Object> getUserJoinedCommunitiesInfo(@Session Long userId) {
        List<LatestPostOfUserJoinedCommunity> communities = postQueryService.getPostsOfUserJoinedCommunity(userId);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "communities", communities
        ));
    }
}
