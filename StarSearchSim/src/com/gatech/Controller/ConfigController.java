/*
CS6310 Software Architecture and Design
Term: Spring 2020
Project: Star Search Simulation System
Author: Group 25
            (  Sungin Baek sbaek33@gatech.edu,
            Leonardo Gabriel Puy lpuy3@gatech.edu,
            Nathaniel Yohannesnyohannes3@gatech.edu,
            Ye Shen yshen61@gatech.edu,
            Mahesh Chandra Sehalli Jagadish mjagadish3@gatech.edu)
*/

package com.gatech.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;


public class ConfigController implements Initializable
{

    @FXML
    private GridPane configView;

    @FXML
    private TextField txtSceneFilePath;

    @FXML
    private TextField txtReportDirFilePath;


    public void exitConfig(ActionEvent actionEvent) {
        Stage stage = (Stage) configView.getScene().getWindow();
        stage.close();
    }

    public void saveConfig(ActionEvent actionEvent) {
        // String filePath = this.getClass().getResource("/com/gatech/config.json").getPath();
        String filePath = System.getProperty("user.dir") + "/config.json";
       // System.out.println("config.json" + filePath);
        try{

            FileWriter myWriter = new FileWriter(filePath);
            myWriter.write( "scenarioFilePath=" + txtSceneFilePath.getText().trim() );
            myWriter.write("\n");
            myWriter.write(  "reportFilePath=" + txtReportDirFilePath.getText().trim());
            myWriter.close();
        } catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
        Stage stage = (Stage) configView.getScene().getWindow();
        stage.close();
    }

    public void selectSceneFileDir(ActionEvent actionEvent) {
        Stage stage = (Stage) configView.getScene().getWindow();

        String txtSceneDir = txtReportDirFilePath.getText();

        if(txtSceneDir == null || txtSceneDir.isEmpty() )
        {
            txtSceneDir = ".";
        }
        else
        {
            File checkDirExists = new File(txtSceneDir);
            if(!checkDirExists.isDirectory())
            {
                txtSceneDir = ".";
            }
        }

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(txtSceneDir));
        File selectedDirectory = directoryChooser.showDialog(stage);
        txtSceneFilePath.setText(selectedDirectory.getAbsolutePath());
    }

    public void selectReportFileDir(ActionEvent actionEvent) {
        Stage stage = (Stage) configView.getScene().getWindow();
        String txtReportDir = txtReportDirFilePath.getText();

        if(txtReportDir == null || txtReportDir.isEmpty() )
        {
            txtReportDir = ".";
        }
        else
        {
            File checkDirExists = new File(txtReportDir);
            if(!checkDirExists.isDirectory())
            {
                txtReportDir = ".";
            }
        }

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(txtReportDir));
        File selectedDirectory = directoryChooser.showDialog(stage);
        txtReportDirFilePath.setText(selectedDirectory.getAbsolutePath());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // String filePath = this.getClass().getResource("/com/gatech/config.json").getPath();
        String filePath = System.getProperty("user.dir") + "/config.json";
       // System.out.println("config.json" + filePath);
        try {
            File file = new File( filePath);
           // System.out.println(file.exists());
            Scanner myReader = new Scanner(file);

            if(myReader.hasNextLine()) {
                String sFilePath = myReader.nextLine();
                txtSceneFilePath.setText( sFilePath.split("=")[1]);
            }

            if(myReader.hasNextLine()) {

                String rFilePath = myReader.nextLine();
                txtReportDirFilePath.setText(rFilePath.split("=")[1]);
            }
            myReader.close();

        } catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
    }
}
