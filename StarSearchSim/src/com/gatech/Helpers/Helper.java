package com.gatech.Helpers;

import javafx.scene.control.Alert;

import java.io.File;
import java.util.Scanner;

public class Helper
{
    public static void showError(int type, String message)
    {
        Alert alert;
        switch (type)
        {
            case 1:
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText(message);
                alert.showAndWait();
                break;

            case 2:
                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information");
                alert.setContentText(message);
                alert.showAndWait();
                break;
        }

    }


    public static String GetUserPreFromConfigFile(int type)
    {
        String dirPath = "";
        String filePath = System.getProperty("user.dir") + "/config.json";

        try {
            File file = new File( filePath);
            System.out.println(file.exists());
            Scanner myReader = new Scanner(file);

            if(myReader.hasNextLine()) {
                String sFilePath = myReader.nextLine();
                dirPath = sFilePath.split("=")[1];

                if(type == 1) {
                    return validateDirExist(dirPath);
                }
            }

            if(myReader.hasNextLine()) {

                String rFilePath = myReader.nextLine();
                dirPath = rFilePath.split("=")[1];
            }
            myReader.close();

        } catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }

        return  validateDirExist(dirPath);
    }

    private static String validateDirExist(String dirPath)
    {
        if(dirPath == null || dirPath.isEmpty() )
        {
            dirPath = ".";
        }
        else
        {
            File checkDirExists = new File(dirPath);
            if(!checkDirExists.isDirectory())
            {
                dirPath = ".";
            }
        }

        return  dirPath;
    }


}
