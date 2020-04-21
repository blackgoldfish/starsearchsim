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

import com.gatech.Domain.*;
import com.gatech.Domain.Point;
import com.gatech.Helpers.Helper;
import com.gatech.Model.Stats;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class MainController
{
    public MainController()
    {
        m_drones = new ArrayList<>();
        m_ufo = null;
        m_turnLimit = -1;
        m_turnsCompleted = 0;
    }

    public void exitSimulator(ActionEvent actionEvent)
    {
        Platform.exit();
    }

    @FXML
    private GridPane gpSpaceRegion;
    @FXML
    private ProgressBar squaresProgressBar;
    @FXML
    private ProgressBar turnsProgressBar;
    @FXML
    private ProgressBar dronesProgressBar;
    @FXML
    private ProgressIndicator squaresProgressIndicator;
    @FXML
    private ProgressIndicator turnsProgressIndicator;

    @FXML
    private Label squaresProgressLabel;
    @FXML
    private Label turnsProgressLabel;
    @FXML
    private Label dronesProgressLabel;
    @FXML
    private Label droneStatusLabel;
    @FXML
    private Label droneActionStatusLabel;

    @FXML
    private javafx.scene.control.Button nextBtn;
    @FXML
    private javafx.scene.control.Button fastForwardActionBtn;
    @FXML
    private javafx.scene.control.Button stopActionBtn;

    @FXML
    public void loadSpaceRegionGridPane(ActionEvent actionEvent)
    {
        String filePath = getScenarioFilePath();

        if(filePath == null || filePath.isEmpty())
        {
            return;
        }

        m_drones = new ArrayList<>();
        m_ufo = null;
        m_turnLimit = -1;
        m_turnsCompleted = 0;
        DroneRegion.getRegion().clear();

        m_currentDroneId = 0;
        nextBtn.setDisable(false);
        fastForwardActionBtn.setDisable(false);
        stopActionBtn.setDisable(false);

        readScenarioFile(filePath);
        showSpaceRegion();
        initiateProgressBarProperties();
        nextBtn.setDisable(false);
        fastForwardActionBtn.setDisable(false);
    }

    private String getScenarioFilePath() {
        Stage primaryStage = (Stage) gpSpaceRegion.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();

        String sceneDirPath = Helper.GetUserPreFromConfigFile(1);
        File defaultDirectory = new File(sceneDirPath);
        fileChooser.setInitialDirectory(defaultDirectory);
        fileChooser.setTitle("Select a valid Scenario File");
        File file = fileChooser.showOpenDialog(primaryStage);

        if(file == null){
            return  "";
        }else{
            return file.getAbsolutePath();
        }
    }

    public void showHelp(ActionEvent actionEvent) {

     Helper.showError(2, "Please go through the Read me file");

    }

    private void showSpaceRegion()
    {
        gpSpaceRegion.setStyle("-fx-background-color: #000000;");
        gpSpaceRegion.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

        //Setting the Grid alignment
        gpSpaceRegion.setAlignment(Pos.CENTER);

        gpSpaceRegion.getChildren().removeAll(gpSpaceRegion.getChildren());

        for (int j = 0; j < m_region.getHeight(); j++) {
            for (int i = 0; i < m_region.getWidth(); i++) {
                com.gatech.Domain.Point point = new com.gatech.Domain.Point(i, j);

                if (m_region.isWormhole(point))
                {
                    gpSpaceRegion.add(readImageResource("wormhole.png", OBJ_SIZE), i, m_region.getHeight() - j);
                }

                if (m_region.isStars(point))
                {
                    gpSpaceRegion.add(readImageResource("stars.png", OBJ_SIZE), i, m_region.getHeight() - j);
                }
                else if (m_region.isSun(point))
                {
                    gpSpaceRegion.add(readImageResource("sun.png", OBJ_SIZE), i, m_region.getHeight() - j);
                }
                else if (m_region.isDrone(point))
                {
                    Drone drone = m_drones.get(m_region.getDroneID(point));
                    ImageView droneImg = readImageResource("drone_" + drone.getDirection().toString() + ".png", OBJ_SIZE);
                    gpSpaceRegion.add(droneImg, i, m_region.getHeight() - j);
                    addDroneLabel(i, m_region.getHeight() - j, String.valueOf(m_region.getDroneID(point)));
                }
                else if (m_region.isUfo(point))
                {
                    ImageView ufoImg = readImageResource("ufo_" + m_ufo.getDirection().toString() + ".png", OBJ_SIZE);
                    gpSpaceRegion.add(ufoImg, i, m_region.getHeight() - j);
                }
                else if (m_region.isEmpty(point))
                {
                    gpSpaceRegion.add(readImageResource("empty.png", OBJ_SIZE), i, m_region.getHeight() - j);
                }
            }
        }
    }

    private void startOrchestration()
    {
        initiateProgressBarProperties();
        m_updateSpaceRegion = new Timeline(new KeyFrame(Duration.millis(1250), new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                run();
            }
        }));
        m_updateSpaceRegion.setCycleCount(m_turnLimit);
        m_updateSpaceRegion.play();
        m_updateSpaceRegion.statusProperty().addListener((obs, oldStatus, newStatus) -> {
            if (newStatus == Animation.Status.STOPPED) {
                try {
                    if(m_isManualMode){                        //https://stackoverflow.com/questions/22463004/using-showandwait-in-the-onfinished-eventhandler-of-an-animation-doesnt-work
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    m_manualAction = loadManualView(new ActionEvent());
                                    m_drones.get(m_currentDroneId).setAction(m_manualAction);
                                    validateDroneAction(m_currentDroneId, m_manualAction);
                                    m_isManualMode = false;
                                    m_updateSpaceRegion.play();
                                    displayActionAndResponses(m_currentDroneId, m_manualAction);
                                    showSpaceRegion();
                                    updateStatusPanel();
                                    m_currentDroneId = findNextActiveDrone();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    }else{
                        loadReportView(new ActionEvent());} //https://bugs.openjdk.java.net/browse/JDK-8095631
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        m_updateSpaceRegion.setOnFinished(event ->
        {
            try {
                loadReportView(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void initiateProgressBarProperties() {
        droneProperty = new SimpleDoubleProperty(0.0);
        dronesProgressBar.progressProperty().unbind();
        dronesProgressBar.progressProperty().bind(droneProperty);

        m_droneStat = m_region.getStats();
        int totalSafeSquareNum = m_droneStat.getPotentialCut();
        int exploredSquareNum = m_droneStat.getActualCut();
        float squareProgress = exploredSquareNum/totalSafeSquareNum;
        squareProperty = new SimpleDoubleProperty(squareProgress);
        squaresProgressBar.progressProperty().unbind();
        squaresProgressBar.progressProperty().bind(squareProperty);
        squaresProgressIndicator.progressProperty().bind(squareProperty);

        turnProperty = new SimpleDoubleProperty((0.0));
        turnsProgressBar.progressProperty().unbind();
        turnsProgressBar.progressProperty().bind(turnProperty);
        turnsProgressIndicator.progressProperty().bind(turnProperty);
    }


    private void addDroneLabel(int x, int y, String text)
    {
        javafx.scene.control.Label droneLabel = new javafx.scene.control.Label(text);
        droneLabel.setTextAlignment(TextAlignment.CENTER);
        droneLabel.setFont(new javafx.scene.text.Font("Arial", DRONE_ID_SIZE));
        droneLabel.setTextFill(Color.BLACK);
        droneLabel.setScaleX(1.1);
        droneLabel.setScaleY(1.1);
        GridPane.setHalignment(droneLabel, HPos.CENTER);
        gpSpaceRegion.add(droneLabel, x, y);

        javafx.scene.control.Label droneLabel1 = new Label(text);
        droneLabel1.setTextAlignment(TextAlignment.CENTER);
        droneLabel1.setFont(new Font("Arial", DRONE_ID_SIZE));
        droneLabel1.setTextFill(Color.LIMEGREEN);
        GridPane.setHalignment(droneLabel1, HPos.CENTER);
        gpSpaceRegion.add(droneLabel1, x, y);
    }

    private ImageView readImageResource(String fileName, int size)
    {
        Image image = new Image("com/gatech/Images/" + fileName, size, size, false, false);
        ImageView iv = new ImageView(image);
        return iv;
    }

    public void showAbout(ActionEvent actionEvent)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Start Search Simulation");
        alert.setHeaderText("Product Information");
        alert.setContentText("Start Search Simulation\r\n" +
                "Version 1.0\r\n" +
                "Copyright - Georgia Tech ");
        alert.showAndWait();
    }

    public void loadConfigView(ActionEvent actionEvent) throws Exception
    {
        Stage configStage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("/com/gatech/View/config.fxml"));
        configStage.setTitle("Configuration");
        configStage.setScene(new Scene(root,400, 250));
        configStage.getIcons().add(new Image("com/gatech/Images/app.jpg"));
        configStage.initModality(Modality.APPLICATION_MODAL);
        configStage.show();
    }

    public void loadReportView(ActionEvent actionEvent) throws Exception
    {
        if (m_updateSpaceRegion != null && ! m_updateSpaceRegion.getStatus().equals( Animation.Status.STOPPED))
        {
            m_updateSpaceRegion.stop();
        }

        if (stopActionBtn.isDisable())
        {
            return;
        }

        nextBtn.setDisable(true);
        fastForwardActionBtn.setDisable(true);
        stopActionBtn.setDisable(true);

        Stage reportStage = new Stage();
        //Parent root = FXMLLoader.load(getClass().getResource("../View/report.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gatech/View/report.fxml"));
        Parent root = loader.load();
        ReportController rc = loader.getController();
        rc.showReport(m_turnLimit, m_region.getStats().getRegionSize(), m_region.getStats().getPotentialCut(),
                m_region.getStats().getActualCut(), m_turnsCompleted);
        reportStage.setTitle("Final Report of Simulation");
        reportStage.setScene(new Scene(root,400, 350));
        reportStage.getIcons().add(new Image("com/gatech/Images/app.jpg"));
        reportStage.initModality(Modality.APPLICATION_MODAL);
        reportStage.show();
    }

    public Action loadManualView(ActionEvent actionEvent) throws Exception {
        Stage manualStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gatech/View/manual.fxml"));
        Parent root = loader.load();
        ManualController mc = loader.getController();
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        manualStage.setX(screenBounds.getWidth()-180-screenBounds.getWidth()/50);
        manualStage.setY(screenBounds.getHeight()-450-screenBounds.getHeight()/10);
        manualStage.setTitle("Final Report of Simulation");
        manualStage.setScene(new Scene(root,180, 450));
        manualStage.getIcons().add(new Image("com/gatech/Images/app.jpg"));
        manualStage.initModality(Modality.APPLICATION_MODAL);
        mc.setManualLabel(m_currentDroneId);
        manualStage.showAndWait();
        Action action = mc.getManualAction();
        return action;

    }


    /**
     * Reads in a configuration file with initial configuration for the simulation
     *
     * @param  testFileName File name that contains simulation parameters
     */
    private void readScenarioFile(String testFileName) {
        final String DELIMITER = ",";

        try {
            Scanner takeCommand = new Scanner(new File(testFileName));

            // read in the region information
            String[] tokens;
            tokens = takeCommand.nextLine().split(DELIMITER);
            int regionWidth = Integer.parseInt(tokens[0]);
            tokens = takeCommand.nextLine().split(DELIMITER);
            int regionHeight = Integer.parseInt(tokens[0]);

            m_region = new com.gatech.Domain.Region(regionWidth, regionHeight);

            // read in the drone starting information
            tokens = takeCommand.nextLine().split(DELIMITER);
            int numDrones = Integer.parseInt(tokens[0]);
            for (int k = 0; k < numDrones; k++) {
                tokens = takeCommand.nextLine().split(DELIMITER);
                Navigation.Orientation orientation = Navigation.fromString(tokens[2]);
                if (orientation == null) {
                    orientation = Navigation.Orientation.N;
                }

                com.gatech.Domain.Point point = new com.gatech.Domain.Point(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
                m_drones.add(new Drone(k, point, orientation, Integer.parseInt(tokens[3])));

                // Explore the stars at the initial location
                m_region.setDrone(point, k);
            }

            // read in the sun information
            tokens = takeCommand.nextLine().split(DELIMITER);
            int numSuns = Integer.parseInt(tokens[0]);
            for (int k = 0; k < numSuns; k++) {
                tokens = takeCommand.nextLine().split(DELIMITER);

                // place a sun at the given location
                m_region.setSun(new com.gatech.Domain.Point(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1])));
            }

            // Read number of iterations
            tokens = takeCommand.nextLine().split(DELIMITER);
            m_turnLimit = Integer.parseInt(tokens[0]);

            // check if the next line is empty
            if (takeCommand.hasNextLine()) {
                try {
                    // Read wormholes information
                    tokens = takeCommand.nextLine().split(DELIMITER);

                    int numOfWh = Integer.parseInt(tokens[0]);
                    for (int k = 0; k < numOfWh; k += 2) {
                        com.gatech.Domain.Point entry = null;

                        if (takeCommand.hasNextLine()) {
                            tokens = takeCommand.nextLine().split(DELIMITER);

                            if (tokens != null && tokens.length == 2) {
                                entry = new com.gatech.Domain.Point(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
                            }
                        }

                        com.gatech.Domain.Point exit = null;

                        if (takeCommand.hasNextLine()) {
                            tokens = takeCommand.nextLine().split(DELIMITER);

                            if (tokens != null && tokens.length == 2) {
                                exit = new com.gatech.Domain.Point(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
                            }
                        }

                        if (entry != null && exit != null) {
                            m_region.setWormholes(entry, exit);
                        }
                    }

                    // check if the next line is empty
                    if (takeCommand.hasNextLine()) {
                        try {
                            // read in UFO information
                            tokens = takeCommand.nextLine().split(DELIMITER);

                            Navigation.Orientation orientation = Navigation.fromString(tokens[2]);
                            if (orientation == null) {
                                orientation = Navigation.Orientation.N;
                            }

                            com.gatech.Domain.Point point = new com.gatech.Domain.Point(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
                            m_ufo = new Ufo(point, orientation, m_region);

                            // Explore the stars at the initial location
                            m_region.setUfo(point);
                        }
                        catch (Exception e)
                        {
                            GenerateRandomUfo();
                        }
                    } else {
                        GenerateRandomUfo();
                    }
                }
                catch (Exception e)
                {
                    GenerateRandomWormholes();
                    GenerateRandomUfo();
                }
            } else {
                GenerateRandomWormholes();
                GenerateRandomUfo();
            }


            takeCommand.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println();
        }
    }

    private void GenerateRandomWormholes() {
        List<com.gatech.Domain.Point> starsList = GetStarsCoordinates();

        if (starsList.size() >= 2)
        {
            // Choose position for first wormhole
            com.gatech.Domain.Point wh1 = starsList.get(m_randGenerator.nextInt(starsList.size()));
            com.gatech.Domain.Point wh2 = wh1;

            // Choose position for second wormhole
            while (wh1.equals(wh2))
            {
                wh2 = starsList.get(m_randGenerator.nextInt(starsList.size()));
            }

            // We have our locations, place them in the space region
            m_region.setWormholes(wh1, wh2);
        }
        else
        {
            // Not possible to generate wormholes, do nothing
        }
    }

    private void GenerateRandomUfo() {
        List<com.gatech.Domain.Point> starsList = GetStarsCoordinates();

        if (starsList.size() >= 1)
        {
            // Choose position for UFO
            com.gatech.Domain.Point point = starsList.get(m_randGenerator.nextInt(starsList.size()));

            // Generate a random orientation
            Navigation.Orientation orientation = Navigation.Orientation.values()[m_randGenerator.nextInt(Navigation.Orientation.values().length)];

            // We have our location, place the UFO in the space region
            m_ufo = new Ufo(point, orientation, m_region);

            // Explore the stars at the initial location
            m_region.setUfo(point);
        }
        else
        {
            // Not possible to generate wormholes, do nothing
        }
    }

    private List<com.gatech.Domain.Point> GetStarsCoordinates() {
        List<com.gatech.Domain.Point> list = new ArrayList<com.gatech.Domain.Point>();

        for (int i = 0; i < m_region.getWidth(); i++) {
            for (int j = 0; j < m_region.getHeight(); j++) {
                Point point = new Point(i, j);
                if (m_region.isStars(point))
                {
                    list.add(point);
                }
            }
        }

        return list;
    }


    /**
     * Runs the simulation for the number of specified turns.
     * Simulation may end up early if all drones are destroyed or if the whole space region has been explored.
     */
    public void run() {
        try {
            if (m_turnsCompleted < m_turnLimit) {
                if (allDronesStopped() || m_region.getStats().getNumStars() == 0) {
                    m_updateSpaceRegion.stop();
                    return;
                }

                if (m_currentDroneId != -1) {
                    // Ask drone for its next action
                    if (m_drones.get(m_currentDroneId).getStrategy() != 2){
                        Action nextDroneAction = m_drones.get(m_currentDroneId).getNextAction();
                        validateDroneAction(m_currentDroneId, nextDroneAction);
                        displayActionAndResponses(m_currentDroneId, nextDroneAction);
                        showSpaceRegion();
                        updateStatusPanel();
                        m_currentDroneId = findNextActiveDrone();
                    }else{
                        try {
                            m_isManualMode = true;
                            m_updateSpaceRegion.stop();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    // Ask UFO for its next action
                    if (m_ufo != null) {
                        Action nextUfoAction = m_ufo.getNextAction();
                        validateUfoAction(nextUfoAction);
                        displayActionAndResponses(-1, nextUfoAction);
                    }

                    m_turnsCompleted++;
                    showSpaceRegion();
                    updateStatusPanel();
                    m_currentDroneId = findNextActiveDrone();
                }


            } else {
                loadReportView(new ActionEvent());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Update the status panel (under fast forward mode)
     */
    private void updateStatusPanel() {
        //label
        updateStatusLabel();

        //drone progress
        droneProperty.set((double)(m_drones.size() - getNumDeadDrones()) / (double)(m_drones.size()));

        //turn progress
        turnProperty.set((double)m_turnsCompleted/(double)m_turnLimit);

        //squares progress
        Stats statistics = m_region.getStats();
        int totalSafeSquareNum = statistics.getPotentialCut();
        int exploredSquareNum = statistics.getActualCut();
        squareProperty.set((double)exploredSquareNum/(double)totalSafeSquareNum);
    }

    private int getNumDeadDrones()
    {
        int n = 0;

        for (int i = 0; i < m_drones.size(); i++)
        {
            if (m_drones.get(i).getStatus() == Drone.Status.CRASH) {
                n++;
            }
        }

        return n;
    }


    /**
     * Update the label in status panel
     */
    private void updateStatusLabel(){
        Stats statistics = m_region.getStats();
        int totalSafeSquareNum = statistics.getPotentialCut();
        int exploredSquareNum = statistics.getActualCut();
        dronesProgressLabel.setText(String.format("%d of %d drones active", m_drones.size() - getNumDeadDrones(), m_drones.size()));
        turnsProgressLabel.setText(String.format("%d completed/%d total turns", m_turnsCompleted, m_turnLimit));
        squaresProgressLabel.setText(String.format("%d explored/%d safe squares", exploredSquareNum, totalSafeSquareNum));

        if (m_currentDroneId == -2){  //initialization stage
            return;
        }

        if(m_currentDroneId == -1){ //UFO
            if (m_ufo != null) {
                String str = String.format("UFO: %s ", m_ufo.getAction().toString());
                if (m_ufo.getAction().getAction() == Action.ActionType.STEER) {
                    str = str + Navigation.toString(((SteerAction) m_ufo.getAction()).getOrientation());
                } else if (m_ufo.getAction().getAction() == Action.ActionType.THRUST) {
                    str = str + ((ThrustAction) m_ufo.getAction()).getThrust();
                }

                droneStatusLabel.setText(str);
                droneActionStatusLabel.setText(m_ufo.getStatusString());
            }

            return;
        }

        StringBuilder str = new StringBuilder();

        str.append(String.format("Drone %d: ", m_currentDroneId));
        Action currentAction = m_drones.get(m_currentDroneId).getAction();

        if (currentAction.getAction() == Action.ActionType.STEER) {
            str.append("Steer " + Navigation.toString(((SteerAction) currentAction).getOrientation()));
        } else if (currentAction.getAction() == Action.ActionType.THRUST) {
            str.append("Thrust " + ((ThrustAction) currentAction).getThrust());
        } else if (currentAction.getAction() == Action.ActionType.PASS) {
            str.append("Pass");
        } else { // scan
            str.append("Scan");
            str.append("\n");
            String scanResults = com.gatech.Domain.Region.formatScanResults(m_trackScanResults);
            List<String> scanList = Arrays.asList(scanResults.split(","));
            str.append(String.join(", ", scanList.subList(0, 4)) + "\n" + String.join(", ", scanList.subList(4, 8)));
        }

        droneStatusLabel.setText(str.toString());
        droneActionStatusLabel.setText(m_drones.get(m_currentDroneId).getStatusString());

    }

    /**
     * Validates proposed action by the drone, including chosen orientation and thrust length.
     *
     * @param id     Drone ID
     * @param droneAction Action proposed by the drone
     */
    public void validateDroneAction(int id, Action droneAction) {
        Drone drone = m_drones.get(id);

        if (droneAction != null) {
            if (droneAction.getAction() == Action.ActionType.SCAN) {
                // in the case of a scan, return the information for the eight surrounding squares
                // always use a northbound orientation
                m_trackScanResults = m_region.scan(drone.getPoint());
                m_drones.get(id).sendScanResults(m_trackScanResults);
            } else if (droneAction.getAction() == Action.ActionType.PASS) {
                // If drone is over a wormhole, send it back.
                if (m_region.isWormhole(drone.getPoint()))
                {
                    sendThroughWormhole(drone, drone.getPoint());
                }
            } else if (droneAction.getAction() == Action.ActionType.STEER) {
                Navigation.Orientation orientation = ((SteerAction)droneAction).getOrientation();
                if (orientation != null) {
                    m_drones.get(id).setDirection(orientation);
                } else {
                    System.out.println("action_not_recognized");
                }
            } else if (droneAction.getAction() == Action.ActionType.THRUST) {
                int thrust = ((ThrustAction)droneAction).getThrust();
                if (thrust > 0 && thrust <= MAX_THRUST) {
                    // in the case of a thrust, ensure that the move doesn't cross suns or barriers
                    Navigation.Orientation orientation = drone.getDirection();
                    int remainingThrust = thrust;

                    while (remainingThrust > 0 && drone.getStatus() == Drone.Status.OK) {
                        com.gatech.Domain.Point newPoint = drone.getPoint().getTranslation(orientation, 1);

                        if (m_region.isBarrier(newPoint)) {
                            // drone hit a barrier and simply doesn't move (do nothing)
                        } else if (m_region.isSun(newPoint)) {
                            // drone hit a sun
                            drone.informVaporization(newPoint);
                            m_region.setEmpty(drone.getPoint());
                        } else if (m_region.isDrone(newPoint)) {
                            int id2 = m_region.getDroneID(newPoint);
                            if (id2 >= 0)
                            {
                                // Another drone is here, destroy both drones
                                drone.informCrash();
                                m_drones.get(id2).informCrash();

                                // Update region
                                m_region.setEmpty(drone.getPoint());
                                m_region.setEmpty(m_drones.get(id2).getPoint());
                            }
                        } else if (m_region.isUfo(newPoint)) {
                            // Destroy drone
                            drone.informCrash();

                            // Update region
                            m_region.setEmpty(drone.getPoint());
                        } else if (m_region.isWormhole(newPoint)) {
                            sendThroughWormhole(drone, newPoint);
                        } else {
                            // Update region status
                            m_region.setEmpty(drone.getPoint());
                            drone.setPosition(newPoint);
                            m_region.setDrone(newPoint, id);
                        }

                        remainingThrust--;
                    }
                } else {
                    System.out.println("action_not_recognized");
                }
            } else {
                System.out.println("action_not_recognized");
            }
        }
    }

    /**
     * Validates proposed action by the UFO, including chosen orientation and thrust length.
     *
     * @param ufoAction Action proposed by the UFO
     */
    public void validateUfoAction(Action ufoAction) {
        if (ufoAction != null) {
            if (ufoAction.getAction() == Action.ActionType.PASS) {
                // If UFO is over a wormhole, send it back.
                if (m_region.isWormhole(m_ufo.getPoint()))
                {
                    sendThroughWormhole(m_ufo, m_ufo.getPoint());
                }
            } else if (ufoAction.getAction() == Action.ActionType.STEER) {
                Navigation.Orientation orientation = ((SteerAction)ufoAction).getOrientation();
                if (orientation != null) {
                    m_ufo.setDirection(orientation);
                } else {
                    System.out.println("action_not_recognized");
                }
            } else if (ufoAction.getAction() == Action.ActionType.THRUST) {
                int thrust = ((ThrustAction)ufoAction).getThrust();
                if (thrust == 1) {
                    // in the case of a thrust, ensure that the move doesn't cross suns or barriers
                    Navigation.Orientation orientation = m_ufo.getDirection();

                    com.gatech.Domain.Point newPoint = m_ufo.getPoint().getTranslation(orientation, 1);

                    if (m_region.isBarrier(newPoint)) {
                        // UFO hit a barrier and simply doesn't move (do nothing)
                    } else if (m_region.isDrone(newPoint)) {
                        int droneID = m_region.getDroneID(newPoint);
                        if (droneID >= 0)
                        {
                            // Destroy drone
                            m_drones.get(droneID).informCrash();

                            // Update region
                            m_region.setEmpty(newPoint);
                            m_ufo.setPosition(newPoint);
                            m_region.setUfo(newPoint);
                        }
                    } else if (m_region.isWormhole(newPoint)) {
                        sendThroughWormhole(m_ufo, newPoint);
                    } else {
                        // Update region status
                        m_ufo.setPosition(newPoint);
                        m_region.setUfo(newPoint);
                    }
                } else {
                    System.out.println("action_not_recognized");
                }
            } else {
                System.out.println("action_not_recognized");
            }
        }
    }

    private void sendThroughWormhole(Drone drone, com.gatech.Domain.Point entryPoint) {
        m_region.setEmpty(drone.getPoint());
        com.gatech.Domain.Point exitPoint = m_region.getWormholeExitPos(entryPoint);
        drone.informWormhole(entryPoint, exitPoint);

        if (m_region.isDrone(exitPoint) ) {
            int id2 = m_region.getDroneID(exitPoint);
            if (id2 >= 0)
            {
                // Another drone is here, destroy both drones
                drone.informCrash();
                m_drones.get(id2).informCrash();

                // Update region
                m_region.setEmpty(m_drones.get(id2).getPoint());
            }
        } else if (m_region.isUfo(exitPoint)) {
            // Destroy drone
            drone.informCrash();
        } else {
            // Exit wormhole is clear, update position
            drone.setPosition(exitPoint);
            m_region.setDrone(exitPoint, drone.getID());
        }
    }

    private void sendThroughWormhole(Ufo ufo, com.gatech.Domain.Point entryPoint) {
        com.gatech.Domain.Point exitPoint = m_region.getWormholeExitPos(entryPoint);

        if (m_region.isDrone(exitPoint)) {
            int droneID = m_region.getDroneID(exitPoint);
            if (droneID >= 0)
            {
                // A drone is here, destroy it
                m_drones.get(droneID).informCrash();

                // Update region
                m_region.setEmpty(m_drones.get(droneID).getPoint());
            }
        } else if (m_region.isUfo(exitPoint)) {
            // Are we gonna have 2+ UFOs?
        }

        ufo.setPosition(exitPoint);
        m_region.setUfo(exitPoint);
    }

    /**
     * Outputs action taken by the drone and system responses
     *
     * @param id     Drone ID
     * @param proposedAction Action proposed by the drone
     */
    public void displayActionAndResponses(int id, Action proposedAction) {
        if (proposedAction != null)
        {
            // Display the drone's actions
            if (id >= 0) {
                System.out.print("Drone " + id + ": " + proposedAction.toString());
            }
            else
            {
                System.out.print("UFO: " + proposedAction.toString());
            }

            if (proposedAction.getAction() == Action.ActionType.STEER) {
                System.out.println("," + Navigation.toString(((SteerAction)proposedAction).getOrientation()));
            } else if (proposedAction.getAction() == Action.ActionType.THRUST) {
                System.out.println("," + ((ThrustAction)proposedAction).getThrust());
            } else {
                System.out.println();
            }

            // display the simulation checks and/or responses
            if (proposedAction.getAction() == Action.ActionType.THRUST || proposedAction.getAction() == Action.ActionType.STEER || proposedAction.getAction() == Action.ActionType.PASS) {
                if (id >= 0) {
                    System.out.println(m_drones.get(id).getStatusString());
                } else {
                    System.out.println(m_ufo.getStatusString());
                }
            } else if (proposedAction.getAction() == Action.ActionType.SCAN) {
                System.out.println(com.gatech.Domain.Region.formatScanResults(m_trackScanResults));
            } else {
                System.out.println("action_not_recognized");
            }

            if (m_verbose) {
                // Render the state of the space region after each command
                m_region.render();

                // Display the drones' orientations
                for (int k = 0; k < m_drones.size(); k++) {
                    if (m_drones.get(k).getStatus() == Drone.Status.OK) {
                        System.out.println("Drone " + k + ": " + m_drones.get(k).getDirection() +
                                " (" + m_drones.get(k).getPoint().getX() + ", " + m_drones.get(k).getPoint().getY() + ")");
                    }
                }

                // Display the UFO's orientation
                if (m_ufo.getStatus() == Ufo.Status.OK) {
                    System.out.println("UFO: " + m_ufo.getDirection() +
                            " (" + m_ufo.getPoint().getX() + ", " + m_ufo.getPoint().getY() + ")");
                }

                System.out.println();

                // Display shared map
                System.out.println("Shared information:");
                if (m_drones.size() > 0)
                {
                    m_drones.get(0).render();
                }
            }
        } else {
            System.out.println("action_not_recognized");
        }
    }

    /**
     * Checks if all drones have been destroyed
     *
     * @return True if all drones have been destroyed, false if at least one drone is active
     */
    public Boolean allDronesStopped() {
        for(int k = 0; k < m_drones.size(); k++) {
            if (m_drones.get(k).getStatus() == Drone.Status.OK) {
                return Boolean.FALSE;
            }
        }

        return Boolean.TRUE;
    }

    public void nextBtnPress(ActionEvent actionEvent) {
        try {
            if (m_turnsCompleted < m_turnLimit) {
                if (allDronesStopped() || m_region.getStats().getNumStars() == 0) {
                    loadReportView(actionEvent);
                    nextBtn.setDisable(true);
                    return;
                }

                if (m_currentDroneId != -1) {
                    // Ask drone for its next action
                    if (m_drones.get(m_currentDroneId).getStrategy() != 2) {
                        Action nextDroneAction = m_drones.get(m_currentDroneId).getNextAction();
                        validateDroneAction(m_currentDroneId, nextDroneAction);
                        displayActionAndResponses(m_currentDroneId, nextDroneAction);
                    } else {
                        m_manualAction = loadManualView(new ActionEvent());
                        m_drones.get(m_currentDroneId).setAction(m_manualAction);
                        validateDroneAction(m_currentDroneId, m_manualAction);
                        displayActionAndResponses(m_currentDroneId, m_manualAction);
                    }
                } else {
                    // Ask UFO for its next action
                    if (m_ufo != null) {
                        Action nextUfoAction = m_ufo.getNextAction();
                        validateUfoAction(nextUfoAction);
                        displayActionAndResponses(-1, nextUfoAction);
                    }

                    m_turnsCompleted++;
                }

                showSpaceRegion();
                updateStatusPanel();

                m_currentDroneId = findNextActiveDrone();
            } else {
                nextBtn.setDisable(true);
                loadReportView(actionEvent);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int findNextActiveDrone()
    {
        for (int k = m_currentDroneId + 1; k < m_drones.size(); k++) {
            if (m_drones.get(k).getStatus() == Drone.Status.OK) {
                return k;
            }
        }

        // We got to the end of the list without finding another drone to move, so return
        // -1 to get the UFO a chance to move next
        return -1;
    }

    public void fastForwardActionBtnPress(ActionEvent actionEvent) {
        nextBtn.setDisable(true);
        stopActionBtn.setDisable(false);
        startOrchestration();
    }

    private Timeline m_updateSpaceRegion = null;
    private DoubleProperty droneProperty;
    private DoubleProperty squareProperty;
    private DoubleProperty turnProperty;
    private final int MAX_THRUST = 3;
    private boolean m_isManualMode = false;
    private Action m_manualAction;

    private boolean m_verbose = false;
    private com.gatech.Domain.Region m_region;
    private List<Drone> m_drones;
    private Ufo m_ufo;
    private int m_currentDroneId = -2;

    private int[] m_trackScanResults;

    private Integer m_turnLimit;
    private int m_turnsCompleted;
    private Stats m_droneStat;

    private static Random m_randGenerator = new Random(System.currentTimeMillis());

    private final int OBJ_SIZE = 75;
    private final int DRONE_ID_SIZE = 20;
}


