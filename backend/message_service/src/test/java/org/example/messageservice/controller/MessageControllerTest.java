package org.example.messageservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.messageservice.dto.ConversationDTO;
import org.example.messageservice.dto.MessageDTO;
import org.example.messageservice.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MessageService messageService;

    @MockBean
    private WebClient.Builder webClientBuilder;

    @BeforeEach
    public void setup() {
        reset(messageService);
    }

    @Test
    public void testGetInbox() throws Exception {
        String userId = "user1";

        // Arrange: mock the inbox to return 1 conversation with 2 messages
        List<String> messages = Arrays.asList("Hello", "Hi there");
        ConversationDTO conversationDTO = new ConversationDTO(
                1L,
                userId,
                "username2",
                "user2",
                messages);
        List<ConversationDTO> conversationsDTO = Collections.singletonList(conversationDTO);

        // Mock the service method
        when(messageService.getInboxConversations(eq(userId), anyString()))
                .thenReturn(conversationsDTO);

        // Mock the authentication
        JwtAuthenticationToken authentication = createMockAuthenticationToken(userId, "User");

        // Act & Assert: GET /inbox and check the conversation data in the response
        MvcResult result = mockMvc.perform(get("/messages/inbox")
                .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].user1Id").value(userId))
                .andExpect(jsonPath("$[0].user2Id").value("user2"))
                .andExpect(jsonPath("$[0].participant").value("username2"))
                .andReturn();
    }

    @Test
    public void testGetMessagesByConversationId() throws Exception {
        Long conversationId = 1L;
        String userId = "user1";

        // Arrange: create conversations with 2 messages
        MessageDTO message1DTO = new MessageDTO();
        message1DTO.setId(1L);
        message1DTO.setSenderUserId(userId);
        message1DTO.setSenderUsername("username1");
        message1DTO.setReceiverUsername("username2");
        message1DTO.setMessage("Hello");
        message1DTO.setConversationId(conversationId);
        message1DTO.setSentDate(LocalDate.now());

        MessageDTO message2DTO = new MessageDTO();
        message2DTO.setId(2L);
        message2DTO.setSenderUserId("user2");
        message2DTO.setSenderUsername("username2");
        message2DTO.setReceiverUsername("username1");
        message2DTO.setMessage("Hi there");
        message2DTO.setConversationId(conversationId);
        message2DTO.setSentDate(LocalDate.now());

        List<MessageDTO> messageDTOs = Arrays.asList(message1DTO, message2DTO);

        // Mock the service method
        when(messageService.getConversationMessages(eq(conversationId), anyString()))
                .thenReturn(messageDTOs);

        // Mock the authentication
        JwtAuthenticationToken authentication = createMockAuthenticationToken(userId, "User");

        // Act & Assert: GET /conversation/id and check it contains the message data
        mockMvc.perform(get("/messages/conversation/{conversationId}/messages", conversationId)
                .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].senderUserId").value(userId))
                .andExpect(jsonPath("$[0].senderUsername").value("username1"))
                .andExpect(jsonPath("$[0].receiverUsername").value("username2"))
                .andExpect(jsonPath("$[0].message").value("Hello"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].senderUserId").value("user2"))
                .andExpect(jsonPath("$[1].senderUsername").value("username2"))
                .andExpect(jsonPath("$[1].receiverUsername").value("username1"))
                .andExpect(jsonPath("$[1].message").value("Hi there"));
    }

    @Test
    public void testReplyToConversation() throws Exception {
        Long conversationId = 1L;
        String userId = "user1";

        MessageDTO replyDTO = new MessageDTO();
        replyDTO.setReceiverUsername("username2");
        replyDTO.setMessage("This is a reply");

        // Arrange: Create message conversation message & reply-message
        MessageDTO existingMessageDTO = new MessageDTO();
        existingMessageDTO.setId(1L);
        existingMessageDTO.setSenderUserId(userId);
        existingMessageDTO.setSenderUsername("username1");
        existingMessageDTO.setReceiverUsername("username2");
        existingMessageDTO.setMessage("Hello");
        existingMessageDTO.setConversationId(conversationId);
        existingMessageDTO.setSentDate(LocalDate.now());

        MessageDTO replyMessageDTO = new MessageDTO();
        replyMessageDTO.setId(2L);
        replyMessageDTO.setSenderUserId(userId);
        replyMessageDTO.setSenderUsername("username1");
        replyMessageDTO.setReceiverUsername("username2");
        replyMessageDTO.setMessage("This is a reply");
        replyMessageDTO.setConversationId(conversationId);
        replyMessageDTO.setSentDate(LocalDate.now());

        List<MessageDTO> updatedMessages = Arrays.asList(existingMessageDTO, replyMessageDTO);

        // Mock the service method
        when(messageService.replyToConversation(
                eq(conversationId),
                eq(userId),
                eq("username2"),
                eq("This is a reply"),
                anyString())).thenReturn(updatedMessages);

        // Mock the authentication
        JwtAuthenticationToken authentication = createMockAuthenticationToken(userId, "User");

        // Act & Assert: /reply/conversation and check messages in the coversation
        mockMvc.perform(post("/messages/reply/{conversationId}", conversationId)
                .with(authentication(authentication))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(replyDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("Hello"))
                .andExpect(jsonPath("$[1].message").value("This is a reply"));
    }

    @Test
    public void testReplyToConversationNotFound() throws Exception {
        Long conversationId = 999L;
        String userId = "user1";

        MessageDTO replyDTO = new MessageDTO();
        replyDTO.setReceiverUsername("username2");
        replyDTO.setMessage("This is a reply");

        // Arrange: Mock the service to throw exception when replying to non existent
        // conversation
        when(messageService.replyToConversation(
                eq(conversationId),
                eq(userId),
                eq("username2"),
                eq("This is a reply"),
                anyString())).thenThrow(new IllegalArgumentException("Conversation not found"));

        // Mock the authentication
        JwtAuthenticationToken authentication = createMockAuthenticationToken(userId, "User");

        // Act & Assert: POST /reply should return 404
        mockMvc.perform(post("/messages/reply/{conversationId}", conversationId)
                .with(authentication(authentication))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(replyDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testSendNewMessage() throws Exception {
        String userId = "user1";

        // Arrange: Create new message
        MessageDTO newMessageDTO = new MessageDTO();
        newMessageDTO.setReceiverUsername("username2");
        newMessageDTO.setMessage("Hello, new conversation");

        // Arrange: Create expected DTO that the service will return
        MessageDTO createdMessageDTO = new MessageDTO();
        createdMessageDTO.setId(1L);
        createdMessageDTO.setSenderUserId(userId);
        createdMessageDTO.setSenderUsername("username1");
        createdMessageDTO.setReceiverUsername("username2");
        createdMessageDTO.setMessage("Hello, new conversation");
        createdMessageDTO.setConversationId(1L);
        createdMessageDTO.setSentDate(LocalDate.now());

        // Mock the service method
        when(messageService.createNewConversation(
                eq(userId),
                eq("username2"),
                eq("Hello, new conversation"),
                anyString())).thenReturn(createdMessageDTO);

        // Mock the authentication
        JwtAuthenticationToken authentication = createMockAuthenticationToken(userId, "User");

        // Act & Assert: POST /new & check a proper conversation is created
        mockMvc.perform(post("/messages/new")
                .with(authentication(authentication))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newMessageDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.senderUserId").value(userId))
                .andExpect(jsonPath("$.senderUsername").value("username1"))
                .andExpect(jsonPath("$.receiverUsername").value("username2"))
                .andExpect(jsonPath("$.message").value("Hello, new conversation"));

    }

    @Test
    public void testSendNewMessageReceiverNotFound() throws Exception {
        String userId = "user1";

        // Arrange: Create message
        MessageDTO newMessageDTO = new MessageDTO();
        newMessageDTO.setReceiverUsername("nonexistent");
        newMessageDTO.setMessage("Hello");

        // Arrange: Mock service to throw exception when receiver does not exist.
        when(messageService.createNewConversation(
                eq(userId),
                eq("nonexistent"),
                eq("Hello"),
                anyString())).thenThrow(new IllegalArgumentException("Receiver not found"));

        // Mock the authentication
        JwtAuthenticationToken authentication = createMockAuthenticationToken(userId, "User");

        // Act & Assert: POST /new with invalid receiver should return 400
        mockMvc.perform(post("/messages/new")
                .with(authentication(authentication))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newMessageDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetInboxWithoutAuthentication() throws Exception {
        // Act & Assert: request without auth should be rejected
        mockMvc.perform(get("/messages/inbox"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetMessagesByConversationIdWithoutAuthentication() throws Exception {
        // Act & Assert: request without auth should be rejected
        mockMvc.perform(get("/messages/conversation/1/messages"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testReplyToConversationWithoutAuthentication() throws Exception {
        MessageDTO replyDTO = new MessageDTO();
        replyDTO.setReceiverUsername("username2");
        replyDTO.setMessage("Reply");

        // Act & Assert: POST /reply without auth should be rejected
        mockMvc.perform(post("/messages/reply/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(replyDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testSendNewMessageWithoutAuthentication() throws Exception {
        MessageDTO newMessageDTO = new MessageDTO();
        newMessageDTO.setReceiverUsername("username2");
        newMessageDTO.setMessage("Hello");

        // Act & Assert: POST /new without auth should be rejected
        mockMvc.perform(post("/messages/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newMessageDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetInboxEmptyResult() throws Exception {
        String userId = "user1";

        // Arrange: Mock empty inbox
        when(messageService.getInboxConversations(eq(userId), anyString()))
                .thenReturn(Collections.emptyList());

        // Mock the authentication
        JwtAuthenticationToken authentication = createMockAuthenticationToken(userId, "User");

        // Act & Assert: GET /inbox should return an empty array
        mockMvc.perform(get("/messages/inbox")
                .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    /**
     * Creates a mock JwtAuthenticationToken with given userId and role
     * to simulate authenticated requests in tests.
     */
    private JwtAuthenticationToken createMockAuthenticationToken(String userId, String role) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userId);
        claims.put("iss", "localhost:8080/");
        claims.put("role", role);

        Jwt jwt = new Jwt("token-value-for-testing", Instant.now(),
                Instant.now().plusSeconds(3600), headers, claims);

        return new JwtAuthenticationToken(jwt,
                Collections.singletonList(new SimpleGrantedAuthority(role)), userId);
    }
}