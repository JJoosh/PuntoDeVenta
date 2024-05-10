package com.app.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.app.controllers.Inventario.FXMLInventarioController;
import com.app.controllers.Ventas.VentasController;
import com.app.controllers.devoluciones.FXMLDevolucionesController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import java.net.URL;
public class HomeController implements Initializable {
    @FXML
    Pane home;
    @FXML
    Pane menu_lateral;
    @FXML
    Label ventas;
    
   @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        menu_lateral.setOnKeyPressed(this::handleKeyPressed);
    }
    
    @FXML
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode()==KeyCode.F1) {
            abrirVentas();
        }
        if (event.getCode() == KeyCode.F2) {
            abrirInventario();   
            System.err.println("SE PRESIONO PERRA");
        }
        if (event.getCode()==KeyCode.F3) {
            abrirDevoluciones();
        }

        if(event.getCode()==KeyCode.F6){
            
        }
        if(event.getCode()==KeyCode.F1){
            
        }
    }

    public void abrirInventario() {
        try {
            // Cargar el archivo FXML con el nuevo contenido
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Inventario.fxml"));
            Pane nuevoContenido = loader.load();
            
            // Obtener el controlador del nuevo contenido
            FXMLInventarioController inventarioController = loader.getController();
            
            // Reemplazar el contenido del contenedor principal con el nuevo contenido
            home.getChildren().setAll(nuevoContenido);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void abrirVentas(){
        try {
            // Cargar el archivo FXML con el nuevo contenido
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Ventas.fxml"));
            Pane nuevoContenido = loader.load();
            
            // Obtener el controlador del nuevo contenido
            VentasController inventarioController = loader.getController();
            
            // Reemplazar el contenido del contenedor principal con el nuevo contenido
            home.getChildren().setAll(nuevoContenido);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void abrirDevoluciones(){
        try {
            // Cargar el archivo FXML con el nuevo contenido
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FXMLDevolucion.fxml"));
            Pane nuevoContenido = loader.load();
            
            // Obtener el controlador del nuevo contenido
            FXMLDevolucionesController inventarioController = loader.getController();
            
            // Reemplazar el contenido del contenedor principal con el nuevo contenido
            home.getChildren().setAll(nuevoContenido);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
