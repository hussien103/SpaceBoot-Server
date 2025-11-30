package org.example.model;

import java.io.Serializable;

public class PlayerInput implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean moveUp;
    private boolean moveDown;
    private boolean moveLeft;
    private boolean moveRight;
    private boolean rotateLeft;
    private boolean rotateRight;
    private boolean shoot;
    
    public PlayerInput() {
        this.moveUp = false;
        this.moveDown = false;
        this.moveLeft = false;
        this.moveRight = false;
        this.rotateLeft = false;
        this.rotateRight = false;
        this.shoot = false;
    }
    
    // Getters and Setters
    public boolean isMoveUp() { return moveUp; }
    public void setMoveUp(boolean moveUp) { this.moveUp = moveUp; }
    
    public boolean isMoveDown() { return moveDown; }
    public void setMoveDown(boolean moveDown) { this.moveDown = moveDown; }
    
    public boolean isMoveLeft() { return moveLeft; }
    public void setMoveLeft(boolean moveLeft) { this.moveLeft = moveLeft; }
    
    public boolean isMoveRight() { return moveRight; }
    public void setMoveRight(boolean moveRight) { this.moveRight = moveRight; }
    
    public boolean isRotateLeft() { return rotateLeft; }
    public void setRotateLeft(boolean rotateLeft) { this.rotateLeft = rotateLeft; }
    
    public boolean isRotateRight() { return rotateRight; }
    public void setRotateRight(boolean rotateRight) { this.rotateRight = rotateRight; }
    
    public boolean isShoot() { return shoot; }
    public void setShoot(boolean shoot) { this.shoot = shoot; }
}

