package com.fitlife;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // ✅ Load the login/start screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainApp.fxml"));
        Parent root = loader.load();

        // ✅ Create scene with exact preferred size from FXML
        Scene scene = new Scene(root, 1220, 720);

        // ✅ Stage setup
        stage.setTitle("FitLife Gym Management System");
        stage.setScene(scene);
        stage.setResizable(false); // Optional: lock size for consistent layout
        stage.centerOnScreen();    // Centers the window on screen
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
