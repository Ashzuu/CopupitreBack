package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.message.ConversationDTO;
import ashzuu.project.copupitreback.dto.message.MessageDTO;
import ashzuu.project.copupitreback.dto.message.SendMessageRequest;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import ashzuu.project.copupitreback.services.MessagingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST exposant la messagerie interne contextuelle (US-COM-03 & US-COM-04).
 */
@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessagingService messagingService;

    public MessageController(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDTO>> getConversations(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(messagingService.getUserConversations(userDetails));
    }

    @GetMapping("/{contextType}/{contextId}")
    public ResponseEntity<List<MessageDTO>> getMessages(
            @PathVariable String contextType,
            @PathVariable Long contextId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(messagingService.getMessages(contextType, contextId, userDetails));
    }

    @PostMapping("/{contextType}/{contextId}")
    public ResponseEntity<MessageDTO> sendMessage(
            @PathVariable String contextType,
            @PathVariable Long contextId,
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        MessageDTO message = messagingService.sendMessage(contextType, contextId, request.getContent(), userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }
}
