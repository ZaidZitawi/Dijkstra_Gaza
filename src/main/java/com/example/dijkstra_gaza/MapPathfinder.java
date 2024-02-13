package com.example.dijkstra_gaza;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MapPathfinder extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("hello-view.fxml")));
        primaryStage.getIcons().add(new Image("file:palestine.png"));
        primaryStage.setTitle("Gaza Map");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }


}
