package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.PlayerEntity;
import org.example.model.*;
import org.example.server.ConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import io.netty.channel.Channel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {
    private static final int GAME_WIDTH = 800;
    private static final int GAME_HEIGHT = 600;
    private static final long SHOT_COOLDOWN = 200; // milliseconds
    
    @Autowired
    private LeaderboardService leaderboardService;
    
    @Autowired
    private ConnectionManager connectionManager;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, ClientSession> sessions;
    private GameState gameState;
    private Map<String, Long> lastShotTime;
    private long lastUpdateTime;
    
    public GameService() {
        this.sessions = new ConcurrentHashMap<>();
        this.gameState = new GameState();
        this.lastShotTime = new ConcurrentHashMap<>();
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public void addPlayerToGame(String sessionId, String username, Channel ct) {
        // Reset game state if starting a new game
        if (gameState.isGameOver() || gameState.getSpaceships().isEmpty()) {
            gameState = new GameState();
            lastShotTime.clear();
            leaderboardService.broadcastLeaderboard();
            System.out.println("Starting new game...");
        }


        // Create spaceship at random position
        Random random = new Random();
        double x = 100 + random.nextDouble() * (GAME_WIDTH - 200);
        double y = 100 + random.nextDouble() * (GAME_HEIGHT - 200);
        
        Spaceship spaceship = new Spaceship(sessionId, x, y);
        gameState.addSpaceship(spaceship);
        
        System.out.println("Player added: " + username + " (Session: " + sessionId + ") at (" + x + ", " + y + ")");
        System.out.println("Total players in game: " + gameState.getSpaceships().size());
        
        // Send player info via Netty
        sendPlayerInfo(sessionId, sessionId, username);
        broadcastGameState();
        broadCastNotification(connectionManager.getUsername(sessionId),ct);
    }
    public void broadCastNotification(String name,Channel ct){
        if (connectionManager.getConnectionCount() == 0) {
            return; // No connections to broadcast to
        }
        Map<String, Object> message = new HashMap<>();
        message.put("type", "NOTIFICATION");
        message.put("data", name+"has joined the game");
        System.out.println(" player " + name + " has joined the game");
        connectionManager.broadCastEveryoneElse(message,objectMapper, ct);
    }
    private void broadcastGameState() {
        if (connectionManager.getConnectionCount() == 0) {
            return; // No connections to broadcast to
        }
        Map<String, Object> message = new HashMap<>();
        message.put("type", "GAME_STATE");
        message.put("data", gameState);
//        System.out.println("Broadcasting game state to " + connectionManager.getConnectionCount() + " connections - Spaceships: " + gameState.getSpaceships().size());
        connectionManager.broadcast(message, objectMapper);
    }
    private void sendPlayerInfo(String sessionId, String playerId, String username) {
        Map<String, Object> info = new HashMap<>();
        info.put("type", "PLAYER_INFO");
        Map<String, Object> data = new HashMap<>();
        data.put("playerId", playerId);
        data.put("username", username);
        info.put("data", data);
        connectionManager.sendToSession(sessionId, info, objectMapper);
    }
    
    public void removePlayer(String sessionId) {
        ClientSession session = sessions.remove(sessionId);
        Spaceship spaceship = gameState.getSpaceships().get(sessionId);
        if (spaceship != null && session != null) {
            boolean won = spaceship.isAlive() && gameState.getSpaceships().size() == 1;
            leaderboardService.updatePlayerStats(session.getUsername(), spaceship.getScore(), won);
        }
        gameState.removeSpaceship(sessionId);
        lastShotTime.remove(sessionId);
        broadcastGameState();
    }
    
    public void handlePlayerInput(String sessionId, PlayerInput input) {
        Spaceship spaceship = gameState.getSpaceships().get(sessionId);
        if (spaceship == null || !spaceship.isAlive()) return;
        
        double speed = 3.0;
        double rotationSpeed = 5.0;
        
        // Movement
        if (input.isMoveUp()) {
            double radians = Math.toRadians(spaceship.getAngle());
            spaceship.move(Math.cos(radians) * speed, Math.sin(radians) * speed);
        }
        if (input.isMoveDown()) {
            double radians = Math.toRadians(spaceship.getAngle());
            spaceship.move(-Math.cos(radians) * speed, -Math.sin(radians) * speed);
        }
        if (input.isMoveLeft()) {
            double radians = Math.toRadians(spaceship.getAngle() - 90);
            spaceship.move(Math.cos(radians) * speed, Math.sin(radians) * speed);
        }
        if (input.isMoveRight()) {
            double radians = Math.toRadians(spaceship.getAngle() + 90);
            spaceship.move(Math.cos(radians) * speed, Math.sin(radians) * speed);
        }
        
        // Rotation
        if (input.isRotateLeft()) {
            spaceship.rotate(-rotationSpeed);
        }
        if (input.isRotateRight()) {
            spaceship.rotate(rotationSpeed);
        }
        
        // Shooting
        if (input.isShoot()) {
            long currentTime = System.currentTimeMillis();
            Long lastShot = lastShotTime.get(sessionId);
            
            if (lastShot == null || (currentTime - lastShot) >= SHOT_COOLDOWN) {
                shootBullet(sessionId, spaceship);
                lastShotTime.put(sessionId, currentTime);
            }
        }
        
        // Keep spaceship in bounds
        if (spaceship.getX() < 0) spaceship.setX(0);
        if (spaceship.getX() > GAME_WIDTH) spaceship.setX(GAME_WIDTH);
        if (spaceship.getY() < 0) spaceship.setY(0);
        if (spaceship.getY() > GAME_HEIGHT) spaceship.setY(GAME_HEIGHT);
    }
    
    private void shootBullet(String playerId, Spaceship spaceship) {
        double bulletSpeed = 8.0;
        double angle = spaceship.getAngle();
        double radians = Math.toRadians(angle);
        
        // Calculate bullet starting position (front of spaceship)
        double startX = spaceship.getX() + Math.cos(radians) * 25;
        double startY = spaceship.getY() + Math.sin(radians) * 25;
        
        Bullet bullet = new Bullet(
            UUID.randomUUID().toString(),
            playerId,
            startX,
            startY,
            angle,
            bulletSpeed
        );
        
        gameState.addBullet(bullet);
    }
    
    @Scheduled(fixedRate = 16) // ~60 FPS
    public void gameLoop() {
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;
        
        // Update game state
        updateGame(deltaTime);
        
        // Broadcast game state via Netty
        broadcastGameState();
    }
    
    private void updateGame(long deltaTime) {
        // Update bullets
        gameState.update(deltaTime);
        
        // Check collisions
        checkCollisions();
    }
    
    private void checkCollisions() {
        List<Bullet> bullets = new ArrayList<>(gameState.getBullets());
        
        for (Bullet bullet : bullets) {
            if (!bullet.isActive()) continue;
            
            // Check collision with spaceships
            for (Spaceship spaceship : gameState.getSpaceships().values()) {
                if (!spaceship.isAlive() || spaceship.getPlayerId().equals(bullet.getShooterId())) {
                    continue;
                }
                
                double dx = bullet.getX() - spaceship.getX();
                double dy = bullet.getY() - spaceship.getY();
                double distance = Math.sqrt(dx * dx + dy * dy);
                
                if (distance < 20) { // Collision radius
                    spaceship.takeDamage(10);
                    bullet.setActive(false);
                    
                    if (!spaceship.isAlive()) {
                        // Find shooter and give points
                        Spaceship shooter = gameState.getSpaceships().get(bullet.getShooterId());
                        if (shooter != null) {
                            shooter.addScore(100);
                        }
                        
                        // Check if game is over (only one player left alive)
                        checkGameOver();
                    }
                }
            }
        }
    }
    

    
    private void checkGameOver() {
        // Count alive players
        long aliveCount = gameState.getSpaceships().values().stream()
                .filter(Spaceship::isAlive)
                .count();
        
        if (aliveCount <= 1 && !gameState.isGameOver()) {
            // Game over - find winner
            Spaceship winner = gameState.getSpaceships().values().stream()
                    .filter(Spaceship::isAlive)
                    .findFirst()
                    .orElse(null);
            
            if (winner != null) {
                ClientSession winnerSession = sessions.get(winner.getPlayerId());
                if (winnerSession != null) {
                    gameState.setGameOver(true);
                    gameState.setWinnerId(winner.getPlayerId());
                    gameState.setWinnerUsername(winnerSession.getUsername());
                    
                    // Update leaderboard - winner gets a win
                    leaderboardService.updatePlayerStats(winnerSession.getUsername(), winner.getScore(), true);
                    
                    // Update leaderboard for all losers
                    for (Spaceship spaceship : gameState.getSpaceships().values()) {
                        if (!spaceship.isAlive() && !spaceship.getPlayerId().equals(winner.getPlayerId())) {
                            ClientSession loserSession = sessions.get(spaceship.getPlayerId());
                            if (loserSession != null) {
                                leaderboardService.updatePlayerStats(loserSession.getUsername(), spaceship.getScore(), false);
                            }
                        }
                    }
                    
                    System.out.println("Game Over! Winner: " + winnerSession.getUsername() + " with score: " + winner.getScore());
                    broadcastGameState(); // Broadcast final state
                }
            }
        }
    }
    
    public GameState getGameState() {
        return gameState;
    }
    
    private static class ClientSession {
        private String sessionId;
        private String username;
        
        public ClientSession(String sessionId, String username) {
            this.sessionId = sessionId;
            this.username = username;
        }
        
        public String getSessionId() { return sessionId; }
        public String getUsername() { return username; }
    }
}
