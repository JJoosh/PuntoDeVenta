package com.app.controllers.Login;

import com.app.models.Usuarios;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class cambiarcontra {
    @FXML
    private TextField lblUsser;
    @FXML
    private TextField lblactPassword;
    @FXML
    private TextField lblNewPassword;
    @FXML
    private TextField lblPasswordCon;
     private Stage dialogStage; // Referencia al Stage

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    @FXML
    private void initialize() {
    }

    @FXML
    public void modificarUser() {
        String username = lblUsser.getText();
        String currentPassword = lblactPassword.getText();
        String newPassword = lblNewPassword.getText();
        String confirmPassword = lblPasswordCon.getText();

        if (username.isEmpty() || currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Por favor, rellene todos los campos."));
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "La nueva contraseña y la confirmación no coinciden."));
            return;
        }

        // Configuración de Hibernate
        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");
        configuration.addAnnotatedClass(Usuarios.class);

        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.openSession();

        try {
            session.beginTransaction();

            Usuarios usuario = (Usuarios) session.createQuery("FROM Usuarios WHERE nombreUsuario = :username")
                    .setParameter("username", username)
                    .uniqueResult();

            if (usuario == null) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "El usuario no existe."));
                return;
            }

            // Verificar si la contraseña actual es correcta
            if (!usuario.getContrasena().equals(currentPassword)) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "La contraseña actual es incorrecta."));
                return;
            }

            // Actualizar la contraseña
            usuario.setContrasena(newPassword);
            session.update(usuario);
            session.getTransaction().commit();
            Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Éxito", "La contraseña se ha actualizado correctamente."));
            dialogStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            session.getTransaction().rollback();
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Ocurrió un error al actualizar la contraseña."));
        } finally {
            session.close();
            sessionFactory.close();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
