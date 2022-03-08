package boogi.apiserver.domain.member.api;

import boogi.apiserver.domain.member.exception.AlreadyBlockedMemberException;
import boogi.apiserver.global.argument_resolver.session.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/members")
public class MemberApiController {

    @GetMapping("/{memberId}")
    public String test(@PathVariable String memberId, @Session Long userId) {
        log.info("userId = {}", userId);
        log.info("memberId = {}", memberId);

        if (memberId.equals("1")) {
            // 해당 계정은 이미 차단된 고객입니다.
            throw new AlreadyBlockedMemberException();
        }
        if(memberId.equals("2")){
            throw new AlreadyBlockedMemberException("특정 문자가 있습니다.");
        }

        return "hello";
    }
}
