package org.example.widgets;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.text.Font;

public class NotificationWidget {

    public static void showNotification(String message, Stage owner) {
        Platform.runLater(() -> {
            Popup popup = new Popup();

            Label label = new Label(message);
            label.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-padding: 10px; -fx-background-radius: 5;");
            label.setFont(new Font("Arial", 14));
            label.setAlignment(Pos.CENTER);

            StackPane pane = new StackPane(label);
            pane.setStyle("-fx-background-radius: 5; -fx-padding: 5;");
            popup.getContent().add(pane);

            // Position at top center
            double popupWidth = 250; // approximate width of popup
            double x = owner.getX() + (owner.getWidth() - popupWidth) / 2;
            double y = owner.getY() + 20; // 20 px from top
            popup.show(owner, x, y);

            // Slide down animation (from top)
            TranslateTransition tt = new TranslateTransition(Duration.millis(500), pane);
            tt.setFromY(-50);
            tt.setToY(0);

            // Fade in
            FadeTransition ft = new FadeTransition(Duration.millis(500), pane);
            ft.setFromValue(0);
            ft.setToValue(1);

            tt.play();
            ft.play();

            // Auto hide after 3 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    Platform.runLater(() -> {
                        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), pane);
                        fadeOut.setFromValue(1);
                        fadeOut.setToValue(0);
                        fadeOut.setOnFinished(e -> popup.hide());
                        fadeOut.play();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }
}
