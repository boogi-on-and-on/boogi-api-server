package boogi.apiserver.domain.like.api;

import boogi.apiserver.domain.like.application.LikeCoreService;
import boogi.apiserver.global.argument_resolver.session.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/likes")
public class LikeApiController {

    private final LikeCoreService likeCoreService;

    @DeleteMapping("/{likeId}")
    public ResponseEntity<Void> doUnlike(@PathVariable Long likeId, @Session Long userId) {
        likeCoreService.doUnlike(likeId, userId);

        return ResponseEntity.ok().build();
    }
}
