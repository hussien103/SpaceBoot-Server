package org.example.service;

public class ClientSession {
    private String sessionId;
    private String username;

    public ClientSession(String sessionId, String username) {
        this.sessionId = sessionId;
        this.username = username;
    }

    public String getSessionId() { return sessionId; }
    public String getUsername() { return username; }
}