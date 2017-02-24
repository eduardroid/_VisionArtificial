/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vision.artificial;


import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import javafx.fxml.FXML;
import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import org.opencv.core.Core;
import static org.opencv.core.Core.BORDER_DEFAULT;
import static org.opencv.core.Core.convertScaleAbs;
import static org.opencv.core.CvType.CV_16S;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import org.opencv.core.Rect;
//import org.opencv.core.


/**
 * FXML Controller class
 *
 * @author Eduardo
 */
public class FXMLDemoController{
    
    //<editor-fold defaultstate="collapsed" desc="variables fxml">
    @FXML
    private TabPane tbDemo;
    @FXML
    private VBox vbImagenOriginal;
    @FXML
    private final ImageView ivImagenOriginal;
    @FXML
    private HBox hbImagenPreProcesada;
    @FXML
    private final ImageView ivImagenPreProcesada;
    @FXML
    private HBox hbImagenSegmentada;
    @FXML
    private final ImageView ivImagenHSV;
    @FXML
    private final ImageView ivImagenSegmentada;
    @FXML
    private final ImageView ivImagenCirculos;
    @FXML
    private HBox hbImagenDescripcion;
    @FXML
    private Button btnCargarImagen;
    
    @FXML 
    private GridPane gPaneSegmentar;
    @FXML
    private final ImageView ivImagenH;
    @FXML
    private final ImageView ivImagenS;
    @FXML
    private final ImageView ivImagenV;
//</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Variables">
    private Scene scene;
    private Stage stage;
    private final FileChooser fileChooser;
    private Mat mImagenReal;      
    private Mat mImagenHSV;   
 
    private Mat mImagenGris;   
    private Mat mImagenPreProcesada;    
    private Mat mImagenBinarizada;

    private final List<Mat> planes;
    private final Mat complexImage;  
    private final String strRutaResources;
    private File file;
    private Image iImagenInicial;  
    private Image iImagenPreProcesada;    
    private Image iImagenSegmentada;  
    private List<Mat> lstCanalesHSV;    
    private List<Mat> lstImgErosion;
    private Size s;
    
//</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Constructor">
    public FXMLDemoController() {
        this.ivImagenOriginal = new ImageView();
        this.ivImagenPreProcesada = new ImageView();        
        this.ivImagenSegmentada = new ImageView();     
        this.ivImagenHSV = new ImageView();

        this.ivImagenCirculos = new ImageView();
        this.ivImagenH = new ImageView();
        this.ivImagenS = new ImageView();
        this.ivImagenV = new ImageView();

        
        this.fileChooser= new FileChooser();
        // support variables
        this.mImagenReal= new Mat();        
        this.mImagenGris= new Mat();

        this.planes= new ArrayList<>();
        // the final complex mImagenReal
        this.complexImage=new Mat();
        this.scene=VisionArtificialMain.scene;
        this.stage=VisionArtificialMain.windows;
        this.strRutaResources="src/resources/";
        File f = new File(this.strRutaResources+"css/DemoStyle.css");
        scene.getStylesheets().clear();
        this.scene.getStylesheets().add("file:///"+f.getAbsolutePath().replace("\\", "/"));
        this.s= new Size(9,9);
    }
    
//</editor-fold>
    
    @FXML
    protected void loadImage() throws MalformedURLException {
        this.file=this.fileChooser.showOpenDialog(this.stage);
        if (file != null)
        {
            this.ivImagenOriginal.setImage(null);
            
            this.mImagenReal = Imgcodecs.imread(this.file.getAbsolutePath());
            Imgproc.cvtColor(this.mImagenReal, this.mImagenGris, Imgproc.COLOR_BGR2GRAY);
            this.iImagenInicial=this.mat2Image(this.mImagenReal);

            this.ivImagenOriginal.setImage(this.iImagenInicial);  
            this.ivImagenOriginal.setPreserveRatio(true);          
            this.ivImagenOriginal.fitHeightProperty().bind(this.vbImagenOriginal.heightProperty());
            this.ivImagenOriginal.setStyle("margin-top:10px");
            
            this.vbImagenOriginal.getChildren().add(ivImagenOriginal);
             
        }
    }
    
    @FXML
    protected void siguienteTab() throws IOException {
        this.tbDemo.getSelectionModel().selectNext();
        int i=this.tbDemo.getSelectionModel().getSelectedIndex();
        System.out.println(i);
        switch (i)
        {
            case 1:
                PreProcesarImagen(); //se aplicara binarizacion
                break;
            case 2:
                SegmentarImagen();//esto ira dentro de segmentaciónFaseOpeMorfologica();
                break;
            case 3:
                DescribirImagen();//aqui se encontrara el circulo a partir de la binarizada, erosionada y dilatada
                break;
            case 4:
                ReconocerImagen();
                break;
            default:
                break;
        }
    }
   
