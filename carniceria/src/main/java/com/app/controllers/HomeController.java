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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class HomeController implements Initializable {
    @FXML
    private HBox root;
    @FXML
    private StackPane home;
    @FXML
    private VBox menu_lateral;
    @FXML
    private Label usser;
    private Stage stage;
    private String userRole;
    private String nombre;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        root.setOnKeyPressed(this::handleKeyPressed);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setResizable(true);
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
        usser.setText(nombre);
    }

    public void setUserRole(String role) {
        this.userRole = role;
    }

    @FXML
    public void cerrar() {
        this.stage.close();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case F1: abrirVentas(); break;
            case F2: abrirInventario(); break;
            case F3: abrirCorteCaja(); break;
            case F4: abrirDevoluciones(); break;
            case F5: openClients(); break;
            default: break;
        }
    }

    @FXML
    private void abrirInventario() {
        if ("administrador".equals(userRole)) {
            loadView("/views/Inventario.fxml", FXMLInventarioController.class);
        } else {
            showAlert("Acceso Denegado", "No tienes permiso para acceder a esta sección.");
        }
    }

    @FXML
    public void abrirVentas() {
        loadView("/views/Ventas.fxml", VentasController.class);
    }

    @FXML
    public void abrirCorteCaja() {
        loadView("/views/Corte.fxml", FXMLCorte.class);
    }

    @FXML
    public void abrirDevoluciones() {
        if ("administrador".equals(userRole)) {
            loadView("/views/FXMLDevolucion.fxml", FXMLDevolucionesController.class);
        } else {
            showAlert("Acceso Denegado", "No tienes permiso para acceder a esta sección.");
        }
    }

    @FXML
    public void abrirConfiguracion() {
        if ("administrador".equals(userRole)) {
            loadView("/views/Usuarios.fxml", UsuariosController.class);
        } else {
            showAlert("Acceso Denegado", "No tienes permiso para acceder a esta sección.");
        }
    }

    @FXML
    public void openClients() {
        if ("administrador".equals(userRole)) {
            loadView("/views/clientes2.fxml", ClientesController.class);
        } else {
            showAlert("Acceso Denegado", "No tienes permiso para acceder a esta sección.");
        }
    }

    @FXML
    public void openHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Home.fxml"));
            Parent homeContent = loader.load();
            home.getChildren().setAll(homeContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private <T> void loadView(String fxmlPath, Class<T> controllerClass) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent nuevoContenido = loader.load();
            T controller = loader.getController();
            if (controller instanceof VentasController) {
                ((VentasController) controller).refrescarVistaVentas();
            }
            home.getChildren().setAll(nuevoContenido);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}