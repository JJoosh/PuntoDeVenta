package com.app.controllers.Inventario;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.app.models.Movimientos;

public class MovimientosController implements Initializable {
    @FXML private TableView<Movimientos> tablaDevolu;
    @FXML private TableColumn<Movimientos, Date> fecha;
    @FXML private TableColumn<Movimientos, String> productos;
    @FXML private TableColumn<Movimientos, String> movimiento;
    @FXML private TableColumn<Movimientos, BigDecimal> cantidad;
    @FXML private DatePicker fechas;

    private ObservableList<Movimientos> tablaDevoluciones;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Inicializar la lista observable
        tablaDevoluciones = FXCollections.observableArrayList();

        // Asignar la lista observable a la tabla
        tablaDevolu.setItems(tablaDevoluciones);

        // Configurar las celdas de la tabla
        fecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        productos.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        movimiento.setCellValueFactory(new PropertyValueFactory<>("tipoMovimiento"));
        cantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        // Cargar la tabla al iniciar
        cargarTabla();
    }

    @FXML
    private void cargarTabla() {
        List<Movimientos> movimientos = obtenerMovimientos();
        tablaDevoluciones.clear();
        tablaDevoluciones.addAll(movimientos);
    }

    private List<Movimientos> obtenerMovimientos() {
        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");
        configuration.addAnnotatedClass(Movimientos.class);

        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = emf.createEntityManager();

        TypedQuery<Movimientos> query = entityManager.createQuery("SELECT m FROM Movimientos m", Movimientos.class);
        List<Movimientos> movimientos = query.getResultList();

        entityManager.close();
        emf.close();

        return movimientos;
    }
}