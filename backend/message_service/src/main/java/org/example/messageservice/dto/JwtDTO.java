package org.example.messageservice.dto;

public class JwtDTO {
    private String username;
    private String Role;
    private Long userId;

    public JwtDTO() {
    }

    public JwtDTO(String username, String Role, Long userId) {
        this.username = username;
        this.Role = Role;
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return Role;
    }

    public void setRole(String role) {
        Role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}

