package com.gatech.Controller;


import com.gatech.Domain.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ManualController implements Initializable {
    @FXML
    private VBox manualBox;

    @FXML
    private ComboBox stepChoice;
    @FXML
    private Label manualLabel;

    private Action manualAction = new PassAction();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void setManualLabel(int droneID){
        manualLabel.setText(String.format("Manual Control \n Drone%d", droneID));
    }



    @FXML
    public void onClickNW(ActionEvent actionEvent){
        manualAction = new SteerAction(Navigation.Orientation.NW);
    }
    @FXML
    public void onClickN(ActionEvent actionEvent){
        manualAction = new SteerAction(Navigation.Orientation.N);
    }
    @FXML
    public void onClickNE(ActionEvent actionEvent){
        manualAction = new SteerAction(Navigation.Orientation.NE);
    }
    @FXML
    public void onClickS(ActionEvent actionEvent){
        manualAction = new SteerAction(Navigation.Orientation.S);
    }
    @FXML
    public void onClickSW(ActionEvent actionEvent){
        manualAction = new SteerAction(Navigation.Orientation.SW);
    }
    @FXML
    public void onClickSE(ActionEvent actionEvent){
        manualAction = new SteerAction(Navigation.Orientation.SE);
    }
    @FXML
    public void onClickW(ActionEvent actionEvent){
        manualAction = new SteerAction(Navigation.Orientation.W);
    }
    @FXML
    public void onClickE(ActionEvent actionEvent){
        manualAction = new SteerAction(Navigation.Orientation.E);
    }
    @FXML
    public void submitSteer(ActionEvent actionEvent){
        Stage stage = (Stage) manualBox.getScene().getWindow();
        stage.close();
    }
    @FXML
    public void submitScan(ActionEvent actionEvent){
        manualAction = new ScanAction();
        Stage stage = (Stage) manualBox.getScene().getWindow();
        stage.close();
    }
    @FXML
    public void submitPass(ActionEvent actionEvent){
        manualAction = new PassAction();
        Stage stage = (Stage) manualBox.getScene().getWindow();
        stage.close();
    }
    @FXML
    public void submitThrust(ActionEvent actionEvent){
        manualAction = new ThrustAction((Integer) stepChoice.getValue());
        Stage stage = (Stage) manualBox.getScene().getWindow();
        stage.close();
    }
    public Action getManualAction() {
        return manualAction;

    }
}
