package com.app.controllers.devoluciones;


import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.app.controllers.Inventario.FXMLInventarioController;
import com.app.models.DetallesVenta;
import com.app.models.Devoluciones;
import com.app.models.Productos;
import com.app.models.Ventas;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;

public class FXMLVerTabla{
    @FXML
    private FXMLDevolucionesController devolucionesController;
    @FXML
    private TableView<Devoluciones> Devoluciones;
    @FXML
    private TableColumn<Devoluciones, Long> IdDevoluciones;
    @FXML
    private TableColumn<Devoluciones, Long> venta;
    @FXML
    private TableColumn<Devoluciones, Double> cantidadDevuelta;
    @FXML
    private TableColumn<Devoluciones, String> Motivo;
    @FXML
    private TableColumn<Devoluciones, Timestamp> fecha;

    private ObservableList<Devoluciones> DevolucionData;

     public void setDevolucionesController(FXMLDevolucionesController devolucionesController) {
        this.devolucionesController = devolucionesController;
    }
     public void initialize() {
        mostartabla();
    }
    public void mostartabla(){
        venta.setCellValueFactory(cellData -> {
    return new SimpleLongProperty(cellData.getValue().getVenta().getId()).asObject();
});

        IdDevoluciones.setCellValueFactory(new PropertyValueFactory<>("id"));
        cantidadDevuelta.setCellValueFactory(new PropertyValueFactory<>("cantidadDevuelta"));
        Motivo.setCellValueFactory(new PropertyValueFactory<>("Motivo"));
        fecha.setCellValueFactory(new PropertyValueFactory<>("fechaDevolucion"));

        // Obtener los productos de la base de datos
        List<Devoluciones> devolucion = obtenerdevolucion();

        // Crear una lista observable a partir de la lista de productos
        
        DevolucionData = FXCollections.observableArrayList(devolucion);

        Devoluciones.setItems(DevolucionData);
        
    }
    private List<Devoluciones> obtenerdevolucion() {
        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");

        configuration.addAnnotatedClass(Devoluciones.class);
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = emf.createEntityManager();
        TypedQuery<Devoluciones> query = entityManager.createQuery("SELECT d FROM Devoluciones d", Devoluciones.class);
     
        List<Devoluciones> devolucion = query.getResultList();
        // Suponiendo que ventas es una lista de objetos Ventas
        entityManager.close();
        emf.close();

        return devolucion;
    }
@FXML
    private void regresar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FXMLDevolucion.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}