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
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;



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
             
            /*if (!this.planes.isEmpty())
            {
                this.planes.clear();
                this.ivImagenPreProcesada.setImage(null);
                this.antitransformedImage.setImage(null);
            }*/
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
                PreProcesar(); //se aplicara binarizacion
                break;
            case 2:
                SegmentarImagen();//esto ira dentro de segmentaciónFaseOpeMorfologica();
                break;
            case 3:
                DescribirImagen();//aqui se encontrara el circulo a partir de la binarizada, erosionada y dilatada
                break;
            case 4:
                //1. analizar forma
                    //al tener la imagen en gaussianblur se puede usar para detectar el borde
                //2. analizar color del circulo
                //3. calcular dimensiones del codex - esto puede ser del siguiente paso
                //4. detectar daños.
                break;
            default:
                break;
        }
    }
    @FXML
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
    
    
    private void PreProcesar() throws IOException {
        BufferedImage bfImage=ImageIO.read(this.file);
        byte[] data = ((DataBufferByte) bfImage.getRaster().getDataBuffer()).getData();
        
        Mat mImgRealLocal = new Mat(bfImage.getHeight(),bfImage.getWidth(),CvType.CV_8UC3); 
        mImgRealLocal.put(0, 0, data);
        
        Mat mImgHSVLocal = new Mat(bfImage.getHeight(), bfImage.getWidth(), CvType.CV_8UC3);
       

        Imgproc.cvtColor(mImgRealLocal, mImgHSVLocal, Imgproc.COLOR_RGB2HSV);
        Imgcodecs.imwrite(strRutaResources+"img/hsv.jpg",mImgHSVLocal);        
        
        
        this.lstCanalesHSV= new ArrayList<>();
        Core.split(mImgHSVLocal,lstCanalesHSV);
        Mat mImagenBinarizadaLocal = new Mat();
        Imgproc.threshold(lstCanalesHSV.get(2), mImagenBinarizadaLocal, 127, 255, Imgproc.THRESH_TOZERO_INV);
        Imgcodecs.imwrite(strRutaResources+"img/binarizada.jpg",mImagenBinarizadaLocal);
        this.mImagenBinarizada=mImagenBinarizadaLocal;
        
        this.iImagenPreProcesada=this.mat2Image(mImagenBinarizadaLocal);//mImgHSVLocal
        this.mImagenPreProcesada=mImagenBinarizadaLocal;//mImgHSVLocal
        // show the result of the transformation as an mImagenReal
        this.ivImagenPreProcesada.setImage(this.mat2Image(mImagenBinarizadaLocal));//mImgHSVLocal
        // set a fixed width
        this.ivImagenOriginal.fitHeightProperty().bind(this.hbImagenPreProcesada.heightProperty());
        this.ivImagenPreProcesada.fitHeightProperty().bind(this.hbImagenPreProcesada.heightProperty());
        this.ivImagenOriginal.setFitWidth(400);
        this.ivImagenPreProcesada.setFitWidth(400);
        this.ivImagenPreProcesada.setPreserveRatio(true);   

        hbImagenPreProcesada.getChildren().add(ivImagenOriginal);        
        hbImagenPreProcesada.getChildren().add(ivImagenPreProcesada);
    }
    
    //ya no se usara este tab
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
    
    //@FXML
    protected void SegmentarImagen() throws IOException {
        
        /*Erosiona Binaria*/
        ImageView ivErosion = new ImageView(); 
        int tamañoErosion = 5;
        Mat mElementoErosion = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE
                ,new Size( 2*tamañoErosion + 1, 2*tamañoErosion+1 ));
        
        Mat mImgErode = new Mat();
        Imgproc.erode(this.mImagenBinarizada, mImgErode,mElementoErosion);
        
        this.lstImgErosion= new ArrayList<>();
        this.lstImgErosion.add(mImgErode);

        ivErosion.setImage(this.mat2Image(mImgErode));
        ivErosion.setFitWidth(200);
        ivErosion.setPreserveRatio(true);
        
        //this.gPaneSegmentar.add(ivErosion, 1, 0);
        
        /*Dilatar Binaria*/
        ImageView ivDilatar = new ImageView(); 

        int tamañoDilatacion = 5;
        Mat mElementoDilatacion = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE
                ,new Size( 2*tamañoDilatacion + 1, 2*tamañoDilatacion+1 ));
        Mat mImgDilatar = new Mat();
        
        Imgproc.erode(this.lstImgErosion.get(0), mImgDilatar,mElementoDilatacion);
        
        ivDilatar.setImage(this.mat2Image(mImgDilatar));
        ivDilatar.setFitWidth(200);
        ivDilatar.setPreserveRatio(true);
        
        //this.gPaneSegmentar.add(ivDilatar, 2, 0);
        
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
    
    private void DescribirImagen() {
        int scale = 1, delta = -10,ddepth = CV_16S;
        
        Mat grad_x = new Mat(), grad_y = new Mat();
        Mat abs_grad_x =new Mat(), abs_grad_y =new Mat();
        
        //ImageView ivlGrayScaleImage = new ImageView();
        /*this.lstCanalesHSV= new ArrayList<>();
        Core.split(this.mImagenPreProcesada,lstCanalesHSV);
        */
        //Mat mImagenSuavizada = new Mat();
        Mat mImagenSegmentadaLocal = new Mat();        
        //Mat mImagenBinarizadaLocal = new Mat();
        //Mat mImagenSalidaInRange = new Mat();    
        Mat mClonImagenReal = this.mImagenReal;

        //this.lstCanalesHSV.get(2);
        //mImagenSalidaInRange=this.lstCanalesHSV.get(2);//this.mImagenPreProcesada;
        //Imgproc.cvtColor(this.mImagenPreProcesada, mImagenSalidaInRange, Imgproc);
        //Core.inRange(this.mImagenPreProcesada, new Scalar(53, 87,43), new Scalar(159, 255, 178), mImagenSalidaInRange);
        
        //Imgproc.GaussianBlur( this.mImagenGris, mImagenSuavizada, this.s ,0,0, BORDER_DEFAULT );
        //Imgproc.threshold(mImagenSuavizada, mImagenBinarizadaLocal, 127, 255, Imgproc.THRESH_TOZERO);
        //ya tengo esto en mImgBinarizada
        
        //sobel
        /*Imgproc.Sobel(mImagenBinarizadaLocal, grad_x ,ddepth,1,0,3,scale,delta,Core.BORDER_DEFAULT);  
        convertScaleAbs( grad_x, abs_grad_x );
        Imgproc.Sobel(mImagenBinarizadaLocal, grad_y ,ddepth,0,1,3,scale,delta,Core.BORDER_DEFAULT);
        convertScaleAbs( grad_y, abs_grad_y );
        
        Core.addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 0, mImagenSegmentadaLocal);
        
        
        
        Imgcodecs.imwrite(strRutaResources+"img/sobel.jpg",mImagenSegmentadaLocal);
        */
        //cany
        //Imgproc.Canny(mImagenSuavizada, mImagenSuavizada ,100,700,5,true);        
        //Imgcodecs.imwrite(strRutaResources+"img/canny.jpg",mImagenSuavizada);

        Mat circles= new Mat();
        int minRadius = 10;//10
	int maxRadius = 290;//18
        Imgproc.HoughCircles(this.mImagenBinarizada, circles,Imgproc.CV_HOUGH_GRADIENT,1, 
                            200, 150, 30, minRadius, maxRadius);//mImagenSegmentadaLocal
        //Imgproc.HoughCircles(mImagenSegmentadaLocal, circles,Imgproc.CV_HOUGH_GRADIENT,1, minRadius, 120, 10, minRadius, maxRadius);

        for( int i = 0; i < circles.cols(); i++ )
	{
            double vCircle[]=circles.get(0,i);
            Point center=new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
            int radius = (int)Math.round(vCircle[2]);
            // draw the circle center
            Imgproc.circle(mClonImagenReal, center, 3,new Scalar(0,255,0), -1, 8, 0 );
            //(mImagenSuavizada, center, 3,sc1new Scalar(0,255,0), -1, 8, 0 )
            // draw the circle outline
            Imgproc.circle(mClonImagenReal, center, radius, new Scalar(0,0,255),3, 8, 0 );
            System.out.println(vCircle[0]+" : "+vCircle[1]+" : "+vCircle[2]);//el tercero es el radio en px
	}
        Imgcodecs.imwrite(strRutaResources+"img/deteccionCirculo.jpg",mClonImagenReal);
        //fin dibujar circulos
        System.out.println("circulos: "+ circles.cols());
        //System.out.println(mImagenSalidaInRange.rows()/8);
        //ivlGrayScaleImage.setImage(this.mat2Image(this.lstCanalesHSV.get(2)));
        //this.ivImagenSegmentada.setImage(this.mat2Image(mImagenSegmentadaLocal));//mImagenSegmentadaLocal
        this.ivImagenCirculos.setImage(this.mat2Image(mClonImagenReal));
        //this.ivImagenSegmentada.fitHeightProperty().bind(this.hbImagenSegmentada.heightProperty());
        //this.ivImagenPreProcesada.fitHeightProperty().bind(this.hbImagenSegmentada.heightProperty());
        //this.ivImagenPreProcesada.setFitWidth(300);
        //this.ivImagenSegmentada.setFitWidth(300);
        this.ivImagenCirculos.setFitWidth(300);
        //ivlGrayScaleImage.setFitWidth(300);        
        //ivlGrayScaleImage.setPreserveRatio(true);    
        //this.ivImagenSegmentada.setPreserveRatio(true);   
        this.ivImagenCirculos.setPreserveRatio(true);   

        //hbImagenDescripcion.getChildren().add(this.ivImagenPreProcesada);
        //hbImagenDescripcion.getChildren().add(this.ivImagenSegmentada);
        hbImagenDescripcion.getChildren().add(this.ivImagenCirculos);
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


    
    
}
