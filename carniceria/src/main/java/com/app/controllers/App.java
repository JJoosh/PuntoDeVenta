package com.app.controllers;

import java.io.IOException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    private static Scene scene;

    @Override
    public void start(@SuppressWarnings("exports") Stage stage) throws IOException {
        // Inicializar Hibernate
        Configuration configuration = new Configuration();
        configuration.configure("/hibernate.cfg.xml");
        SessionFactory sessionFactory = configuration.buildSessionFactory();

        // Crear una sesión de Hibernate
        Session session = sessionFactory.openSession();
        System.out.println(session);

        // Cargar la vista principal
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/primary.fxml"));
        Parent root = loader.load();
        scene = new Scene(root, 1000, 650);
        stage.setScene(scene);
        scene.getStylesheets().add(getClass().getResource("/css/Fondo.css").toExternalForm());
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/views/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch(args);
    }

}