    private void PreProcesarImagen() throws IOException {
        //corregir metodo de redimensionamiento, para que mantenga aspect ratio
        Size size= new Size(300,300);
        Mat mImgRealRedimensionada= this.mImagenReal;//new Mat();
        //Imgproc.resize(this.mImagenReal, mImgRealRedimensionada, size);
        
        Mat mImgHSVLocal = new Mat();//bfImage.getHeight(), bfImage.getWidth(), CvType.CV_8UC3);
       
        Imgproc.cvtColor(mImgRealRedimensionada, mImgHSVLocal, Imgproc.COLOR_BGR2HSV);
        Imgcodecs.imwrite(strRutaResources+"img/hsv.jpg",mImgHSVLocal);        
        this.mImagenHSV=mImgHSVLocal;
        
        this.lstCanalesHSV= new ArrayList<>();
        Core.split(mImgHSVLocal,lstCanalesHSV);
        
        Scalar minVerde = new Scalar(29, 86, 6);        
        Scalar maxVerde = new Scalar(64, 255, 255);

        Mat mImagenBinarizadaLocal = new Mat();
        Core.inRange(mImgHSVLocal, minVerde, maxVerde, mImagenBinarizadaLocal);
        Imgcodecs.imwrite(strRutaResources+"img/binarizada.jpg",mImagenBinarizadaLocal);
        this.mImagenBinarizada=mImagenBinarizadaLocal;
        
        this.iImagenPreProcesada=this.mat2Image(mImagenBinarizadaLocal);//mImgHSVLocal
        this.mImagenPreProcesada=mImagenBinarizadaLocal;//mImgHSVLocal
        // show the result of the transformation as an mImagenReal
        this.ivImagenPreProcesada.setImage(this.mat2Image(mImagenBinarizadaLocal));//mImgHSVLocal        
        this.ivImagenHSV.setImage(this.mat2Image(mImgHSVLocal));//mImgHSVLocal

        // set a fixed width
        this.ivImagenOriginal.fitHeightProperty().bind(this.hbImagenPreProcesada.heightProperty());
        this.ivImagenPreProcesada.fitHeightProperty().bind(this.hbImagenPreProcesada.heightProperty());        
        this.ivImagenHSV.fitHeightProperty().bind(this.hbImagenPreProcesada.heightProperty());

        this.ivImagenOriginal.setFitWidth(400);
        this.ivImagenPreProcesada.setFitWidth(400);
        this.ivImagenPreProcesada.setPreserveRatio(true); 
        
        this.ivImagenHSV.setFitWidth(400);
        this.ivImagenHSV.setPreserveRatio(true); 

        hbImagenPreProcesada.getChildren().add(ivImagenOriginal);        
        hbImagenPreProcesada.getChildren().add(ivImagenPreProcesada);  
        hbImagenPreProcesada.getChildren().add(ivImagenHSV);
    }
    
    //@FXML
    protected void SegmentarImagen() throws IOException {
        /*Erosiona Binaria*/
        ImageView ivErosion = new ImageView(); 
        int tamañoErosion = 1;
        Mat mElementoErosion = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE
                ,new Size( 2*tamañoErosion + 1, 2*tamañoErosion+1 ));
        
        Mat mImgErode = new Mat();
        Imgproc.erode(this.mImagenBinarizada, mImgErode,mElementoErosion);
        
        this.lstImgErosion= new ArrayList<>();
        this.lstImgErosion.add(mImgErode);

        ivErosion.setImage(this.mat2Image(mImgErode));
        ivErosion.setFitWidth(200);
        ivErosion.setPreserveRatio(true);
        
