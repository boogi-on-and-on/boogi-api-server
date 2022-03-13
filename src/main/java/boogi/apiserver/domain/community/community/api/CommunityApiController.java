package boogi.apiserver.domain.community.community.api;

import boogi.apiserver.domain.community.community.application.CommunityCoreService;
import boogi.apiserver.domain.community.community.domain.Community;
import boogi.apiserver.domain.community.community.dto.CreateCommunityRequest;
import boogi.apiserver.global.argument_resolver.session.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/communities")
public class CommunityApiController {

    private final CommunityCoreService communityCoreService;

    @PostMapping
    public ResponseEntity<Object> createCommunity(@RequestBody @Validated CreateCommunityRequest request, @Session Long userId) {
        Community community = Community.of(request.getName(), request.getDescription(), request.isPrivate(), request.isAutoApproval());
        Long communityId = communityCoreService.createCommunity(community, request.getHashtags(), userId).getId();

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "communityId", communityId
        ));
    }
}
