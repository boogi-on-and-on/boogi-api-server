package boogi.apiserver.domain.message.message.api;

import boogi.apiserver.domain.message.message.application.MessageService;
import boogi.apiserver.domain.message.message.dto.request.SendMessage;
import boogi.apiserver.domain.message.message.dto.response.MessageResponse;
import boogi.apiserver.domain.message.message.dto.response.MessageRoomResponse;
import boogi.apiserver.global.argument_resolver.session.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class MessageApiController {

    private final MessageService messageService;

    @PostMapping("/")
    public ResponseEntity<Object> sendMessage(@RequestBody SendMessage sendMessage, @Session Long userId) {
        Long sendedMessageId = messageService.sendMessage(sendMessage, userId).getId();

        return ResponseEntity.ok().body(Map.of(
                "id", sendedMessageId
        ));
    }

    @GetMapping("/")
    public ResponseEntity<MessageRoomResponse> getMessageRooms(@Session Long userId) {
        MessageRoomResponse messageRooms = messageService.getMessageRooms(userId);

        return ResponseEntity.ok().body(messageRooms);
    }

    @GetMapping("/{opponentId}")
    public ResponseEntity<MessageResponse> getMessages(@PathVariable Long opponentId, @Session Long userId, Pageable pageable) {
        MessageResponse messageResponse = messageService.getMessagesByOpponentId(opponentId, userId, pageable);

        return ResponseEntity.ok().body(messageResponse);
    }
}
