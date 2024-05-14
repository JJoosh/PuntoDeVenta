package com.app.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.app.controllers.Inventario.FXMLInventarioController;
import com.app.controllers.Ventas.VentasController;
import com.app.controllers.corte.FXMLCorte;
import com.app.controllers.devoluciones.FXMLDevolucionesController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class HomeController implements Initializable {
    @FXML
    Pane home;
    @FXML
    Pane menu_lateral;
    @FXML
    Label ventas;

    private Stage stage;
    private String userRole; // Variable para almacenar el rol del usuario

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        menu_lateral.setOnKeyPressed(this::handleKeyPressed);

    }

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setResizable(false);
    }


    public void setUserRole(String role) {
        this.userRole = role;
    }

    @FXML
    public void cerrar() {
        this.stage.close();
        Scene scene;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Login.fxml"));
        Parent root;
        try {
            root = loader.load();
            scene = new Scene(root, 640, 480);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleKeyPressed(KeyEvent event) {

        if (event.getCode()==KeyCode.F1) {
            abrirVentas();
         
        }
        if (event.getCode() == KeyCode.F2) {
            abrirInventario();   
           
        }
        if (event.getCode()==KeyCode.F3) {
            abrirDevoluciones();
            
        }

        if(event.getCode()==KeyCode.F4){
            abrirCorteCaja();
           
        }
       
    }

   
    @FXML
    private void abrirInventario() {
        if ("administrador".equals(userRole)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Inventario.fxml"));
                Pane nuevoContenido = loader.load();
                FXMLInventarioController inventarioController = loader.getController();
                home.getChildren().setAll(nuevoContenido);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showAlert("Acceso Denegado", "No tienes permiso para acceder a esta sección.");
        }
    }

    private void showAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void abrirVentas() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Ventas.fxml"));
            Pane nuevoContenido = loader.load();
            VentasController ventasController = loader.getController();
            home.getChildren().setAll(nuevoContenido);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void abrirCorteCaja() {
        if ("administrador".equals(userRole)){
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Corte.fxml"));
                Pane nuevoContenido = loader.load();
                FXMLCorte corteController = loader.getController();
                home.getChildren().setAll(nuevoContenido);
            } catch (IOException e) {
                e.printStackTrace();
            } 
        } else {
            showAlert("Acceso Denegado", "No tienes permiso para acceder a esta sección.");
        }
        
    }

    public void abrirDevoluciones() {
        if ("administrador".equals(userRole)){
            try {  
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FXMLDevolucion.fxml"));
            Pane nuevoContenido = loader.load();
            FXMLDevolucionesController devolucionesController = loader.getController();
            home.getChildren().setAll(nuevoContenido);

            } catch (IOException e) {
                e.printStackTrace();
            } 
               
            } else {
                 showAlert("Acceso Denegado", "No tienes permiso para acceder a esta sección.");
            }
    
}}
