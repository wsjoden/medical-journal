package org.example.messageservice.service;

import org.example.messageservice.model.Conversation;
import org.example.messageservice.model.Message;
import org.example.messageservice.repository.IConversationRepository;
import org.example.messageservice.repository.IMessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    private final IMessageRepository messageRepository;
    private final IConversationRepository conversationRepository;

    public MessageService(IMessageRepository messageRepository, IConversationRepository conversationRepository) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
    }

    public List<Conversation> findConversationsByUserId(String userId) {
        return conversationRepository.findConversationsByUserId(userId,userId);
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
}

