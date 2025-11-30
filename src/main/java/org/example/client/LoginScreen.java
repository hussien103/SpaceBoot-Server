package org.example.client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.Map;

public class LoginScreen {
    private Stage stage;
    private RestClient restClient;
    private GameClient gameClient;
    private java.util.function.Consumer<String> onLoginSuccess;
    
    public LoginScreen(Stage stage, RestClient restClient, GameClient gameClient, java.util.function.Consumer<String> onLoginSuccess) {
        this.stage = stage;
        this.restClient = restClient;
        this.gameClient = gameClient;
        this.onLoginSuccess = onLoginSuccess;
    }
    
    public Scene createScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #1a1a2e;");
        
        Label title = new Label("Spaceship Battle");
        title.setFont(Font.font("Arial", 36));
        title.setTextFill(Color.WHITE);
        
        Label subtitle = new Label("Multiplayer Game");
        subtitle.setFont(Font.font("Arial", 18));
        subtitle.setTextFill(Color.LIGHTGRAY);
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(300);
        usernameField.setPrefHeight(40);
        usernameField.setStyle("-fx-font-size: 14px;");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(300);
        passwordField.setPrefHeight(40);
        passwordField.setStyle("-fx-font-size: 14px;");
        
        Label statusLabel = new Label();
        statusLabel.setTextFill(Color.RED);
        statusLabel.setVisible(false);
        
        Button loginButton = new Button("Login");
        loginButton.setPrefWidth(300);
        loginButton.setPrefHeight(40);
        loginButton.setStyle("-fx-font-size: 16px; -fx-background-color: #0f3460; -fx-text-fill: white;");
        
        Button registerButton = new Button("Register");
        registerButton.setPrefWidth(300);
        registerButton.setPrefHeight(40);
        registerButton.setStyle("-fx-font-size: 16px; -fx-background-color: #16213e; -fx-text-fill: white;");
        
        Button leaderboardButton = new Button("View Leaderboard");
        leaderboardButton.setPrefWidth(300);
        leaderboardButton.setPrefHeight(40);
        leaderboardButton.setStyle("-fx-font-size: 16px; -fx-background-color: #533483; -fx-text-fill: white;");
        
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            
            if (username.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Please enter username and password");
                statusLabel.setVisible(true);
                return;
            }
            
            new Thread(() -> {
                Map<String, Object> response = restClient.login(username, password);
                javafx.application.Platform.runLater(() -> {
                    Boolean success = (Boolean) response.get("success");
                    if (Boolean.TRUE.equals(success)) {
                        // Pass username to callback
                        onLoginSuccess.accept(username);
                    } else {
                        statusLabel.setText("Login failed: " + response.get("message"));
                        statusLabel.setTextFill(Color.RED);
                        statusLabel.setVisible(true);
                    }
                });
            }).start();
        });
        
        registerButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            
            if (username.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Please enter username and password");
                statusLabel.setVisible(true);
                return;
            }
            
            new Thread(() -> {
                Map<String, Object> response = restClient.register(username, password);
                javafx.application.Platform.runLater(() -> {
                    Boolean success = (Boolean) response.get("success");
                    if (Boolean.TRUE.equals(success)) {
                        statusLabel.setText("Registration successful! You can now login.");
                        statusLabel.setTextFill(Color.GREEN);
                        statusLabel.setVisible(true);
                    } else {
                        statusLabel.setText("Registration failed: " + response.get("message"));
                        statusLabel.setTextFill(Color.RED);
                        statusLabel.setVisible(true);
                    }
                });
            }).start();
        });

        final LeaderboardScreen[] leaderboardRef = new LeaderboardScreen[1];

        leaderboardButton.setOnAction(e -> {
            leaderboardRef[0] = new LeaderboardScreen(stage, restClient, gameClient, () -> {
                leaderboardRef[0].stop();
                stage.setScene(createScene());
            });

            stage.setScene(leaderboardRef[0].createScene());
        });

        root.getChildren().addAll(title, subtitle, usernameField, passwordField, statusLabel, 
                                 loginButton, registerButton, leaderboardButton);
        
        return new Scene(root, 600, 500);
    }
}

