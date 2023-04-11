package boogi.apiserver.domain.like.controller;

import boogi.apiserver.domain.like.application.LikeCommandService;
import boogi.apiserver.global.argument_resolver.session.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/likes")
public class LikeApiController {

    private final LikeCommandService likeCommandService;

    @DeleteMapping("/{likeId}")
    public void doUnlike(@PathVariable Long likeId, @Session Long userId) {
        likeCommandService.doUnlike(likeId, userId);
    }
}
