package com.app.controllers.Configuracion;

import com.app.models.Usuarios;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;

public class modUsser {

    @FXML
    private TextField lblUsser;
    @FXML
    private TextField lblPasswor;
    @FXML
    private TextField lblPasswordConfirmada;

    private Usuarios usuario;

    public void setData(Usuarios usuario) {
        this.usuario = usuario;
        lblUsser.setText(usuario.getNombreUsuario());
        lblPasswor.setText(usuario.getContrasena());
    }

    @FXML
    private void initialize() {
    }

    @FXML
    public void modificarUser() {
        String newPassword = lblPasswor.getText();
        String confirmPassword = lblPasswordConfirmada.getText();

        if (newPassword.equals(confirmPassword)) {
            usuario.setContrasena(newPassword);

            // Actualizar el usuario en la base de datos
            Configuration configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");
            configuration.addAnnotatedClass(Usuarios.class);

            SessionFactory sessionFactory = configuration.buildSessionFactory();
            Session session = sessionFactory.openSession();

            try {
                session.beginTransaction();
                session.update(usuario);
                session.getTransaction().commit();

                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Éxito");
                alert.setHeaderText(null);
                alert.setContentText("Usuario actualizado correctamente");
                alert.showAndWait();
                lblUsser.setText("");
                lblPasswor.setText("");
            } catch (Exception e) {
                e.printStackTrace();
                session.getTransaction().rollback();

                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Error al actualizar el usuario");
                alert.showAndWait();
            } finally {
                session.close();
                sessionFactory.close();
            }
        } else {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Las contraseñas no coinciden");
            alert.showAndWait();
        }
    }
}
