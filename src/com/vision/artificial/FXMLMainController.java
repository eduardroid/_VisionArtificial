/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vision.artificial;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

/**
 *
 * @author Eduardo
 */
public class FXMLMainController implements Initializable {

    @FXML
    private void btnDemoAction(ActionEvent event) throws IOException {
        VisionArtificialMain.showDemoOverview();
    }
    
    @FXML
    private void btnEjecutarAction(ActionEvent event) {
        System.out.println("You clicked me! ejecutar");
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
