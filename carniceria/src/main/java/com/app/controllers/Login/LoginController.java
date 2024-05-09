package com.app.controllers.Login;

import java.io.IOException;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import com.app.controllers.Ventas.VentasController;
import com.app.models.Usuarios;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void initialize() {
        // Agregar listener de teclado al campo de usuario
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                passwordField.requestFocus();
            }
        });

        // Agregar listener de teclado al campo de contraseña
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });

        // Enfocar el campo de nombre de usuario al iniciar
        Platform.runLater(() -> usernameField.requestFocus());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.ERROR, "Error", "Por favor, ingrese un nombre de usuario y una contraseña.");
            return;
        }

        boolean isAuthenticated = authenticateUser(username, password);

        if (isAuthenticated) {
            try {
                // Cargar la vista Ventas.fxml
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Ventas.fxml"));
                Parent root = loader.load();

                // Obtener el controlador de la vista Ventas.fxml
                VentasController ventasController = loader.getController();
                ventasController.setNombreUsser(username);

                Scene scene = new Scene(root);

                // Obtener la ventana actual desde la escena asociada a los campos de texto
                Stage stage = (Stage) usernameField.getScene().getWindow();

                // Establecer la nueva escena en la ventana actual
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Error", "Error al cargar la vista Ventas.fxml");
            }
        } else {
            showAlert(AlertType.ERROR, "Error de autenticación", "Usuario o contraseña incorrectos");
        }
    }

    private boolean authenticateUser(String username, String password) {
        try (SessionFactory sessionFactory = new Configuration().configure().addAnnotatedClass(Usuarios.class)
                .buildSessionFactory();
                Session session = sessionFactory.openSession()) {
            // Consulta HQL para verificar las credenciales
            String hql = "FROM Usuarios WHERE nombreUsuario = :username AND contrasena = :password";
            Query<Usuarios> query = session.createQuery(hql, Usuarios.class);
            query.setParameter("username", username);
            query.setParameter("password", password);
            List<Usuarios> usuarios = query.list();
            return !usuarios.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}