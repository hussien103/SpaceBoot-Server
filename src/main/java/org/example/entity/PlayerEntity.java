package org.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "players")
public class PlayerEntity {
    @Id
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(name = "total_score")
    private int totalScore = 0;
    
    @Column(name = "games_played")
    private int gamesPlayed = 0;
    
    @Column(name = "wins")
    private int wins = 0;
    
    public PlayerEntity() {
    }
    
    public PlayerEntity(String username, String password) {
        this.username = username;
        this.password = password;
        this.totalScore = 0;
        this.gamesPlayed = 0;
        this.wins = 0;
    }
    
    public void addGameResult(int score, boolean won) {
        this.gamesPlayed++;
        this.totalScore += score;
        if (won) {
            this.wins++;
        }
    }
    
    // Getters and Setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public int getTotalScore() {
        return totalScore;
    }
    
    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }
    
    public int getGamesPlayed() {
        return gamesPlayed;
    }
    
    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }
    
    public int getWins() {
        return wins;
    }
    
    public void setWins(int wins) {
        this.wins = wins;
    }
}

