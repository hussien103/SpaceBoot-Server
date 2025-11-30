package org.example.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Map<String, Spaceship> spaceships;
    private List<Bullet> bullets;
    private long gameTime;
    private boolean gameOver;
    private String winnerId;
    private String winnerUsername;
    
    public GameState() {
        this.spaceships = new HashMap<>();
        this.bullets = new ArrayList<>();
        this.gameTime = 0;
        this.gameOver = false;
    }
    
    public void update(long deltaTime) {
        this.gameTime += deltaTime;
        
        // Update bullets
        bullets.removeIf(bullet -> {
            bullet.update();
            // Remove bullets that are out of bounds
            return !bullet.isActive() || 
                   bullet.getX() < 0 || bullet.getX() > 800 ||
                   bullet.getY() < 0 || bullet.getY() > 600;
        });
    }
    
    public void addSpaceship(Spaceship spaceship) {
        spaceships.put(spaceship.getPlayerId(), spaceship);
    }
    
    public void removeSpaceship(String playerId) {
        spaceships.remove(playerId);
    }
    
    public void addBullet(Bullet bullet) {
        bullets.add(bullet);
    }
    
    public void removeBullet(Bullet bullet) {
        bullets.remove(bullet);
    }
    
    // Getters and Setters
    public Map<String, Spaceship> getSpaceships() { return spaceships; }
    public void setSpaceships(Map<String, Spaceship> spaceships) { this.spaceships = spaceships; }
    
    public List<Bullet> getBullets() { return bullets; }
    public void setBullets(List<Bullet> bullets) { this.bullets = bullets; }
    
    public long getGameTime() { return gameTime; }
    public void setGameTime(long gameTime) { this.gameTime = gameTime; }
    
    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }
    
    public String getWinnerId() { return winnerId; }
    public void setWinnerId(String winnerId) { this.winnerId = winnerId; }
    
    public String getWinnerUsername() { return winnerUsername; }
    public void setWinnerUsername(String winnerUsername) { this.winnerUsername = winnerUsername; }
}

