/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vision.artificial;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTabPane;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.opencv.core.Core;

/**
 *
 * @author Eduardo
 */
public class VisionArtificialMain extends Application {
    public static Stage windows;
    private static BorderPane rootLayout;
    public static Scene scene;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.windows = primaryStage;
        
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(VisionArtificialMain.class.getResource("view/FXMLMain.fxml"));
        this.rootLayout = (BorderPane) loader.load();
        // Show the scene containing the root layout.
        this.scene = new Scene(this.rootLayout);
        //scene.getStylesheets().add("view/DemoStyle.css");
        this.windows.setScene(this.scene);
        this.windows.show();     
    }
    
    
    public static void showDemoOverview() throws IOException {
      
            // Load person overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(VisionArtificialMain.class.getResource("view/FXMLDemo.fxml"));
            AnchorPane demoOverview = (AnchorPane) loader.load();
            FXMLDemoController d = new FXMLDemoController();
            rootLayout.setCenter(demoOverview);
            //d.setStage(windows);
    }
    
    public static void showFullExecutionOverview() throws IOException {
      
            // Load person overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(VisionArtificialMain.class.getResource("view/FXMLFullExecution.fxml"));
            AnchorPane demoOverview = (AnchorPane) loader.load();
            FXMLDemoController d = new FXMLDemoController();
            rootLayout.setCenter(demoOverview);
            //d.setStage(windows);
    }
    
    public Stage getWindows() {
        return windows;
    }
    
    
}
