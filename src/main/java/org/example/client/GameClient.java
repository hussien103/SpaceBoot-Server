package org.example.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.example.model.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class GameClient {
    private static final String WS_URL = "ws://localhost:8081/ws";
    private EventLoopGroup group;
    private Channel channel;
    private boolean connected;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Consumer<Lobby> lobbyCreatedListener;
    private Consumer<GameState> gameStateListener;
    private Consumer<String> notificationListener;
    private Consumer<String> playerIdListener;
    private final List<Consumer<List<LeaderboardEntry>>> leaderboardListeners = new CopyOnWriteArrayList<>();
    private volatile List<LeaderboardEntry> latestLeaderboard = new ArrayList<>();
    private Consumer<List<LobbyEntry>> lobbyListener;
    private String username;
    private volatile String playerId;
    private volatile boolean handshakeComplete = false;
    private final Object handshakeLock = new Object();

    public boolean connect(String username) {
        this.username = username;
        this.handshakeComplete = false;
        try {
            group = new NioEventLoopGroup();
            URI uri = new URI(WS_URL);
            String host = uri.getHost();
            int port = uri.getPort() == -1 ? (uri.getScheme().equals("wss") ? 443 : 80) : uri.getPort();
            
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpClientCodec());
                            pipeline.addLast(new HttpObjectAggregator(65536));
                            pipeline.addLast(new WebSocketClientProtocolHandler(
                                    WebSocketClientHandshakerFactory.newHandshaker(
                                            uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders())));
                            pipeline.addLast(new GameWebSocketClientHandler());
                        }
                    });
            
            ChannelFuture connectFuture = bootstrap.connect(host, port);
            channel = connectFuture.sync().channel();
            
            // Wait for channel to be active (connection established)
            if (!channel.isActive()) {
                throw new IllegalStateException("Channel is not active after connection");
            }
            
            // Wait briefly for WebSocket handshake to complete
            // The handshake will be detected either via userEventTriggered or when first frame is received
            // We'll also try sending the JOIN message - if handshake isn't complete, it will be queued
            synchronized (handshakeLock) {
                long startTime = System.currentTimeMillis();
                while (!handshakeComplete && (System.currentTimeMillis() - startTime) < 1000) {
                    handshakeLock.wait(50);
                }
            }
            
            connected = true;
            System.out.println("Connected to game server via Netty WebSocket" + 
                (handshakeComplete ? " (handshake confirmed)" : " (sending JOIN, handshake may still be in progress)"));
            
            // Send join message - Netty will queue it if handshake not complete yet
            sendJoinMessage();
            
            return true;
        } catch (Exception e) {
            connected = false;
            System.err.println("Failed to connect via WebSocket: " + e.getMessage());
            e.printStackTrace();
            if (group != null) {
                group.shutdownGracefully();
            }
            return false;
        }
    }
    
    private void sendJoinMessage() {
        if (channel == null || !channel.isActive()) {
            System.err.println("Cannot send JOIN - channel not active");
            return;
        }
        
        try {
            Map<String, Object> message = new java.util.HashMap<>();
            message.put("type", "LOGIN");
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("username", username);
            message.put("data", data);
            
            String json = objectMapper.writeValueAsString(message);
            // Ensure UTF-8 encoding
            TextWebSocketFrame frame = new TextWebSocketFrame(json);
            channel.writeAndFlush(frame);
            System.out.println("LOGIN message sent for user: " + username);
        } catch (Exception e) {
            System.err.println("Error sending join message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setGameStateListener(Consumer<GameState> listener) {
        this.gameStateListener = listener;
    }

    public void addLeaderboardListener(Consumer<List<LeaderboardEntry>> listener) {
        leaderboardListeners.add(listener);
        if (!latestLeaderboard.isEmpty()) {
            listener.accept(new ArrayList<>(latestLeaderboard));
        }
    }

    public void removeLeaderboardListener(Consumer<List<LeaderboardEntry>> listener) {
        leaderboardListeners.remove(listener);
    }

    public List<LeaderboardEntry> getLatestLeaderboard() {
        return new ArrayList<>(latestLeaderboard);
    }

    public void setPlayerIdListener(Consumer<String> listener) {
        this.playerIdListener = listener;
        if (playerId != null) {
            listener.accept(playerId);
        }
    }

    public String getPlayerId() {
        return playerId;
    }

    public void sendPlayerInput(PlayerInput input) {
        if (channel != null && channel.isActive()) {
            try {
                Map<String, Object> message = new java.util.HashMap<>();
                message.put("type", "INPUT");
                message.put("data", input);
                
                String json = objectMapper.writeValueAsString(message);
                // Ensure UTF-8 encoding
                TextWebSocketFrame frame = new TextWebSocketFrame(json);
                channel.writeAndFlush(frame);
            } catch (Exception e) {
                System.err.println("Error sending player input: " + e.getMessage());
            }
        }
    }

    public void setNotificationListener(Consumer<String> notificationListener) {
        this.notificationListener = notificationListener;
    }
    public void setLobbyListener(Consumer<List<LobbyEntry>> lobbyListener) {
        this.lobbyListener = lobbyListener;
    }
    public boolean isConnected() {
        return connected && channel != null && channel.isActive();
    }

    public void stop() {
        connected = false;
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(new CloseWebSocketFrame());
            channel.close();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
    }

    private void notifyLeaderboardListeners(List<LeaderboardEntry> entries) {
        for (Consumer<List<LeaderboardEntry>> listener : leaderboardListeners) {
            listener.accept(new ArrayList<>(entries));
        }
    }

    private void handleMessage(String text) {
        try {
            Map<String, Object> message = objectMapper.readValue(text, Map.class);
            String type = (String) message.get("type");
            Object data = message.get("data");
            
            switch (type) {
                case "GAME_STATE":
                    GameState state = objectMapper.convertValue(data, GameState.class);
//                    System.out.println("Received GAME_STATE - Spaceships: " + (state != null ? state.getSpaceships().size() : 0));
                    if (gameStateListener != null) {
                        gameStateListener.accept(state);
                    }
                    break;
                    
                case "LEADERBOARD":
                    List<Map<String, Object>> entriesList = (List<Map<String, Object>>) data;
                    List<LeaderboardEntry> entries = new ArrayList<>();
                    for (Map<String, Object> entryMap : entriesList) {
                        LeaderboardEntry entry = objectMapper.convertValue(entryMap, LeaderboardEntry.class);
                        entries.add(entry);
                    }
                    latestLeaderboard = entries;
                    notifyLeaderboardListeners(entries);
                    break;
                    
                case "PLAYER_INFO":
                    Map<String, Object> infoMap = (Map<String, Object>) data;
                    playerId = (String) infoMap.get("playerId");
//                    System.out.println("Received player ID: " + playerId);
                    if (playerIdListener != null) {
                        playerIdListener.accept(playerId);
                    }
                    break;
                    
                case "NOTIFICATION":
                    String notification = (String) data;
                    if (notificationListener != null) {
                        notificationListener.accept(notification);
                    }
                    break;
                case "LOBBY":
                    // The server sends a Map, not a List
                    Map<String, Object> lobbyMap = (Map<String, Object>) data;

                    List<LobbyEntry> lobbyEntries = new ArrayList<>();

                    for (Object value : lobbyMap.values()) {
                        LobbyEntry entry = objectMapper.convertValue(value, LobbyEntry.class);
                        lobbyEntries.add(entry);
                    }

                    this.lobbyListener.accept(lobbyEntries);
                    break;
                case "LOBBY_CREATED":
                    Lobby lobby = objectMapper.convertValue(data, Lobby.class);


                    this.lobbyCreatedListener.accept(lobby);
                default:
                    System.out.println("Unknown message type: " + type);
            }
        } catch (Exception e) {
            System.err.println("Error handling message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void setLobbyCreatedListener(Consumer<Lobby> lobbyCreatedListener){
        this.lobbyCreatedListener = lobbyCreatedListener;

    }
    private class GameWebSocketClientHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
        
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            // Log all events to debug
            System.out.println("Received user event: " + evt.getClass().getName() + " = " + evt);
            
            // WebSocket handshake is complete when this event is triggered
            if (evt instanceof WebSocketClientProtocolHandler.ClientHandshakeStateEvent) {
                WebSocketClientProtocolHandler.ClientHandshakeStateEvent stateEvent = 
                    (WebSocketClientProtocolHandler.ClientHandshakeStateEvent) evt;
                System.out.println("Handshake state: " + stateEvent);
                if (stateEvent == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE) {
                    synchronized (handshakeLock) {
                        handshakeComplete = true;
                        handshakeLock.notifyAll();
                    }
                    System.out.println("WebSocket handshake completed");
                } else if (stateEvent == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_TIMEOUT) {
                    System.err.println("WebSocket handshake timed out!");
                }
            }
            super.userEventTriggered(ctx, evt);
        }
        
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            // Channel active means TCP connection established, not WebSocket handshake
            // Handshake happens after this
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
            // handshake guard (same as you have)
            if (!handshakeComplete) {
                synchronized (handshakeLock) {
                    handshakeComplete = true;
                    handshakeLock.notifyAll();
                }
                System.out.println("WebSocket handshake completed (detected via first frame)");
            }

//            System.out.println("Received frame type: " + frame.getClass().getSimpleName());

            if (frame instanceof CloseWebSocketFrame) {
                connected = false;
                ctx.close();
                return;
            }

            if (frame instanceof PongWebSocketFrame) {
                return; // ignore
            }

            if (frame instanceof TextWebSocketFrame) {
                String text = ((TextWebSocketFrame) frame).text();
                // defensive: log length
//                System.out.println("Text frame length: " + (text == null ? 0 : text.length()));
                handleMessage(text);
                return;
            }

            if (frame instanceof BinaryWebSocketFrame) {
                // Received binary frame — log and ignore (or handle if you expect binary)
                System.out.println("Received BinaryWebSocketFrame - ignoring (not expecting binary)");
                return;
            }

            if (frame instanceof ContinuationWebSocketFrame) {
                System.out.println("Received ContinuationWebSocketFrame - unexpected (aggregator should handle this)");
                return;
            }

            // Unknown frame
            System.out.println("Ignoring unsupported frame: " + frame.getClass().getName());
        }


        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            connected = false;
            System.out.println("WebSocket connection closed");
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            // Don't close on read errors - they might be transient
            if (cause instanceof java.io.IOException) {
                System.err.println("WebSocket IO error (connection may have been closed): " + cause.getMessage());
            } else {
                System.err.println("WebSocket error: " + cause.getMessage());
                cause.printStackTrace();
            }
            // Only close if it's a critical error
            if (!(cause instanceof java.io.IOException)) {
                ctx.close();
            }
        }
    }
}
