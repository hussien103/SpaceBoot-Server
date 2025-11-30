package org.example.client;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.example.model.LobbyEntry;

import java.util.List;

public class LobbyScreen  {

    private GameClient gameClient;
    private Stage stage;
    private List<LobbyEntry> LobbyEntries;
    public LobbyScreen(GameClient gameClient,Stage stage) {
        this.gameClient = gameClient;
        this.stage = stage;
    }

    public Scene createScene(){
        // ---------- LEFT SIDE: PLAYER LIST ----------
        Label playersLabel = new Label("Players");
        playersLabel.setFont(Font.font(18));
        ListView<String> playersList = new ListView<>();
        gameClient.setLobbyListener(LobbyEntries -> {
            Platform.runLater(() -> {
                this.LobbyEntries = LobbyEntries;
                for(LobbyEntry entry: LobbyEntries) {
                    playersList.getItems().add(entry.getUsername());
                }
            });
        });


        VBox leftPane = new VBox(10, playersLabel, playersList);
        leftPane.setPadding(new Insets(15));
        leftPane.setPrefWidth(200);


        // ---------- CENTER: CHAT ----------
        Label chatLabel = new Label("Lobby Chat");
        chatLabel.setFont(Font.font(18));

        TextArea chatArea = new TextArea();
        chatArea.setEditable(false);

        TextField messageField = new TextField();
        messageField.setPromptText("Type your message...");

        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> {
            if (!messageField.getText().isEmpty()) {
                chatArea.appendText("You: " + messageField.getText() + "\n");
                messageField.clear();
            }
        });

        HBox sendBox = new HBox(10, messageField, sendButton);
        sendBox.setAlignment(Pos.CENTER);

        VBox centerPane = new VBox(10, chatLabel, chatArea, sendBox);
        centerPane.setPadding(new Insets(15));


        // ---------- RIGHT SIDE: GAME SETTINGS ----------
        Label settingsLabel = new Label("Game Settings");
        settingsLabel.setFont(Font.font(18));

        ComboBox<String> mapSelector = new ComboBox<>();
        mapSelector.getItems().addAll("Desert", "Forest", "Snow");
        mapSelector.setValue("Desert");

        ComboBox<String> modeSelector = new ComboBox<>();
        modeSelector.getItems().addAll("Deathmatch", "Capture the Flag", "Co-op");
        modeSelector.setValue("Deathmatch");

        Button readyButton = new Button("Ready");
        readyButton.setStyle("-fx-font-size: 16px; -fx-padding: 10 20;");

        VBox rightPane = new VBox(15,
                settingsLabel,
                new Label("Select Map:"), mapSelector,
                new Label("Select Mode:"), modeSelector,
                readyButton
        );
        rightPane.setAlignment(Pos.TOP_CENTER);
        rightPane.setPadding(new Insets(15));
        rightPane.setPrefWidth(200);


        // ---------- MAIN LAYOUT ----------
        BorderPane layout = new BorderPane();
        layout.setLeft(leftPane);
        layout.setCenter(centerPane);
        layout.setRight(rightPane);

        Scene scene = new Scene(layout, 800, 500);
        stage.setTitle("Game Lobby");
        stage.setScene(scene);
        stage.show();
        return scene;
    }

}
