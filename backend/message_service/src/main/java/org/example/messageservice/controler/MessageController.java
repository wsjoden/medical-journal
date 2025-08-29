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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    private final WebClient.Builder webClientBuilder;

    public MessageController(MessageService messageService, WebClient.Builder webClientBuilder) {
        this.messageService = messageService;
        this.webClientBuilder = webClientBuilder;
    }

    @GetMapping("/test")
    public String test() {
        System.out.println("Message service is up and running!");

        System.out.println("trying to reach jwt service...");
        String response = this.webClientBuilder.build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("jwt-service")
                        .port(8081)
                        .path("/jwt/test")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return "Message service is up and running! JWT service response: " + response;
    }

    // Find inbox
    @GetMapping("/inbox")
    public ResponseEntity<List<ConversationDTO>> getConversationsByUserId(Authentication authentication) {
        System.out.println("someone is trying to open their inbox");

        String userId = authentication.getName();

        System.out.println("userId: " + userId);

        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String token = null;
        if (authentication != null && authentication.getCredentials() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getCredentials();
            token = jwt.getTokenValue();
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        System.out.println("after token stuff");

        List<Conversation> conversations = messageService.findConversationsByUserId(userId);
        List<ConversationDTO> conversationsDTO = new ArrayList<>();

        System.out.println("lists were created");

        for (Conversation c : conversations) {
            long conversationId = c.getId();
            List<String> messages = c.getMessages().stream()
                    .map(Message::getMessage)
                    .collect(Collectors.toList());

            String participantId;
            if (c.getUser1Id().equals(userId)) {
                participantId = c.getUser2Id();
            } else {
                participantId = c.getUser1Id();
            }
            System.out.println("participantId: " + participantId);
            String participantUsername = GetUserById(participantId, token).getUsername();

            ConversationDTO conversationDTO = new ConversationDTO(conversationId, c.getUser1Id(), participantUsername,
                    c.getUser2Id(), messages);
            conversationsDTO.add(conversationDTO);
        }

        return ResponseEntity.ok(conversationsDTO);
    }

    // Find messages by conversation id
    @GetMapping("/conversation/{conversationId}/messages")
    public ResponseEntity<List<MessageDTO>> getMessagesByConversationId(Authentication authentication,
            @PathVariable Long conversationId) {
        System.out.println("Fetching messages for conversationId: " + conversationId);

        final String token;
        ;
        if (authentication != null && authentication.getCredentials() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getCredentials();
            token = jwt.getTokenValue();
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<Message> messages = messageService.findMessagesByConversationId(conversationId);

        List<MessageDTO> messageDTOList = messages.stream()
                .map(message -> convertToMessageDTO(message, token))
                .collect(Collectors.toList());
        return ResponseEntity.ok(messageDTOList);
    }

    @PostMapping("reply/{conversationId}")
    public ResponseEntity<List<MessageDTO>> replyToConversation(Authentication authentication,
            @PathVariable Long conversationId, @RequestBody MessageDTO messageDTO) {
        System.out.println("replyToConversation called!");

        String userId = authentication.getName();
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String token = null;
        if (authentication != null && authentication.getCredentials() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getCredentials();
            token = jwt.getTokenValue();
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String receiverId = GetUserByUsername(messageDTO.getReceiverUsername(), token);
        Conversation conversation = messageService.findConversationById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        Message message = new Message();
        message.setSenderUserId(userId);
        message.setReceiverUserId(receiverId);
        message.setConversation(conversation);
        message.setMessage(messageDTO.getMessage());
        message.setSentDate(LocalDate.now());

        messageService.sendMessage(message);

        List<Message> messages = messageService.findMessagesByConversationId(conversationId);
        List<MessageDTO> messageDTOList = new ArrayList<>();
        for (Message m : messages) {
            messageDTOList.add(convertToMessageDTO(m, token));
        }
        return ResponseEntity.ok(messageDTOList);
    }

    @PostMapping("/new")
    public ResponseEntity<MessageDTO> sendMessage(Authentication authentication, @RequestBody MessageDTO messageDTO) {
        System.out.println("sendMessage called!!!");
        System.out.println("received messageDTO: " + messageDTO.toString());

        String userId = authentication.getName();
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String token = null;
        if (authentication != null && authentication.getCredentials() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getCredentials();
            token = jwt.getTokenValue();
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        System.out.println("fetching receiver...");

        String receiverId = GetUserByUsername(messageDTO.getReceiverUsername(), token);
        if (receiverId == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        System.out.println("receiverId: " + receiverId);

        Conversation conversation = new Conversation();
        conversation.setUser1Id(userId);
        conversation.setUser2Id(receiverId);

        Message message = new Message();
        message.setSenderUserId(userId);
        message.setReceiverUserId(receiverId);
        message.setConversation(conversation);
        message.setMessage(messageDTO.getMessage());
        message.setSentDate(LocalDate.now());

        messageService.saveConversation(conversation);
        messageService.sendMessage(message);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToMessageDTO(message, token));
    }

    private MessageDTO convertToMessageDTO(Message message, String token) {
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setId(message.getId());
        messageDTO.setSenderUserId(message.getSenderUserId());
        messageDTO.setSenderUsername(GetUserById(message.getSenderUserId(), token).getUsername());
        messageDTO.setReceiverUsername(GetUserById(message.getReceiverUserId(), token).getUsername());
        messageDTO.setConversationId(message.getConversation().getId());
        messageDTO.setMessage(message.getMessage());
        messageDTO.setSentDate(message.getSentDate());
        return messageDTO;
    }

    public UserDTO GetUserById(String userId, String token) {
        if(userId == null) {
            System.out.println("userId is null");
            return null;
        }

        String userServiceURL = "http://user-service:8082/user/" + userId;

        UserDTO user = this.webClientBuilder.build()
                .get()
                .uri(userServiceURL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(UserDTO.class)
                .block();

        System.out.println("user: " + user.toString());

        return user;
    }

    public String GetUserByUsername(String username, String token) {
        if(username == null) {
            System.out.println("username is null");
            return null;
        }

        String userServiceURL = "http://user-service:8082/user/username/" + username;
        String userId = this.webClientBuilder.build()
                .get()
                .uri(userServiceURL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println("userId: " + userId);
        return userId;
    }
}
