package org.example.messageservice.service;

import org.example.messageservice.dto.ConversationDTO;
import org.example.messageservice.dto.MessageDTO;
import org.example.messageservice.dto.UserDTO;
import org.example.messageservice.model.Conversation;
import org.example.messageservice.model.Message;
import org.example.messageservice.repository.IConversationRepository;
import org.example.messageservice.repository.IMessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final IMessageRepository messageRepository;
    private final IConversationRepository conversationRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${user.service.url}")
    private String userServiceURL;

    public MessageService(IMessageRepository messageRepository, IConversationRepository conversationRepository,
            WebClient.Builder webClientBuilder) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.webClientBuilder = webClientBuilder;
    }

    public List<Conversation> findConversationsByUserId(String userId) {
        return conversationRepository.findConversationsByUserId(userId, userId);
    }

    public List<Message> findMessagesByConversationId(Long conversationId) {
        return messageRepository.findMessagesByConversationId(conversationId);
    }

    public Optional<Conversation> findConversationById(Long conversationId) {
        return conversationRepository.findById(conversationId);
    }

    public Conversation saveConversation(Conversation conversation) {
        return conversationRepository.save(conversation);
    }

    public void sendMessage(Message message) {
        messageRepository.save(message);
    }

    public List<ConversationDTO> getInboxConversations(String userId, String token) {
        List<Conversation> conversations = findConversationsByUserId(userId);
        List<ConversationDTO> conversationsDTO = new ArrayList<>();

        for (Conversation c : conversations) {
            long conversationId = c.getId();
            List<String> messages = c.getMessages().stream()
                    .map(Message::getMessage)
                    .collect(Collectors.toList());

            String participantId = c.getUser1Id().equals(userId) ? c.getUser2Id() : c.getUser1Id();
            String participantUsername = getUserById(participantId, token).getUsername();

            ConversationDTO conversationDTO = new ConversationDTO(
                    conversationId,
                    c.getUser1Id(),
                    participantUsername,
                    c.getUser2Id(),
                    messages);
            conversationsDTO.add(conversationDTO);
        }

        return conversationsDTO;
    }

    public List<MessageDTO> getConversationMessages(Long conversationId, String token) {
        List<Message> messages = findMessagesByConversationId(conversationId);
        return messages.stream()
                .map(message -> convertToMessageDTO(message, token))
                .collect(Collectors.toList());
    }

    public List<MessageDTO> replyToConversation(Long conversationId, String senderId,
            String receiverUsername, String messageText, String token) {
        String receiverId = getUserIdByUsername(receiverUsername, token);

        Conversation conversation = findConversationById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        Message message = new Message();
        message.setSenderUserId(senderId);
        message.setReceiverUserId(receiverId);
        message.setConversation(conversation);
        message.setMessage(messageText);
        message.setSentDate(LocalDate.now());

        sendMessage(message);

        return getConversationMessages(conversationId, token);
    }

    public MessageDTO createNewConversation(String senderId, String receiverUsername,
            String messageText, String token) {
        String receiverId = getUserIdByUsername(receiverUsername, token);

        if (receiverId == null) {
            throw new IllegalArgumentException("Receiver not found");
        }

        Conversation conversation = new Conversation();
        conversation.setUser1Id(senderId);
        conversation.setUser2Id(receiverId);
        saveConversation(conversation);

        Message message = new Message();
        message.setSenderUserId(senderId);
        message.setReceiverUserId(receiverId);
        message.setConversation(conversation);
        message.setMessage(messageText);
        message.setSentDate(LocalDate.now());

        sendMessage(message);

        return convertToMessageDTO(message, token);
    }

    // Helper methods for external service calls

    public UserDTO getUserById(String userId, String token) {
        if (userId == null) {
            return null;
        }

        String url = userServiceURL + "/user/" + userId;

        return this.webClientBuilder.build()
                .get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(UserDTO.class)
                .block();
    }

    public String getUserIdByUsername(String username, String token) {
        if (username == null) {
            return null;
        }

        String url = userServiceURL + "/user/username/" + username;

        return this.webClientBuilder.build()
                .get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private MessageDTO convertToMessageDTO(Message message, String token) {
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setId(message.getId());
        messageDTO.setSenderUserId(message.getSenderUserId());
        messageDTO.setSenderUsername(getUserById(message.getSenderUserId(), token).getUsername());
        messageDTO.setReceiverUsername(getUserById(message.getReceiverUserId(), token).getUsername());
        messageDTO.setConversationId(message.getConversation().getId());
        messageDTO.setMessage(message.getMessage());
        messageDTO.setSentDate(message.getSentDate());
        return messageDTO;
    }
}
