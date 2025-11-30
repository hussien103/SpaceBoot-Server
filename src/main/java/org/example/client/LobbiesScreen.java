package org.example.client;


import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.example.model.Lobby;

import java.util.ArrayList;
import java.util.List;

public class LobbiesScreen {

    private GameClient gameClient;
    private Stage stage;
    private List<Lobby> lobbies;

    public LobbiesScreen(GameClient gameClient, Stage stage) {
        this.gameClient = gameClient;
        this.stage = stage;
        this.lobbies = new ArrayList<>();
    }

    public Scene createScene() {
        // ---------- TOP LABEL ----------
        Label titleLabel = new Label("Available Lobbies");
        titleLabel.setFont(Font.font(20));

        // ---------- LIST OF LOBBIES ----------
        ListView<String> lobbiesListView = new ListView<>();

        // Update list when server sends lobby data
        gameClient.setLobbyCreatedListener(lobby -> {
            Platform.runLater(() -> {
                this.lobbies.add(lobby);
                lobbiesListView.getItems().clear();
                for (Lobby lobby1 : lobbies) {
                    lobbiesListView.getItems().add(
                            lobby1.getName() + " (" + lobby1.getEntries().size() + " players)"
                    );
                }
            });
        });

        VBox listBox = new VBox(10, titleLabel, lobbiesListView);
        listBox.setPadding(new Insets(15));
        listBox.setPrefWidth(300);

        // ---------- RIGHT PANEL: ACTIONS ----------
        Label actionsLabel = new Label("Actions");
        actionsLabel.setFont(Font.font(18));

        Button createLobbyBtn = new Button("Create Lobby");
        createLobbyBtn.setMaxWidth(Double.MAX_VALUE);
//        createLobbyBtn.setOnAction(e -> {
//            // send create lobby request to server
//            gameClient.sendCreateLobbyRequest();
//        });

        Button joinLobbyBtn = new Button("Join Lobby");
        joinLobbyBtn.setMaxWidth(Double.MAX_VALUE);
//        joinLobbyBtn.setOnAction(e -> {
//            int selectedIndex = lobbiesListView.getSelectionModel().getSelectedIndex();
//            if (selectedIndex >= 0) {
//                Lobby selectedLobby = lobbies.get(selectedIndex);
//                gameClient.sendJoinLobbyRequest(selectedLobby.getId());
//            } else {
//                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a lobby to join!");
//                alert.showAndWait();
//            }
//        });

        VBox actionsBox = new VBox(15, actionsLabel, createLobbyBtn, joinLobbyBtn);
        actionsBox.setPadding(new Insets(15));
        actionsBox.setAlignment(Pos.TOP_CENTER);
        actionsBox.setPrefWidth(200);

        // ---------- MAIN LAYOUT ----------
        BorderPane layout = new BorderPane();
        layout.setLeft(listBox);
        layout.setRight(actionsBox);

        Scene scene = new Scene(layout, 700, 400);
        stage.setTitle("Game Lobbies");
        stage.setScene(scene);
        stage.show();

        return scene;
    }
}
