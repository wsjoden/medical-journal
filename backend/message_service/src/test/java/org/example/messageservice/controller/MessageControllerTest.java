package org.example.messageservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.messageservice.dto.ConversationDTO;
import org.example.messageservice.dto.MessageDTO;
import org.example.messageservice.dto.UserDTO;
import org.example.messageservice.model.Conversation;
import org.example.messageservice.model.Message;
import org.example.messageservice.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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

import org.example.messageservice.controler.MessageController;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

    @SpyBean
    private MessageController messageController;

    @MockBean
    private WebClient.Builder webClientBuilder;

    @BeforeEach
    public void setup() {
        // Mock the GetUserById and GetUserByUsername methods
        doReturn(new UserDTO("user1", "username1")).when(messageController).GetUserById(eq("user1"), anyString());
        doReturn(new UserDTO("user2", "username2")).when(messageController).GetUserById(eq("user2"), anyString());
        doReturn("user1").when(messageController).GetUserByUsername(eq("username1"), anyString());
        doReturn("user2").when(messageController).GetUserByUsername(eq("username2"), anyString());
    }

    @Test
    public void testGetInbox() throws Exception {
        // Create test data
        String userId = "user1";

        Conversation conversation = new Conversation();
        conversation.setId(1L);
        conversation.setUser1Id(userId);
        conversation.setUser2Id("user2");

        Message message1 = new Message();
        message1.setId(1L);
        message1.setSenderUserId(userId);
        message1.setReceiverUserId("user2");
        message1.setMessage("Hello");
        message1.setSentDate(LocalDate.now());
        message1.setConversation(conversation);

        Message message2 = new Message();
        message2.setId(2L);
        message2.setSenderUserId("user2");
        message2.setReceiverUserId(userId);
        message2.setMessage("Hi there");
        message2.setSentDate(LocalDate.now());
        message2.setConversation(conversation);

        List<Message> messages = Arrays.asList(message1, message2);
        conversation.setMessages(messages);

        List<Conversation> conversations = Collections.singletonList(conversation);

        // Mock the service
        when(messageService.findConversationsByUserId(userId)).thenReturn(conversations);

        // Create authentication token
        JwtAuthenticationToken authentication = createMockAuthenticationToken(userId, "User");

        // Execute test
        MvcResult result = mockMvc.perform(get("/messages/inbox")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].user1Id").value(userId))
                .andExpect(jsonPath("$[0].user2Id").value("user2"))
                .andExpect(jsonPath("$[0].participant").value("username2"))
                .andReturn();

        // Further verification if needed
        String content = result.getResponse().getContentAsString();
        List<ConversationDTO> returnedConversations = objectMapper.readValue(content,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ConversationDTO.class));

        assertEquals(1, returnedConversations.size());
        assertEquals(2, returnedConversations.get(0).getMessages().size());
    }

    @Test
    public void testGetMessagesByConversationId() throws Exception {
        // Create test data
        Long conversationId = 1L;
        String userId = "user1";

        Conversation conversation = new Conversation();
        conversation.setId(conversationId);
        conversation.setUser1Id(userId);
        conversation.setUser2Id("user2");

        Message message1 = new Message();
        message1.setId(1L);
        message1.setSenderUserId(userId);
        message1.setReceiverUserId("user2");
        message1.setMessage("Hello");
        message1.setSentDate(LocalDate.now());
        message1.setConversation(conversation);

        Message message2 = new Message();
        message2.setId(2L);
        message2.setSenderUserId("user2");
        message2.setReceiverUserId(userId);
        message2.setMessage("Hi there");
        message2.setSentDate(LocalDate.now());
        message2.setConversation(conversation);

        List<Message> messages = Arrays.asList(message1, message2);

        // Mock the service
        when(messageService.findMessagesByConversationId(conversationId)).thenReturn(messages);

        // Create authentication token
        JwtAuthenticationToken authentication = createMockAuthenticationToken(userId, "User");

        // Execute test
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
        // Create test data
        Long conversationId = 1L;
        String userId = "user1";
        String receiverId = "user2";

        Conversation conversation = new Conversation();
        conversation.setId(conversationId);
        conversation.setUser1Id(userId);
        conversation.setUser2Id(receiverId);

        Message existingMessage = new Message();
        existingMessage.setId(1L);
        existingMessage.setSenderUserId(userId);
        existingMessage.setReceiverUserId(receiverId);
        existingMessage.setMessage("Hello");
        existingMessage.setSentDate(LocalDate.now());
        existingMessage.setConversation(conversation);

        MessageDTO replyDTO = new MessageDTO();
        replyDTO.setReceiverUsername("username2");
        replyDTO.setMessage("This is a reply");

        Message replyMessage = new Message();
        replyMessage.setId(2L);
        replyMessage.setSenderUserId(userId);
        replyMessage.setReceiverUserId(receiverId);
        replyMessage.setMessage("This is a reply");
        replyMessage.setSentDate(LocalDate.now());
        replyMessage.setConversation(conversation);

        List<Message> updatedMessages = Arrays.asList(existingMessage, replyMessage);

        // Mock the service
        when(messageService.findConversationById(conversationId)).thenReturn(Optional.of(conversation));
        when(messageService.findMessagesByConversationId(conversationId)).thenReturn(updatedMessages);

        // Create authentication token
        JwtAuthenticationToken authentication = createMockAuthenticationToken(userId, "User");

        // Execute test
        mockMvc.perform(post("/messages/reply/{conversationId}", conversationId)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replyDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("Hello"))
                .andExpect(jsonPath("$[1].message").value("This is a reply"));

        // Verify the service was called
        verify(messageService).sendMessage(any(Message.class));
    }

    @Test
    public void testSendNewMessage() throws Exception {
        // Create test data
        String userId = "user1";
        String receiverId = "user2";

        MessageDTO newMessageDTO = new MessageDTO();
        newMessageDTO.setReceiverUsername("username2");
        newMessageDTO.setMessage("Hello, new conversation");

        // Mock the service
        // No specific mocking needed as we're using SpyBean with doReturn for the user methods

        // Create authentication token
        JwtAuthenticationToken authentication = createMockAuthenticationToken(userId, "User");

        // Execute test
        mockMvc.perform(post("/messages/new")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newMessageDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.senderUserId").value(userId))
                .andExpect(jsonPath("$.senderUsername").value("username1"))
                .andExpect(jsonPath("$.receiverUsername").value("username2"))
                .andExpect(jsonPath("$.message").value("Hello, new conversation"));

        // Verify the services were called
        verify(messageService).saveConversation(any(Conversation.class));
        verify(messageService).sendMessage(any(Message.class));
    }

    @Test
    public void testGetInboxWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/messages/inbox"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetMessagesByConversationIdWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/messages/conversation/1/messages"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void debugAuthorities() throws Exception {
        JwtAuthenticationToken authentication = createMockAuthenticationToken("patient1", "Patient");

        MvcResult result = mockMvc.perform(get("/user/patients").with(authentication(authentication))).andReturn();

        System.out.println("Status: " + result.getResponse().getStatus());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            System.out.println("Authentication class: " + auth.getClass().getName());
            System.out.println("Principal: " + auth.getPrincipal());
            System.out.println("Authorities: " + auth.getAuthorities());

            for (GrantedAuthority authority : auth.getAuthorities()) {
                System.out.println("Authority: '" + authority.getAuthority() + "'");
            }
        } else {
            System.out.println("No authentication in context");
        }
    }

    private Jwt createMockJwt() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        claims.put("iss", "localhost:8080/");
        claims.put("role", "USER");

        return new Jwt("token-value-for-testing", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);
    }

    private JwtAuthenticationToken createMockAuthenticationToken(String userId, String role) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userId);
        claims.put("iss", "localhost:8080/");
        claims.put("role", role);

        Jwt jwt = new Jwt("token-value-for-testing", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);

        return new JwtAuthenticationToken(jwt, Collections.singletonList(new SimpleGrantedAuthority(role)), userId);
    }
}
