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

import com.gatech.Helpers.Helper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ReportController implements Initializable
{
    @FXML
    private VBox reportBox;

    public void closeReportWindow(ActionEvent actionEvent)
    {
        Stage stage = (Stage) reportBox.getScene().getWindow();
        stage.close();
    }

    private  void saveFinalReport(int RegionSize, int PotentialCut, int ActualCut, int TurnsCompleted)
    {
        String filePath = Helper.GetUserPreFromConfigFile(2);
        //System.out.println("config.json" + filePath);
        try{
            LocalDateTime myDateObj = LocalDateTime.now();
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");

            String formattedDate = myDateObj.format(myFormatObj);

            FileWriter myWriter = new FileWriter(filePath + "/finalReport_"+formattedDate+".txt");
            String report ="RegionSize - " + String.valueOf(RegionSize) + ","
                    + "PotentialCut - " +String.valueOf(PotentialCut) + "," +
                    "ActualCut - "+ String.valueOf(ActualCut) + "," +
                    "TurnsCompleted - "+ String.valueOf(TurnsCompleted);
            myWriter.write( report );
            myWriter.close();
        } catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
    }

    private  void showChartReport(int turnLimit, int RegionSize, int PotentialCut, int ActualCut, int TurnsCompleted)
    {
        CategoryAxis xAxis    = new CategoryAxis();
        xAxis.setLabel("");

        NumberAxis yAxis = new NumberAxis();
      /*  yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(turnLimit);*/
        yAxis.setLabel("Stats");

        BarChart finalReport = new BarChart(xAxis, yAxis);

        XYChart.Series dataSeries1 = new XYChart.Series();
        dataSeries1.setName("final report");

        dataSeries1.getData().add(new XYChart.Data("RegionSize", RegionSize));
        dataSeries1.getData().add(new XYChart.Data("PotentialCut", PotentialCut));
        dataSeries1.getData().add(new XYChart.Data("ActualCut"  , ActualCut));
        dataSeries1.getData().add(new XYChart.Data("TurnsCompleted"  ,TurnsCompleted));

        finalReport.getData().add(dataSeries1);

        reportBox.getChildren().removeAll(reportBox.getChildren());
        reportBox.getChildren().add(finalReport);
    }

    public void showReport(int turnLimit, int RegionSize, int PotentialCut, int ActualCut, int TurnsCompleted)
    {
        showChartReport(turnLimit, RegionSize, PotentialCut, ActualCut, TurnsCompleted);
        saveFinalReport(RegionSize, PotentialCut, ActualCut, TurnsCompleted);
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
