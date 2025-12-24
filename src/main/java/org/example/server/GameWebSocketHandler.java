package org.example.server;

import ch.qos.logback.core.net.server.Client;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import org.example.entity.PlayerEntity;
import org.example.model.*;
import org.example.repository.PlayerRepository;
import org.example.service.ClientSession;
import org.example.service.GameService;
import org.example.service.LobbiesService;
import org.example.service.LobbyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
@ChannelHandler.Sharable
public class GameWebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    
    @Autowired
    private GameService gameService;
    @Autowired
    private LobbiesService lobbiesService;
    @Autowired
    private LobbyService lobbyService;
    @Autowired
    private ConnectionManager connectionManager;

    private Map<String, ClientSession> sessions;


    @Autowired
    private PlayerRepository playerRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GameWebSocketHandler() {
        this.sessions = new HashMap<>();
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof CloseWebSocketFrame) {
            ctx.close();
            return;
        }
        
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException("Unsupported frame type: " + frame.getClass().getName());
        }
        
        String text = ((TextWebSocketFrame) frame).text();
        handleMessage(ctx.channel(), text);
    }

    private void handleMessage(Channel channel, String text) {
        try {
            Map<String, Object> message = objectMapper.readValue(text, Map.class);
            String type = (String) message.get("type");
            Map<String, Object> data = (Map<String, Object>) message.get("data");

            if (type == null || data == null) {
                System.err.println("Invalid message received. Missing type or data.");
                return;
            }

            String sessionId = connectionManager.getSessionId(channel);
            String username = (String) data.get("username");

            switch (type) {

                case "LOGIN":
                    handleLogin(channel, username);
                    break;

                case "JOIN":
                    handleJoin(channel, username);
                    break;

                case "START":
                    handleStart(sessionId, username);
                    break;


                case "INPUT":
                    handleInput(sessionId, data);
                    break;

                default:
                    System.err.println("Unknown message type: " + type);
            }

        } catch (Exception e) {
            System.err.println("Error handling message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleLogin(Channel channel, String username) {
        String newSessionId = UUID.randomUUID().toString();
        connectionManager.addConnection(newSessionId, channel, username);
    }
    private void handleJoin(Channel channel, String username) {
        String sessionId = UUID.randomUUID().toString();

        Optional<PlayerEntity> entityOpt = playerRepository.findByUsername(username);
        if (entityOpt.isEmpty()) {
            System.err.println("Player not found: " + username);
            return;
        }

        PlayerEntity player = entityOpt.get();

        LobbyEntry lobbyEntry = lobbyService.addLobbyEntry(player);
        ClientSession session = new ClientSession(sessionId, username);
        sessions.put(sessionId, session);

        // Send response
        Map<String, Object> response = new HashMap<>();
        response.put("type", "LOBBY");
        response.put("data", Map.of("Player", lobbyEntry));

        connectionManager.sendToSession(sessionId, response, objectMapper);
    }
    private void handleStart(String sessionId, String username) {
        if (sessionId == null) {
            System.err.println("Cannot start game: session is null");
            return;
        }

        // Direct game start (not from lobby) - pass null for lobbyId
        gameService.addPlayerToGame(sessionId, username, connectionManager.getChannel(sessionId), null);
    }
    private void handleInput(String sessionId, Map<String, Object> data) {
        if (sessionId == null) return;

        PlayerInput input = objectMapper.convertValue(data, PlayerInput.class);
        gameService.handlePlayerInput(sessionId, input);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("WebSocket connection opened: " + ctx.channel().remoteAddress());
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String sessionId = connectionManager.getSessionId(ctx.channel());
        if (sessionId != null) {
            System.out.println("WebSocket connection closed: " + sessionId);
            gameService.removePlayer(sessionId);
            connectionManager.removeConnection(sessionId);
        } else {
            System.out.println("WebSocket connection closed before handshake completed: " + ctx.channel().remoteAddress());
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Log all exceptions to understand what's happening
        String remoteAddr = ctx.channel().remoteAddress() != null ? 
            ctx.channel().remoteAddress().toString() : "unknown";
        
        if (cause instanceof IOException) {
            System.err.println("WebSocket IO error on channel " + remoteAddr + ": " + cause.getMessage());
            // Don't print stack trace for IO errors - they're usually connection resets
        } else {
            System.err.println("WebSocket error on channel " + remoteAddr + ": " + cause.getMessage());
            cause.printStackTrace();
        }
        // Don't close the channel - let Netty handle it
        // Closing here might interfere with handshake
    }
    
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // Log WebSocket handshake events
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            System.out.println("WebSocket handshake completed on server for: " + ctx.channel().remoteAddress());
        }
        super.userEventTriggered(ctx, evt);
    }
    
    public void sendMessage(Channel channel, Object message) {
        if (channel != null && channel.isActive()) {
            try {
                String json = objectMapper.writeValueAsString(message);
                channel.writeAndFlush(new TextWebSocketFrame(json));
            } catch (Exception e) {
                System.err.println("Error sending message: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    public void broadcast(Object message) {
        connectionManager.broadcast(message, objectMapper);
    }
}

