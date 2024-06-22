package com.app.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.app.controllers.Configuracion.UsuariosController;
import com.app.controllers.Inventario.FXMLInventarioController;
import com.app.controllers.Ventas.VentasController;
import com.app.controllers.corte.FXMLCorte;
import com.app.controllers.devoluciones.FXMLDevolucionesController;
import com.app.controllers.Clientes.ClientesController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class HomeController implements Initializable {
    @FXML
    Pane home;
    @FXML
    Pane menu_lateral;
    @FXML
    Label ventas;
    @FXML
    Label usser;
    private Stage stage;
    private String userRole; // Variable para almacenar el rol del usuario
    private String nombre;
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        menu_lateral.setOnKeyPressed(this::handleKeyPressed);
        
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setResizable(false);
    }

    public void setNombre(String nombre){
        this.nombre=nombre;
        usser.setText(nombre);
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
            scene = new Scene(root, 670, 480);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case F1:
                abrirVentas();
                break;
            case F2:
                abrirInventario();
                break;
            case F4:
                abrirDevoluciones();
                break;
            case F3:
                abrirCorteCaja();
                break;
            case F5:
                openClients();
            default:
                break;
        }
    }

    @FXML
    private void abrirInventario() {
        if ("administrador".equals(userRole)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Inventario.fxml"));
                Pane nuevoContenido = loader.load();
                FXMLInventarioController inventarioController = loader.getController();
                VentasController ventasController=new VentasController();
               ventasController.onClose();
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
            ventasController.refrescarVistaVentas();
            ventasController.onClose();
            

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void abrirCorteCaja() {
       
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Corte.fxml"));
                Pane nuevoContenido = loader.load();
                FXMLCorte corteController = loader.getController();
                VentasController ventasController=new VentasController();
            ventasController.onClose();
                home.getChildren().setAll(nuevoContenido);
            } catch (IOException e) {
                e.printStackTrace();
            } 
       
        
    }

    public void abrirDevoluciones() {
        if ("administrador".equals(userRole)){
            try {  
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FXMLDevolucion.fxml"));
            Pane nuevoContenido = loader.load();
            FXMLDevolucionesController devolucionesController = loader.getController();
            VentasController ventasController=new VentasController();
              ventasController.onClose();
            home.getChildren().setAll(nuevoContenido);

            } catch (IOException e) {
                e.printStackTrace();
            } 
               
            } else {
                 showAlert("Acceso Denegado", "No tienes permiso para acceder a esta sección.");
            }
    
}

public void abrirConfiguracion() {
    if ("administrador".equals(userRole)){
        try {  
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Usuarios.fxml"));
        Pane nuevoContenido = loader.load();
        UsuariosController devolucionesController = loader.getController();
        VentasController ventasController=new VentasController();
            ventasController.onClose();
        home.getChildren().setAll(nuevoContenido);
        } catch (IOException e) {
            e.printStackTrace();
        } 
           
        } else {
             showAlert("Acceso Denegado", "No tienes permiso para acceder a esta sección.");
        }

}

public void openClients(){
    if ("administrador".equals(userRole)){
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/clientes2.fxml"));
        Pane nuevoContenido = loader.load();
        ClientesController clienteController = loader.getController();
        VentasController ventasController=new VentasController();
             ventasController.onClose();
        home.getChildren().setAll(nuevoContenido);
    } catch (IOException e) {
        e.printStackTrace();
    } }
    else{
        showAlert("Acceso Denegado", "No tienes permiso para acceder a esta sección.");

    }
}

@FXML
public void openHome() {
    // Limpia el contenido actual del panel home
    home.getChildren().clear();

    // Agrega nuevamente los elementos originales del panel home
    Label bienvenidoLabel = new Label("Bienvenido");
    bienvenidoLabel.setAlignment(Pos.CENTER);
    bienvenidoLabel.setPrefSize(1368, 176);
    bienvenidoLabel.setLayoutX(9);
    bienvenidoLabel.setLayoutY(426);
    bienvenidoLabel.setStyle("-fx-text-alignment: center;");
    bienvenidoLabel.setTextAlignment(TextAlignment.CENTER);
    bienvenidoLabel.setTextFill(Color.web("#4d2d12"));
    bienvenidoLabel.setFont(new Font("Yu Gothic UI Regular", 120));

    ImageView homeImageView = new ImageView(new Image(getClass().getResourceAsStream("/img/Home.png")));
    homeImageView.setFitHeight(243);
    homeImageView.setFitWidth(260);
    homeImageView.setLayoutX(571);
    homeImageView.setLayoutY(152);
    homeImageView.setPickOnBounds(true);
    homeImageView.setPreserveRatio(true);

    // Agrega los elementos al panel home
    home.getChildren().addAll(bienvenidoLabel, homeImageView);

    // Establece el fondo blanco
    home.setStyle("-fx-background-color: white;");
}
}