        /*Dilatar Binaria*/
        ImageView ivDilatar = new ImageView(); 
        int tamañoDilatacion = 2;
        Mat mElementoDilatacion = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE
                ,new Size( 2*tamañoDilatacion + 1, 2*tamañoDilatacion+1 ));
        Mat mImgDilatar = new Mat();
        
        Imgproc.dilate(this.lstImgErosion.get(0), mImgDilatar,mElementoDilatacion);
        
        ivDilatar.setImage(this.mat2Image(mImgDilatar));
        ivDilatar.setFitWidth(200);
        ivDilatar.setPreserveRatio(true);

        ImageView ivImgBinarizada = new ImageView();
        ivImgBinarizada.setImage(this.mat2Image(this.mImagenBinarizada));
        ivImgBinarizada.setFitWidth(200);
        ivImgBinarizada.setPreserveRatio(true);
        
        /*Fin operaciones morfologicas*/
        hbImagenSegmentada.getChildren().add(ivImgBinarizada);
        hbImagenSegmentada.getChildren().add(ivErosion);
        hbImagenSegmentada.getChildren().add(ivDilatar);
        //hbImagenSegmentada.getChildren().add(this.ivImagenCirculos);
    }
    
    public static final double M_PI = 3.14159265358979323846;
    public static final double MIN_AREA = 100.00;
    public static final double MAX_TOL = 200.00;

    private void DescribirImagen() {
        Mat mClonImagenReal = this.mImagenReal;

        List<MatOfPoint> circles;
        circles = new ArrayList<MatOfPoint>();
       
        Mat mHierarchy= new Mat();
        //List<MatOfPoint> mContours = new ArrayList<MatOfPoint>(); 
        //double mMinContourArea = 0.1; 
        Imgproc.findContours(this.mImagenBinarizada, circles,mHierarchy
                , Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        //INVESTIGAR CMO FILTRAR MALOS CONTORNOS
        Mat mElipse= new Mat();
        
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
            
            /*mElipse= Imgproc.moments(circles.get(i));
            Point center = new Point();
            
            center.x= mElipse.get_m10()/ mElipse.get_m00;
            center.y= mElipse.get_m01()/ mElipse.get_m00;
          
            //mElipse=Imgproc.moments(mMoments)
            
            
            /*MatOfPoint2f temp=new MatOfPoint2f(mHierarchy);   
            RotatedRect elipse= Imgproc.fitEllipse(temp) ;
            //Imgproc.matchShapes(mHierarchy, elipse, 1, 0);//(mHierarchy,elipse, 1, 0.0)<1
            //if(elipse.size.height)
            //{*/
            
            Scalar color = new Scalar( 0,255,255);
            Imgproc.drawContours( mClonImagenReal, circles, i, color, 2, 8, mHierarchy, 0, new Point() );
            //}
        }
               
        Imgcodecs.imwrite(strRutaResources+"img/deteccionCirculo.jpg",mClonImagenReal);
        //fin dibujar circulos
        
        //System.out.println("circulos: "+ circles.cols());
        this.ivImagenCirculos.setImage(this.mat2Image(mClonImagenReal));
        this.ivImagenCirculos.setFitWidth(300);
        this.ivImagenCirculos.setPreserveRatio(true);   
        hbImagenDescripcion.getChildren().add(this.ivImagenCirculos);
    }
    
    private void ReconocerImagen() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    //<editor-fold defaultstate="collapsed" desc="GET - SET">
    public void setStage(Stage stage)
    {
        this.stage = stage;
    }
    public void setScene(Scene scene) 
    { 
        this.scene = scene; 
    }
//</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Utilities">
    /**
     * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
     * 
     * @param frame
     *            the {@link Mat} representing the current frame
     * @return the {@link Image} to show
     */
    private Image mat2Image(Mat frame)
    {
        // create a temporary buffer
        MatOfByte buffer = new MatOfByte();
        // encode the frame in the buffer, according to the PNG format
        Imgcodecs.imencode(".png", frame, buffer);
        // build and return an Image created from the mImagenReal encoded in the
        // buffer
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }
//</editor-fold>

