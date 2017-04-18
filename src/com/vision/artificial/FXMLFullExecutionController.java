/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vision.artificial;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * FXML Controller class
 *
 * @author EduardoDev
 */
public class FXMLFullExecutionController{
    
    @FXML
    private Button btnCargarImagenes;
    
    private Scene scene;
    private Stage stage;
    private final FileChooser fileChooser;
    //private File file;
    private final String strRutaResources;

    
    public FXMLFullExecutionController(){
        
        this.fileChooser= new FileChooser();
        
        this.strRutaResources="src/resources/img/base/";
    }
    
    @FXML
    protected void bulkImages() throws MalformedURLException {
        
        Mat mImagenReal;  
        Mat mImagenRealRedimensionada;
        
        
        List<File> list =fileChooser.showOpenMultipleDialog(stage);
        
        if (list != null) {
            File base= new File(this.strRutaResources);
            for(File file: base.listFiles()) 
                if (!file.isDirectory()) 
                    file.delete();
            
            for (File file : list) {
                System.out.println(file.getName());
                
                mImagenRealRedimensionada= new Mat();
                mImagenReal = Imgcodecs.imread(file.getAbsolutePath());
                
                double ratio=mImagenReal.size().width/mImagenReal.size().height;
                int area=500000;
                double altura=Math.sqrt(area/(ratio));
                double anchura=altura*ratio;
                Size size= new Size(anchura,altura);
                Imgproc.resize(mImagenReal, mImagenRealRedimensionada, size);
                Imgcodecs.imwrite(strRutaResources+file.getName(),mImagenRealRedimensionada); 
            }
        }

    }
    
    /*private void openFile(File file) {
        desktop.open(file);
    }*/

    
}
