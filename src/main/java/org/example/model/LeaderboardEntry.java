package org.example.model;

import java.io.Serializable;

public class LeaderboardEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String username;
    private int totalScore;
    private int wins;
    private int gamesPlayed;
    
    // Default constructor for Jackson
    public LeaderboardEntry() {
    }
    
    public LeaderboardEntry(String username, int totalScore, int wins, int gamesPlayed) {
        this.username = username;
        this.totalScore = totalScore;
        this.wins = wins;
        this.gamesPlayed = gamesPlayed;
    }
    
    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }
    
    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }
    
    public int getGamesPlayed() { return gamesPlayed; }
    public void setGamesPlayed(int gamesPlayed) { this.gamesPlayed = gamesPlayed; }
}

