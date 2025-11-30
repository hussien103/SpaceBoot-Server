package org.example.model;

import org.example.entity.PlayerEntity;

public class LobbyEntry {


    private String username;
    private int totalScore;

    public LobbyEntry(){

    }
    public LobbyEntry(PlayerEntity player) {
        this.username = player.getUsername();
        this.totalScore = getTotalScore();
    }
    public String getUsername() {
        return this.username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public int getTotalScore() {
        return totalScore;
    }
    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

}
