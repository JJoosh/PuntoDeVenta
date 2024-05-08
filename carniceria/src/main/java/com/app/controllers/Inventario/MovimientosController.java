package com.app.controllers.Inventario;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.apache.poi.ss.formula.functions.T;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.app.models.Movimientos;
import com.app.models.Productos;

public class MovimientosController implements Initializable {
    @FXML private TableView<Movimientos> tablaDevolu;
    @FXML private TableColumn<Movimientos, Date> fecha;
    @FXML private TableColumn<Movimientos, String> productos;
    @FXML private TableColumn<Movimientos, String> movimiento;
    @FXML private TableColumn<Movimientos, BigDecimal> cantidad;
    @FXML private TableColumn<Movimientos, LocalTime> hora;
    @FXML private TableColumn<Movimientos, String> columCategoria;
    @FXML private DatePicker fechas;
    @FXML private ComboBox<String> categorias;
    @FXML private ComboBox<String> boxMovimiento;

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
        hora.setCellValueFactory(new PropertyValueFactory<>("hora") );
        columCategoria.setCellValueFactory(new PropertyValueFactory<>("nombreCategoria"));
        
        cargarTabla();

        FXMLInventarioController loadCat= new FXMLInventarioController();

        loadCat.cargarCategorias(this.categorias, 1);
        filtrarCategorias();
        ObservableList<String> movimientos = FXCollections.observableArrayList(
            "Todos",  "Entrada", "Salida"
        );
        boxMovimiento.setItems(movimientos);
        boxMovimiento.setValue("Todos");
        LocalDate fechaActual = LocalDate.now();
        fechas.setValue(fechaActual);

        
        fechas.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                List<Movimientos> movimientosFiltrados = obtenerMovimientosPorFecha(newValue);
                tablaDevoluciones.clear();
                tablaDevoluciones.addAll(movimientosFiltrados);
            }
        });
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

    private List<Movimientos> obtenerMovimientosPorFecha(LocalDate fecha) {
    Configuration configuration = new Configuration();
    configuration.configure("hibernate.cfg.xml");
    configuration.addAnnotatedClass(Movimientos.class);
    SessionFactory sessionFactory = configuration.buildSessionFactory();
    EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
    EntityManager entityManager = emf.createEntityManager();

    // Convertir la fecha seleccionada al formato de fecha utilizado en la base de datos
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    String fechaString = fecha.format(formatter);

    TypedQuery<Movimientos> query = entityManager.createQuery(
        "SELECT m FROM Movimientos m WHERE m.fecha = :fecha", Movimientos.class);
    query.setParameter("fecha", fechaString);

    List<Movimientos> movimientosFiltrados = query.getResultList();

    entityManager.close();
    emf.close();

    return movimientosFiltrados;
}

    public void filtrarCategorias() {
        categorias.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals("Todos")) {
                cargarTabla();
            } else {
                List<Movimientos> movimientosFiltrados = obtenerMovimientosPorCategoria(newValue);
                tablaDevoluciones.clear();
                tablaDevoluciones.addAll(movimientosFiltrados);
            }
        });
    }
    
    private List<Movimientos> obtenerMovimientosPorCategoria(String categoria) {
        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");
        configuration.addAnnotatedClass(Movimientos.class);
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = emf.createEntityManager();
    
        TypedQuery<Movimientos> query = entityManager.createQuery(
            "SELECT m FROM Movimientos m WHERE m.id_producto.categoria.nombreCategoria = :categoria", Movimientos.class);
        query.setParameter("categoria", categoria);
    
        List<Movimientos> movimientosFiltrados = query.getResultList();
    
        entityManager.close();
        emf.close();
    
        return movimientosFiltrados;
    }
}