/* @FXML
    private void ErosionarImagen() {
        ImageView ivErosion = new ImageView();        
        ImageView ivErosionH = new ImageView();
        ImageView ivErosionS = new ImageView();
        ImageView ivErosionV = new ImageView();

        int tamañoErosion = 5;
        Mat mElementoErosion = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE
                ,new Size( 2*tamañoErosion + 1, 2*tamañoErosion+1 ));
        Mat mImgErode = new Mat();
        Mat mImgErodeH = new Mat();
        Mat mImgErodeS = new Mat();
        Mat mImgErodeV = new Mat();
        
        Imgproc.erode(this.mImagenPreProcesada, mImgErode,mElementoErosion);
        Imgproc.erode(this.lstCanalesHSV.get(0), mImgErodeH,mElementoErosion);
        Imgproc.erode(this.lstCanalesHSV.get(1), mImgErodeS,mElementoErosion);
        Imgproc.erode(this.lstCanalesHSV.get(2), mImgErodeV,mElementoErosion);
        
        this.lstImgErosion= new ArrayList<>();
        this.lstImgErosion.add(mImgErode);
        this.lstImgErosion.add(mImgErodeH);
        this.lstImgErosion.add(mImgErodeS);
        this.lstImgErosion.add(mImgErodeV);

        
        ivErosion.setImage(this.mat2Image(mImgErode));
        ivErosionH.setImage(this.mat2Image(mImgErodeH));
        ivErosionS.setImage(this.mat2Image(mImgErodeS));
        ivErosionV.setImage(this.mat2Image(mImgErodeV));

        ivErosion.setFitWidth(200);
        ivErosionH.setFitWidth(200);
        ivErosionS.setFitWidth(200);
        ivErosionV.setFitWidth(200);
        
        ivErosion.setPreserveRatio(true);
        ivErosionH.setPreserveRatio(true);
        ivErosionS.setPreserveRatio(true);
        ivErosionV.setPreserveRatio(true);
        
        this.gPaneSegmentar.add(ivErosion, 1, 0);
        this.gPaneSegmentar.add(ivErosionH, 1, 1);
        this.gPaneSegmentar.add(ivErosionS, 1, 2);        
        this.gPaneSegmentar.add(ivErosionV, 1, 3);
    }
    
    @FXML
    private void DilatarImagen() {
        ImageView ivDilatar = new ImageView();        
        ImageView ivDilatarH = new ImageView();
        ImageView ivDilatarS = new ImageView();
        ImageView ivDilatarV = new ImageView();


        int tamañoDilatacion = 5;
        Mat mElementoDilatacion = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE
                ,new Size( 2*tamañoDilatacion + 1, 2*tamañoDilatacion+1 ));
        Mat mImgDilatar = new Mat();
        Mat mImgDilatarH = new Mat();
        Mat mImgDilatarS = new Mat();
        Mat mImgDilatarV = new Mat();
        
        Imgproc.erode(this.lstImgErosion.get(0), mImgDilatar,mElementoDilatacion);
        Imgproc.erode(this.lstImgErosion.get(1), mImgDilatarH,mElementoDilatacion);
        Imgproc.erode(this.lstImgErosion.get(2), mImgDilatarS,mElementoDilatacion);
        Imgproc.erode(this.lstImgErosion.get(3), mImgDilatarV,mElementoDilatacion);
        
        ivDilatar.setImage(this.mat2Image(mImgDilatar));
        ivDilatarH.setImage(this.mat2Image(mImgDilatarH));
        ivDilatarS.setImage(this.mat2Image(mImgDilatarS));
        ivDilatarV.setImage(this.mat2Image(mImgDilatarV));

        ivDilatar.setFitWidth(200);
        ivDilatarH.setFitWidth(200);
        ivDilatarS.setFitWidth(200);
        ivDilatarV.setFitWidth(200);
        
        ivDilatar.setPreserveRatio(true);
        ivDilatarH.setPreserveRatio(true);
        ivDilatarS.setPreserveRatio(true);
        ivDilatarV.setPreserveRatio(true);
        
        this.gPaneSegmentar.add(ivDilatar, 2, 0);
        this.gPaneSegmentar.add(ivDilatarH, 2, 1);
        this.gPaneSegmentar.add(ivDilatarS, 2, 2);        
        this.gPaneSegmentar.add(ivDilatarV, 2, 3);
    }
    
    */
    /*//ya no se usara este tab
    private void FaseOpeMorfologica() {
        
        Imgcodecs.imwrite(strRutaResources+"img/H.jpg",lstCanalesHSV.get(0));        
        Imgcodecs.imwrite(strRutaResources+"img/S.jpg",lstCanalesHSV.get(1));
        Imgcodecs.imwrite(strRutaResources+"img/V.jpg",lstCanalesHSV.get(2));

        this.ivImagenH.setImage(this.mat2Image(lstCanalesHSV.get(0)));        
        this.ivImagenS.setImage(this.mat2Image(lstCanalesHSV.get(1)));
        this.ivImagenV.setImage(this.mat2Image(lstCanalesHSV.get(2)));

        this.ivImagenPreProcesada.setFitWidth(200);
        this.ivImagenH.setFitWidth(200);
        this.ivImagenS.setFitWidth(200);
        this.ivImagenV.setFitWidth(200);
        
//        this.ivImagenPreProcesada.set
//        this.ivImagenH.setFitHeight(300);
//        this.ivImagenS.setFitHeight(300);
//        this.ivImagenV.setFitHeight(300);
        
        this.ivImagenPreProcesada.setPreserveRatio(true);
        this.ivImagenH.setPreserveRatio(true);
        this.ivImagenS.setPreserveRatio(true);  
        this.ivImagenV.setPreserveRatio(true);  
 
        
        this.gPaneSegmentar.add(this.ivImagenPreProcesada, 0, 0);
        this.gPaneSegmentar.add(this.ivImagenH, 0, 1);
        this.gPaneSegmentar.add(this.ivImagenS, 0, 2);        
        this.gPaneSegmentar.add(this.ivImagenV, 0, 3);
    }
    */

 
    
}
