package org.example.messageservice.controler;

import org.example.messageservice.dto.ConversationDTO;
import org.example.messageservice.dto.MessageDTO;
import org.example.messageservice.dto.UserDTO;
import org.example.messageservice.model.Conversation;
import org.example.messageservice.model.Message;
import org.example.messageservice.service.MessageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    @Value("${user.service.url}")
    private String userServiceURL;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/test")
    public String test() {
        return "Message service is up and running!";
    }

    // Find inbox
    @GetMapping("/inbox")
    public ResponseEntity<List<ConversationDTO>> getConversationsByUserId(Authentication authentication) {
        String userId = authentication.getName();

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = extractToken(authentication);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<ConversationDTO> conversations = messageService.getInboxConversations(userId, token);
        return ResponseEntity.ok(conversations);
    }

    // Find messages by conversation id
    @GetMapping("/conversation/{conversationId}/messages")
    public ResponseEntity<List<MessageDTO>> getMessagesByConversationId(Authentication authentication,
            @PathVariable Long conversationId) {
        String token = extractToken(authentication);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<MessageDTO> messages = messageService.getConversationMessages(conversationId, token);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("reply/{conversationId}")
    public ResponseEntity<List<MessageDTO>> replyToConversation(Authentication authentication,
            @PathVariable Long conversationId, @RequestBody MessageDTO messageDTO) {
        String userId = authentication.getName();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = extractToken(authentication);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<MessageDTO> messages = messageService.replyToConversation(
                    conversationId,
                    userId,
                    messageDTO.getReceiverUsername(),
                    messageDTO.getMessage(),
                    token);
            return ResponseEntity.ok(messages);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/new")
    public ResponseEntity<MessageDTO> sendMessage(Authentication authentication, @RequestBody MessageDTO messageDTO) {

        String userId = authentication.getName();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = extractToken(authentication);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            MessageDTO createdMessage = messageService.createNewConversation(
                    userId,
                    messageDTO.getReceiverUsername(),
                    messageDTO.getMessage(),
                    token);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMessage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    private String extractToken(Authentication authentication) {
        if (authentication != null && authentication.getCredentials() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getCredentials();
            return jwt.getTokenValue();
        }
        return null;
    }
}
