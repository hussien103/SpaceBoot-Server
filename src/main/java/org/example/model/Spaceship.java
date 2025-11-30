package org.example.model;

import java.io.Serializable;

public class Spaceship implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String playerId;
    private double x;
    private double y;
    private double angle;
    private int health;
    private int score;
    private boolean alive;
    
    // Default constructor for Jackson
    public Spaceship() {
        this.angle = 0;
        this.health = 100;
        this.score = 0;
        this.alive = true;
    }
    
    public Spaceship(String playerId, double x, double y) {
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.angle = 0;
        this.health = 100;
        this.score = 0;
        this.alive = true;
    }
    
    public void move(double deltaX, double deltaY) {
        this.x += deltaX;
        this.y += deltaY;
    }
    
    public void rotate(double deltaAngle) {
        this.angle += deltaAngle;
        if (this.angle < 0) this.angle += 360;
        if (this.angle >= 360) this.angle -= 360;
    }
    
    public void takeDamage(int damage) {
        this.health -= damage;
        if (this.health <= 0) {
            this.health = 0;
            this.alive = false;
        }
    }
    
    public void addScore(int points) {
        this.score += points;
    }
    
    // Getters and Setters
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    
    public double getAngle() { return angle; }
    public void setAngle(double angle) { this.angle = angle; }
    
    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }
    
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    
    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }
}

