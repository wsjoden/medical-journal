package org.example.messageservice.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String senderUserId;
    private String receiverUserId;

    @ManyToOne
    @JoinColumn(name = "conversationId")
    private Conversation conversation;

    private String message;
    private LocalDate sentDate;

    public Message() {}

    public Message(String senderUserId, String receiverUserId, Conversation conversation, String message) {
        this.senderUserId = senderUserId;
        this.receiverUserId = receiverUserId;
        this.conversation = conversation;
        this.message = message;
        this.sentDate = LocalDate.now();
    }

    public long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(String senderUserId) {
        this.senderUserId = senderUserId;
    }

    public String getReceiverUserId() {
        return receiverUserId;
    }

    public void setReceiverUserId(String receiverUserId) {
        this.receiverUserId = receiverUserId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public LocalDate getSentDate() {
        return sentDate;
    }

    public void setSentDate(LocalDate sentDate) {
        this.sentDate = sentDate;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", senderId=" + senderUserId +
                ", receiverId=" + receiverUserId +
                ", conversation=" + conversation.getId() +
                ", message='" + message + '\'' +
                ", sentDate=" + sentDate +
                '}';
    }
}