package org.example.model;

import java.io.Serializable;

public class Bullet implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String shooterId;
    private double x;
    private double y;
    private double velocityX;
    private double velocityY;
    private boolean active;
    
    // Default constructor for Jackson
    public Bullet() {
        this.active = true;
    }
    
    public Bullet(String id, String shooterId, double x, double y, double angle, double speed) {
        this.id = id;
        this.shooterId = shooterId;
        this.x = x;
        this.y = y;
        double radians = Math.toRadians(angle);
        this.velocityX = Math.cos(radians) * speed;
        this.velocityY = Math.sin(radians) * speed;
        this.active = true;
    }
    
    public void update() {
        this.x += velocityX;
        this.y += velocityY;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getShooterId() { return shooterId; }
    public void setShooterId(String shooterId) { this.shooterId = shooterId; }
    
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    
    public double getVelocityX() { return velocityX; }
    public void setVelocityX(double velocityX) { this.velocityX = velocityX; }
    
    public double getVelocityY() { return velocityY; }
    public void setVelocityY(double velocityY) { this.velocityY = velocityY; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}

