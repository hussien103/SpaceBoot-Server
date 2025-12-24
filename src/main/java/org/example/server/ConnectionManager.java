package org.example.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConnectionManager {
    private final Map<String, Channel> sessionToChannel = new ConcurrentHashMap<>();
    private final Map<Channel, String> channelToSession = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToUsername = new ConcurrentHashMap<>();
    private final Map<String, String> usernameToSession = new ConcurrentHashMap<>();
    
    public void addConnection(String sessionId, Channel channel, String username) {
        sessionToChannel.put(sessionId, channel);
        channelToSession.put(channel, sessionId);
        sessionToUsername.put(sessionId, username);
        if (username != null) {
            usernameToSession.put(username, sessionId);
        }
        System.out.println("Connection added: " + sessionId + " (" + username + ")");
    }
    
    public void removeConnection(String sessionId) {
        Channel channel = sessionToChannel.remove(sessionId);
        if (channel != null) {
            channelToSession.remove(channel);
        }
        String username = sessionToUsername.remove(sessionId);
        if (username != null) {
            usernameToSession.remove(username);
        }
        System.out.println("Connection removed: " + sessionId);
    }
    
    public Channel getChannel(String sessionId) {
        return sessionToChannel.get(sessionId);
    }
    
    public String getSessionId(Channel channel) {
        return channelToSession.get(channel);
    }
    
    public String getUsername(String sessionId) {
        return sessionToUsername.get(sessionId);
    }

    public String getSessionIdByUsername(String username) {
        return usernameToSession.get(username);
    }
    
    public void sendToSession(String sessionId, Object message, ObjectMapper objectMapper) {
        Channel channel = sessionToChannel.get(sessionId);
        if (channel != null && channel.isActive()) {
            try {
                String json = objectMapper.writeValueAsString(message);
                channel.writeAndFlush(new TextWebSocketFrame(json));
            } catch (Exception e) {
                System.err.println("Error sending to session " + sessionId + ": " + e.getMessage());
            }
        }
    }

    public void broadcast(Object message, ObjectMapper objectMapper) {
        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            System.err.println("Error serializing broadcast message: " + e.getMessage());
            return;
        }

        for (Channel channel : sessionToChannel.values()) {
            if (channel != null && channel.isActive()) {
                TextWebSocketFrame frame = new TextWebSocketFrame(json);
                channel.writeAndFlush(frame);
                // No need to retain/release manually because each channel gets a fresh frame
            }
        }
    }
    public void broadCastEveryoneElse(Object message, ObjectMapper objectMapper, Channel ct) {
        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            System.err.println("Error serializing broadcast message: " + e.getMessage());
            return;
        }

        for (Channel channel : sessionToChannel.values()) {
            if (channel != null && channel.isActive() && channel != ct) {
                TextWebSocketFrame frame = new TextWebSocketFrame(json);
                channel.writeAndFlush(frame);
                // No need to retain/release manually because each channel gets a fresh frame
            }
        }
    }


    public int getConnectionCount() {
        return sessionToChannel.size();
    }
}

