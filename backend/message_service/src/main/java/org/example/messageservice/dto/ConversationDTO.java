package org.example.messageservice.dto;

import java.util.List;

public class ConversationDTO {
    private Long id;
    private String user1Id;
    private String participant;
    private String user2Id;
    private List<String> messages;

    public ConversationDTO() {
    }

    public ConversationDTO(long ConversationID, String user1Id, String participant, String user2Id, List<String> messages) {
        this.id = ConversationID;
        this.user1Id = user1Id;
        this.participant = participant;
        this.user2Id = user2Id;
        this.messages = messages;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public String getUser1Id() {
        return user1Id;
    }

    public void setUser1Id(String user1Id) {
        this.user1Id = user1Id;
    }

    public String getUser2Id() {
        return user2Id;
    }

    public void setUser2Id(String user2Id) {
        this.user2Id = user2Id;
    }

    public String getParticipant() {
        return participant;
    }

    public void setParticipant(String participant) {
        this.participant = participant;
    }

    @Override
    public String toString() {
        return "ConversationDTO{" +
                "user1='" + user1Id + '\'' +
                ", user2='" + user2Id + '\'' +
                ", messages=" + messages +
                '}';
    }
}
