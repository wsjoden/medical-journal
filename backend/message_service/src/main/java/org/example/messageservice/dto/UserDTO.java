package org.example.messageservice.dto;

public class UserDTO {
    private String userId;
    private String username;

    public UserDTO() {
    }

    public UserDTO(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                '}';
    }

}