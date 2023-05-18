package boogi.apiserver.domain.message.message.controller;

import boogi.apiserver.domain.message.message.application.MessageCommand;
import boogi.apiserver.domain.message.message.application.MessageQuery;
import boogi.apiserver.domain.message.message.dto.request.SendMessageRequest;
import boogi.apiserver.domain.message.message.dto.response.MessageResponse;
import boogi.apiserver.domain.message.message.dto.response.MessageRoomResponse;
import boogi.apiserver.global.argument_resolver.session.Session;
import boogi.apiserver.global.dto.SimpleIdResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class MessageApiController {

    private final MessageCommand messageCommand;
    private final MessageQuery messageQuery;

    @PostMapping("/")
    public SimpleIdResponse sendMessage(@RequestBody @Validated SendMessageRequest request, @Session Long userId) {
        Long sendedMessageId = messageCommand.sendMessage(request, userId);

        return SimpleIdResponse.from(sendedMessageId);
    }

    @GetMapping("/")
    public MessageRoomResponse getMessageRooms(@Session Long userId) {
        return messageQuery.getMessageRooms(userId);
    }

    @GetMapping("/{opponentId}")
    public MessageResponse getMessages(@PathVariable Long opponentId, @Session Long userId, Pageable pageable) {
        return messageQuery.getMessagesByOpponentId(opponentId, userId, pageable);
    }
}
