package org.example.client;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.example.model.LeaderboardEntry;

import java.util.List;
import java.util.function.Consumer;

public class LeaderboardScreen {
    private Stage stage;
    private RestClient restClient;
    private GameClient gameClient;
    private Runnable onBack;
    private TableView<LeaderboardEntry> tableView;
    private AnimationTimer refreshTimer;
    private long lastRefreshTime;
    private static final long REFRESH_INTERVAL = 2000; // Refresh every 2 seconds (fallback)
    private boolean usingWebSocket;
    private Consumer<List<LeaderboardEntry>> leaderboardListener;
    
    public LeaderboardScreen(Stage stage, RestClient restClient, GameClient gameClient, Runnable onBack) {
        this.stage = stage;
        this.restClient = restClient;
        this.gameClient = gameClient;
        this.onBack = onBack;
        this.lastRefreshTime = System.currentTimeMillis();
        this.usingWebSocket = gameClient != null && gameClient.isConnected();
    }
    
    public Scene createScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #1a1a2e;");
        
        Label title = new Label("Leaderboard");
        title.setFont(Font.font("Arial", 36));
        title.setTextFill(Color.WHITE);
        
        tableView = new TableView<>();
        tableView.setPrefWidth(600);
        tableView.setPrefHeight(400);
        tableView.setStyle("-fx-background-color: #16213e;");
        
        TableColumn<LeaderboardEntry, String> rankColumn = new TableColumn<>("Rank");
        rankColumn.setCellValueFactory(cellData -> {
            int rank = tableView.getItems().indexOf(cellData.getValue()) + 1;
            return new javafx.beans.property.SimpleStringProperty(String.valueOf(rank));
        });
        rankColumn.setPrefWidth(80);
        
        TableColumn<LeaderboardEntry, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameColumn.setPrefWidth(200);
        
        TableColumn<LeaderboardEntry, Integer> scoreColumn = new TableColumn<>("Total Score");
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("totalScore"));
        scoreColumn.setPrefWidth(150);
        
        TableColumn<LeaderboardEntry, Integer> winsColumn = new TableColumn<>("Wins");
        winsColumn.setCellValueFactory(new PropertyValueFactory<>("wins"));
        winsColumn.setPrefWidth(100);
        
        TableColumn<LeaderboardEntry, Integer> gamesColumn = new TableColumn<>("Games");
        gamesColumn.setCellValueFactory(new PropertyValueFactory<>("gamesPlayed"));
        gamesColumn.setPrefWidth(100);
        
        tableView.getColumns().addAll(rankColumn, usernameColumn, scoreColumn, winsColumn, gamesColumn);
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setPrefWidth(200);
        refreshButton.setPrefHeight(40);
        refreshButton.setStyle("-fx-font-size: 16px; -fx-background-color: #0f3460; -fx-text-fill: white;");
        
        Button backButton = new Button("Back");
        backButton.setPrefWidth(200);
        backButton.setPrefHeight(40);
        backButton.setStyle("-fx-font-size: 16px; -fx-background-color: #533483; -fx-text-fill: white;");
        
        refreshButton.setOnAction(e -> refreshLeaderboard());
        backButton.setOnAction(e -> {
            stop();
            onBack.run();
        });
        
        // Request leaderboard on load
        if (usingWebSocket) {
            leaderboardListener = entries -> Platform.runLater(() -> updateTable(entries));
            gameClient.addLeaderboardListener(leaderboardListener);
            List<LeaderboardEntry> latest = gameClient.getLatestLeaderboard();
            if (!latest.isEmpty()) {
                updateTable(latest);
            } else {
                refreshLeaderboard(); // initial load via REST
            }
        } else {
            refreshLeaderboard();
            
            // Auto-refresh leaderboard every 2 seconds (fallback when WebSocket not available)
            refreshTimer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastRefreshTime >= REFRESH_INTERVAL) {
                        refreshLeaderboard();
                        lastRefreshTime = currentTime;
                    }
                }
            };
            refreshTimer.start();
        }
        
        root.getChildren().addAll(title, tableView, refreshButton, backButton);
        
        return new Scene(root, 700, 600);
    }
    
    private void refreshLeaderboard() {
        new Thread(() -> {
            List<LeaderboardEntry> entries = restClient.getLeaderboard();
            Platform.runLater(() -> {
                updateTable(entries);
            });
        }).start();
    }
    
    private void updateTable(List<LeaderboardEntry> entries) {
        tableView.getItems().clear();
        if (entries != null && !entries.isEmpty()) {
            tableView.getItems().addAll(entries);
            System.out.println("Updated leaderboard with " + entries.size() + " entries");
        } else {
            System.out.println("No leaderboard entries received");
        }
    }
    
    public void stop() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        if (usingWebSocket && leaderboardListener != null && gameClient != null) {
            gameClient.removeLeaderboardListener(leaderboardListener);
        }
    }
}

