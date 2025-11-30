package org.example.client;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.example.model.*;
import javafx.application.Platform;
import org.example.widgets.NotificationWidget;

import java.util.Map;

public class GameScreen {
    private Stage stage;
    private GameClient gameClient;
    private Canvas canvas;
    private GraphicsContext gc;
    private GameState gameState;
    private PlayerInput currentInput;
    private String myPlayerId;
    private AnimationTimer gameLoop;
    private Runnable onGameOver;

    private boolean gameOverHandled;
    
    public GameScreen(Stage stage, GameClient gameClient, Runnable onGameOver) {
        this.stage = stage;
        this.gameClient = gameClient;
        this.onGameOver = onGameOver;
        this.currentInput = new PlayerInput();
        this.gameState = new GameState();
        this.gameOverHandled = false;
        this.myPlayerId = null;

    }
    
    public void setPlayerId(String playerId) {
        this.myPlayerId = playerId;
    }
    
    public Scene createScene() {
        canvas = new Canvas(800, 600);
        gc = canvas.getGraphicsContext2D();
        
        StackPane root = new StackPane(canvas);
        root.setStyle("-fx-background-color: #000000;");
        
        Scene scene = new Scene(root, 800, 600);
        
        // Handle keyboard input
        scene.setOnKeyPressed(this::handleKeyPressed);
        scene.setOnKeyReleased(this::handleKeyReleased);
        
        // Handle game state updates from server
        gameClient.setGameStateListener(gameState -> {
            Platform.runLater(() -> {
                this.gameState = gameState;
                if (gameState != null) {
//                        System.out.println("Game state updated - Spaceships: " + gameState.getSpaceships().size() +
//                                         ", Bullets: " + gameState.getBullets().size());
                    
                    // Check if game is over
                    if (gameState.isGameOver() && !gameOverHandled) {
                        handleGameOver(gameState);
                        gameOverHandled = true;
                    }
                }
            });
        });
        gameClient.setNotificationListener(notification -> {
            // Assuming 'primaryStage' is your main JavaFX stage
            NotificationWidget.showNotification(notification.toString(), this.stage);
        });

        gameClient.setPlayerIdListener(id -> Platform.runLater(() -> this.myPlayerId = id));
        
        // Start game loop
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render();
            }
        };
        gameLoop.start();
        
        return scene;
    }
    
    private void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        
        if (code == KeyCode.W || code == KeyCode.UP) {
            currentInput.setMoveUp(true);
        }
        if (code == KeyCode.S || code == KeyCode.DOWN) {
            currentInput.setMoveDown(true);
        }
        if (code == KeyCode.A || code == KeyCode.LEFT) {
            currentInput.setMoveLeft(true);
        }
        if (code == KeyCode.D || code == KeyCode.RIGHT) {
            currentInput.setMoveRight(true);
        }
        if (code == KeyCode.Q) {
            currentInput.setRotateLeft(true);
        }
        if (code == KeyCode.E) {
            currentInput.setRotateRight(true);
        }
        if (code == KeyCode.SPACE) {
            currentInput.setShoot(true);
        }
        
        gameClient.sendPlayerInput(currentInput);
    }
    
    private void handleKeyReleased(KeyEvent event) {
        KeyCode code = event.getCode();
        
        if (code == KeyCode.W || code == KeyCode.UP) {
            currentInput.setMoveUp(false);
        }
        if (code == KeyCode.S || code == KeyCode.DOWN) {
            currentInput.setMoveDown(false);
        }
        if (code == KeyCode.A || code == KeyCode.LEFT) {
            currentInput.setMoveLeft(false);
        }
        if (code == KeyCode.D || code == KeyCode.RIGHT) {
            currentInput.setMoveRight(false);
        }
        if (code == KeyCode.Q) {
            currentInput.setRotateLeft(false);
        }
        if (code == KeyCode.E) {
            currentInput.setRotateRight(false);
        }
        if (code == KeyCode.SPACE) {
            currentInput.setShoot(false);
        }
        
        gameClient.sendPlayerInput(currentInput);
    }

    private long lastSent = 0;

    public void update() {
        long now = System.currentTimeMillis();
        if (now - lastSent >= 50) {  // 20 times per second
            gameClient.sendPlayerInput(currentInput);
            lastSent = now;
        }
    }
    private void render() {
        // Clear canvas
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 800, 600);
        
        // Draw stars background
        gc.setFill(Color.WHITE);
        for (int i = 0; i < 100; i++) {
            double x = (i * 37) % 800;
            double y = (i * 53) % 600;
            gc.fillOval(x, y, 2, 2);
        }
        
        // Draw debug info if gameState is null
        if (gameState == null) {
            gc.setFill(Color.YELLOW);
            gc.setFont(Font.font(20));
            gc.fillText("Waiting for game state...", 300, 300);
            gc.setFont(Font.font(12));
            gc.setFill(Color.WHITE);
            gc.fillText("Player ID: " + (myPlayerId != null ? myPlayerId : "Not set"), 10, 100);
            return;
        }
        
        // Draw spaceships
        if (gameState.getSpaceships().isEmpty()) {
            gc.setFill(Color.YELLOW);
            gc.setFont(Font.font(20));
            gc.fillText("No players in game", 300, 300);
            return;
        }
        
        for (Spaceship spaceship : gameState.getSpaceships().values()) {
            if (!spaceship.isAlive()) continue;
            
            double x = spaceship.getX();
            double y = spaceship.getY();
            double angle = spaceship.getAngle();
            
            // Draw spaceship
            gc.save();
            gc.translate(x, y);
            gc.rotate(angle);
            
            // Draw triangle spaceship (bigger and more visible)
            if (myPlayerId != null && spaceship.getPlayerId().equals(myPlayerId)) {
                gc.setFill(Color.CYAN);
            } else {
                gc.setFill(Color.ORANGE);
            }
            
            // Larger spaceship
            double[] xPoints = {0, -20, 20};
            double[] yPoints = {-25, 15, 15};
            gc.fillPolygon(xPoints, yPoints, 3);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(3);
            gc.strokePolygon(xPoints, yPoints, 3);
            
            // Draw a circle at center for visibility
            gc.setFill(Color.WHITE);
            gc.fillOval(-3, -3, 6, 6);
            
            gc.restore();
            
            // Draw health bar
            double barWidth = 40;
            double barHeight = 5;
            gc.setFill(Color.RED);
            gc.fillRect(x - barWidth/2, y - 35, barWidth, barHeight);
            gc.setFill(Color.GREEN);
            gc.fillRect(x - barWidth/2, y - 35, barWidth * (spaceship.getHealth() / 100.0), barHeight);
            
            // Draw score
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(12));
            gc.fillText("Score: " + spaceship.getScore(), x - 30, y - 40);
        }
        
        // Draw bullets
        gc.setFill(Color.YELLOW);
        for (Bullet bullet : gameState.getBullets()) {
            if (bullet.isActive()) {
                gc.fillOval(bullet.getX() - 3, bullet.getY() - 3, 6, 6);
            }
        }
        
        // Draw HUD
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(16));
        Spaceship mySpaceship = myPlayerId != null ? gameState.getSpaceships().get(myPlayerId) : null;
        if (mySpaceship != null) {
            gc.fillText("Health: " + mySpaceship.getHealth(), 10, 30);
            gc.fillText("Score: " + mySpaceship.getScore(), 10, 50);
            gc.fillText("Players: " + gameState.getSpaceships().size(), 10, 70);
        } else {
            gc.fillText("Players: " + gameState.getSpaceships().size(), 10, 30);
            if (myPlayerId != null) {
                gc.setFont(Font.font(12));
                gc.fillText("Player ID: " + myPlayerId, 10, 50);
            }
        }
        
        // Draw game over message
        if (gameState != null && gameState.isGameOver()) {
            gc.setFill(Color.YELLOW);
            gc.setFont(Font.font(36));
            String message = "GAME OVER!";
            if (gameState.getWinnerUsername() != null) {
                if (myPlayerId != null && gameState.getWinnerId() != null && gameState.getWinnerId().equals(myPlayerId)) {
                    message = "YOU WIN!";
                    gc.setFill(Color.GREEN);
                } else {
                    message = gameState.getWinnerUsername() + " WINS!";
                    gc.setFill(Color.RED);
                }
            }
            gc.fillText(message, 250, 250);
            gc.setFont(Font.font(20));
            gc.setFill(Color.WHITE);
            gc.fillText("Returning to login...", 280, 300);
        }
        
        // Draw controls
        gc.setFont(Font.font(12));
        gc.fillText("Controls: W/A/S/D - Move, Q/E - Rotate, SPACE - Shoot", 10, 580);
    }
    
    private void handleGameOver(GameState gameState) {
        if (gameState.isGameOver() && onGameOver != null) {
            // Wait a bit to show the game over message, then return to login
            new Thread(() -> {
                try {
                    Thread.sleep(3000); // Show message for 3 seconds
                    Platform.runLater(() -> {
                        if (onGameOver != null) {
                            onGameOver.run();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    
    public void stop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        if (gameClient != null) {
            gameClient.stop();
        }
    }
}

