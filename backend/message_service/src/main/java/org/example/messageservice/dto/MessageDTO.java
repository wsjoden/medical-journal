package org.example.messageservice.dto;

import java.time.LocalDate;

public class MessageDTO {
    private Long id;
    private Long conversationId;
    private String senderUserId;
    private String senderUsername;
    private String receiverUsername;
    private String message;
    private LocalDate sentDate;

    public MessageDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(String senderUserId) {
        this.senderUserId = senderUserId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDate getSentDate() {
        return sentDate;
    }

    public void setSentDate(LocalDate sentDate) {
        this.sentDate = sentDate;
    }

    // toString
    @Override
    public String toString() {
        return "MessageDTO{" +
                "id=" + id +
                ", conversationId=" + conversationId +
                ", senderId=" + senderUserId +
                ", senderUsername='" + senderUsername + '\'' +
                ", receiverUsername='" + receiverUsername + '\'' +
                ", message='" + message + '\'' +
                ", sentDate=" + sentDate +
                '}';
    }
}