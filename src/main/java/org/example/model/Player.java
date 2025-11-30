package org.example.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public class Player implements Serializable, UserDetails {
    private static final long serialVersionUID = 1L;
    
    private String username;
    private String password;
    private int totalScore;
    private int gamesPlayed;
    private int wins;
    
    public Player(String username, String password) {
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
    @Override
    public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void setUsername(String username) { this.username = username; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }
    @Override
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }
    
    public int getGamesPlayed() { return gamesPlayed; }
    public void setGamesPlayed(int gamesPlayed) { this.gamesPlayed = gamesPlayed; }
    
    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }
}

