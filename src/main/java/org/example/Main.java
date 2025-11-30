package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import org.example.client.*;

public class Main extends Application {
    private Stage primaryStage;
    private RestClient restClient;
    private GameClient gameClient;
    private String currentUsername;
    
    public static void main(String[] args) {
        // Launch JavaFX application
        // Note: Spring Boot server should be started separately using SpaceshipBattleApplication
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Spaceship Battle - Multiplayer Game");
        
        // Create REST client and game client
        restClient = new RestClient();
        gameClient = new GameClient();
        
        // Show login screen
        showLoginScreen();
        
        primaryStage.show();
    }
    
    private void showLoginScreen() {
        LoginScreen loginScreen = new LoginScreen(primaryStage, restClient, gameClient, (username) -> {
            currentUsername = username;
            System.out.println("Login successful for: " + currentUsername);
            if (gameClient.connect(username)) {
                showHomeScreen();
            } else {
                Alert alert = new Alert(AlertType.ERROR, "Failed to connect to game server via WebSocket.");
                alert.showAndWait();
            }
        });
        primaryStage.setScene(loginScreen.createScene());
    }
    
    private void showGameScreen() {
        GameScreen gameScreen = new GameScreen(primaryStage, gameClient, () -> {
            // On game over, return to login
            gameClient.stop();
            currentUsername = null;
            showLoginScreen();
        });
        primaryStage.setScene(gameScreen.createScene());
    }
    private void showLobbyScreen(){
        LobbyScreen lobbyScreen = new LobbyScreen(gameClient,primaryStage);
        primaryStage.setScene(lobbyScreen.createScene());
    }
    private void showLobbiesScreen(){
        LobbiesScreen lobbiesScreen = new LobbiesScreen(gameClient,primaryStage);
        primaryStage.setScene(lobbiesScreen.createScene());
    }
    private void showHomeScreen(){
        HomeScreen homeScreen = new HomeScreen(gameClient,primaryStage);
        primaryStage.setScene(homeScreen.createScene());
    }
    @Override
    public void stop() {
        if (gameClient != null) {
            gameClient.stop();
        }
    }
}
