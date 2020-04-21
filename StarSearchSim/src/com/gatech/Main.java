package com.gatech;

/*
CS6310 Software Architecture and Design
Term: Spring 2020
Project: Star Search Simulation System
Author: Group 25
            (Sungin Baek sbaek33@gatech.edu,
            Leonardo Gabriel Puy lpuy3@gatech.edu,
            Nathaniel Yohannesnyohannes3@gatech.edu,
            Ye Shen yshen61@gatech.edu,
            Mahesh Chandra Sehalli Jagadish mjagadish3@gatech.edu)

*/

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("View/main.fxml"));
        primaryStage.setTitle("Star Search Simulator");
        primaryStage.setScene(new Scene(root, 500, 500));
        primaryStage.setMaximized(true);
        primaryStage.getIcons().add(new Image("com/gatech/Images/app.jpg"));
        //primaryStage.initModality(Modality.APPLICATION_MODAL);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
