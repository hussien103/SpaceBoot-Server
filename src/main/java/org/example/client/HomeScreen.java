package org.example.client;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HomeScreen {

    private GameClient gameClient;
    private Stage stage;

    public HomeScreen(GameClient gameClient, Stage stage) {
        this.gameClient = gameClient;
        this.stage = stage;
    }

    public Scene createScene() {
        // ---------- TITLE BUTTON ----------
        Button lobbiesButton = new Button("Show Lobbies");
        lobbiesButton.setStyle("-fx-font-size: 16px; -fx-padding: 10 20;");
        lobbiesButton.setOnAction(e -> {
            // Open the Lobbies screen
            LobbiesScreen lobbiesScreen = new LobbiesScreen(gameClient, stage);
            lobbiesScreen.createScene();
        });

        // Optional: You can add more buttons here for Profile, Settings, etc.

        VBox layout = new VBox(20, lobbiesButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(50));

        Scene scene = new Scene(layout, 400, 300);
        stage.setTitle("Home Screen");
        stage.setScene(scene);
        stage.show();

        return scene;
    }
}
