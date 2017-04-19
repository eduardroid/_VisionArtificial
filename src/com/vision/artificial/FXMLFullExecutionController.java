/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vision.artificial;

import static com.vision.artificial.FXMLDemoController.MAX_TOL;
import static com.vision.artificial.FXMLDemoController.MIN_AREA;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.Core;
import static org.opencv.core.Core.BORDER_DEFAULT;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
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
        Mat mImagenAnalisis;       

        List<File> list =fileChooser.showOpenMultipleDialog(stage);
        if (list != null) {
            
            //<editor-fold defaultstate="collapsed" desc="delete previous file">
            File base= new File(this.strRutaResources);
            for(File file: base.listFiles())
                if (!file.isDirectory())
                    file.delete();
            //</editor-fold>
            
            //<editor-fold defaultstate="collapsed" desc="save new files">
            for (File file : list) {
                System.out.println(file.getName());
                mImagenReal = Imgcodecs.imread(file.getAbsolutePath());
                Imgcodecs.imwrite(strRutaResources+file.getName(),mImagenReal);
            }
            //</editor-fold>
            
            // <editor-fold defaultstate="collapsed" desc="procesamiento de imagen">
            for (int i = 1; i <= 387; i++) {
                //analisis para imagen A y B sera 1 y 2
                for (int j = 1; j <= 2; j++) {
                    mImagenAnalisis = Imgcodecs.imread(strRutaResources+ Integer.toString(i) +"_"+Integer.toString(j)+".jpeg");
                    ProcesarImagen(mImagenAnalisis);
                    //falta insertar en bd el resultado del análisis para hacer los estudios
                }
            }   
            // </editor-fold>
        }
    }
    
    public static final double MIN_AREA = 100.00;
    
    public static final double MAX_TOL = 200.00;
    
    private void ProcesarImagen(Mat mImagen){
        Mat mImagenRealRedimensionada= new Mat();
        Mat mImagenSuavizada = new Mat();
        Mat mImagenHSV = new Mat();
        List<Mat> lstCanalesHSV=new ArrayList<>(); 
        Mat mImagenBinarizada=new Mat();
        Mat mImgDilatar = new Mat();
        Mat mImgErode = new Mat();
        Mat mElementoErosion ;
        Mat mElementoDilatacion;
        Size tamaño= new Size(3,3);
        int tamañoErosion = 1;
        int tamañoDilatacion = 2;
        Scalar minVerde = new Scalar(29, 86, 6);        
        Scalar maxVerde = new Scalar(64, 255, 255);
        Mat mHierarchy= new Mat();
        
        // <editor-fold defaultstate="collapsed" desc=" Pre Procesar ">
        //1. REDIMENSION
        double ratio=mImagen.size().width/mImagen.size().height;
        int area=500000;
        double altura=Math.sqrt(area/(ratio));
        double anchura=altura*ratio;
        Size size= new Size(anchura,altura);
        Imgproc.resize(mImagen, mImagenRealRedimensionada, size);
        //2. SUAVIZAR
        Imgproc.GaussianBlur(mImagenRealRedimensionada, mImagenSuavizada, tamaño ,0,0, BORDER_DEFAULT );
        //3. HSV
        Imgproc.cvtColor(mImagenSuavizada, mImagenHSV, Imgproc.COLOR_BGR2HSV);
        Core.split(mImagenHSV,lstCanalesHSV);
        // </editor-fold>
        
        // <editor-fold defaultstate="collapsed" desc=" Segmentar ">
        //1. Binarizar
        Core.inRange(mImagenHSV, minVerde, maxVerde, mImagenBinarizada);
        //2. Erosiona Binaria
        mElementoErosion = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,new Size( 2*tamañoErosion + 1, 2*tamañoErosion+1 ));
        Imgproc.erode(mImagenBinarizada, mImgErode,mElementoErosion);
        //3. Dilatar Binaria
        mElementoDilatacion = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,new Size( 2*tamañoDilatacion + 1, 2*tamañoDilatacion+1 ));
        Imgproc.dilate(mImgErode, mImgDilatar,mElementoDilatacion);
        // </editor-fold>
        
        // <editor-fold defaultstate="collapsed" desc=" Descripcion ">
        List<MatOfPoint> circles;
        circles = new ArrayList<MatOfPoint>();
       
        Imgproc.findContours(mImagenBinarizada, circles,mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        for( int i = 0; i< circles.size(); i++ )
        {
            double actual_area = Math.abs(Imgproc.contourArea(circles.get(i)));
            if (actual_area < MIN_AREA) continue;
            
            Rect rect = Imgproc.boundingRect(circles.get(i));
            int A = rect.width  / 2;
            int B = rect.height / 2;
            double estimated_area = Math.PI * A * B;
            double error = Math.abs(actual_area - estimated_area);
            
            if (error > MAX_TOL) continue;
           
            System.out.printf("center x: %d y: %d A: %d B: %d\n", rect.x + A, rect.y + B, A, B);
        }     
        // </editor-fold>
        
        // <editor-fold defaultstate="collapsed" desc=" reconocimiento ">
        Mat mCircles= new Mat();
        int minRadius = 10;//10
	int maxRadius = 250;//18
        Imgproc.HoughCircles(lstCanalesHSV.get(1), mCircles,Imgproc.CV_HOUGH_GRADIENT,1, 
                            200, 150, 30, minRadius, maxRadius);//this.mImagenBinarizada
        double diametro=0;
        for( int i = 0; i < mCircles.cols(); i++ )
	{
            double vCircle[]=mCircles.get(0,i);
            System.out.println(vCircle[0]+" : "+vCircle[1]+" : "+vCircle[2]);//el tercero es el radio en px
            diametro=vCircle[2]*2*0.26454;
	}
        //fin dibujar circulos
        System.out.println("circulos: "+ mCircles.cols());
        
        if(diametro>=58 && diametro<=67)
        {
            System.out.println("Calibre 1: "+diametro);
        }
        if(diametro>=53 && diametro<=62)
        {
            System.out.println("Calibre 2: "+diametro);
        }
        if(diametro>=48 && diametro<=57)
        {
            System.out.println("Calibre 3: "+diametro);
        }
        if(diametro>=45 && diametro<=52)
        {
            System.out.println("Calibre 4: "+diametro);
        }
        if(diametro>=42 && diametro<=59)
        {
            System.out.println("Calibre 5: "+diametro);
        }
        if(diametro>=42 && diametro<=59)
        {
            System.out.println("Calibre no encontrado : "+diametro);
        }
        // </editor-fold>
    }

    
}
