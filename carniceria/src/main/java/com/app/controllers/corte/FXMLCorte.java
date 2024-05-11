package com.app.controllers.corte;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.app.models.Ventas;
import com.app.models.CortedeCaja;
import com.app.models.DetallesVenta;
import com.app.models.Devoluciones;
import com.app.models.Productos;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;

public class FXMLCorte {

    @FXML
    private TableView<CortedeCaja> Corte;
    @FXML
    private TableColumn<CortedeCaja, BigDecimal> IDcorte;
    @FXML
    private TableColumn<CortedeCaja, LocalDate> Fecha;
    @FXML
    private TableColumn<CortedeCaja, BigDecimal> CantidadC;
    @FXML
    private DatePicker datePicker;

    @FXML 
    private Pane rootPane;

    private ObservableList<CortedeCaja> datosTabla = FXCollections.observableArrayList();
    private List<Ventas> listaVentas;
    List<DetallesVenta> Listadetalles;
    List<DetallesVenta> detallesventasfiltrado;

    public void initialize() {
        configurarTabla();
        listaVentas = obtenerListaDeVentas();
        Listadetalles= obtenerListaDeDetalleVentas();
        

        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                datosTabla.clear();
                Corte.setItems(datosTabla);

            } else {
                LocalDate fecha = newValue;
                filtrarCortePorFecha(fecha);
            }
        });
        if (rootPane != null) {
            rootPane.setOnKeyPressed(this::handleKeyPressed);
        } else {
            // Manejar el caso en el que rootPane sea nulo
        }
    }

    private void configurarTabla() {
        IDcorte.setCellValueFactory(new PropertyValueFactory<>("totalcantidad"));
        Fecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        CantidadC.setCellValueFactory(new PropertyValueFactory<>("totalVentas"));
        datosTabla = FXCollections.observableArrayList();
    }

    @SuppressWarnings("exports")
    public List<DetallesVenta> obtenerListaDeDetalleVentas() {
        Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = emf.createEntityManager();
        TypedQuery<DetallesVenta> query = entityManager.createQuery("SELECT dv FROM DetallesVenta dv", DetallesVenta.class);
        List<DetallesVenta> detalles = query.getResultList();

        entityManager.close();
        emf.close();

        return detalles;
    }

    public List<Ventas> obtenerListaDeVentas() {
        Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = emf.createEntityManager();
        TypedQuery<Ventas> query = entityManager.createQuery("SELECT v FROM Ventas v JOIN FETCH v.detalles", Ventas.class);
        List<Ventas> ventas = query.getResultList();
        entityManager.close();
        emf.close();
        return ventas;
    }

    private void mostrarTodosLosCortesEnTabla() {
        List<CortedeCaja> cortesDeCaja = obtenerCorteDeCaja(null);
        datosTabla.clear();
        datosTabla.addAll(cortesDeCaja);
        Corte.setItems(datosTabla);
    }

    private void filtrarCortePorFecha(LocalDate fecha) {
        List<CortedeCaja> cortesFiltrados = obtenerCorteDeCaja(fecha);
        datosTabla.clear();
        datosTabla.addAll(cortesFiltrados);
        Corte.setItems(datosTabla);
    }

    public List<CortedeCaja> obtenerCorteDeCaja(LocalDate fecha) {
    List<Ventas> ventasFiltradas = new ArrayList<>();
    BigDecimal totalVentas = BigDecimal.ZERO;
    BigDecimal totalCantidadProductos = BigDecimal.ZERO;

    for (Ventas venta : listaVentas) {
        LocalDate fechaVenta = venta.getFecha().toLocalDate();
        if (fecha == null || fechaVenta.equals(fecha)) {
            ventasFiltradas.add(venta);
            totalVentas = totalVentas.add(venta.getTotal());
            for (DetallesVenta detalle : venta.getDetalles()) {
                Productos producto = detalle.getProducto();
                BigDecimal cantidad = detalle.getCantidad();
                totalCantidadProductos = totalCantidadProductos.add(cantidad);
                // Aquí puedes acceder a las propiedades del producto y realizar operaciones adicionales si es necesario
            }
        }
    }

    List<CortedeCaja> cortesDeCaja = new ArrayList<>();
    if (!ventasFiltradas.isEmpty()) {
        LocalDateTime fechaCorte = ventasFiltradas.get(0).getFecha();
        CortedeCaja corte = new CortedeCaja(null, fechaCorte, totalVentas, totalCantidadProductos);
        cortesDeCaja.add(corte);
    }

    return cortesDeCaja;
}
@FXML
private void corte(ActionEvent event) {
    cortecaja();
    
}
    private void cortecaja() {
        if(datePicker.getValue()==null){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("El valor es nulo");
            alert.setHeaderText(null);
            alert.setContentText("seleccione una fecha");
            alert.showAndWait();
            datePicker.setValue(null);
        }
        else{
            BigDecimal total=null;
            Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
            SessionFactory sessionFactory = configuration.buildSessionFactory();
            EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
            EntityManager entityManager = emf.createEntityManager();
            entityManager.getTransaction().begin();
        
            try {
                for (CortedeCaja corte : datosTabla) {
                    // Guardar cada instancia de CortedeCaja en la base de datos
                    total=corte.getTotalVentas();
                    entityManager.persist(corte);
                }
        
                entityManager.getTransaction().commit();
                System.out.println("Datos de corte de caja agregados correctamente");
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("SE hizo corte de caja");
                    alert.setHeaderText(null);
                    alert.setContentText("La fecha del corte de caja es "+datePicker.getValue()+" el total de venta es "+total);
                    alert.showAndWait();
                    datePicker.setValue(null);
                    
            } catch (Exception e) {
                entityManager.getTransaction().rollback();
                e.printStackTrace();
            } finally {
                entityManager.close();
                emf.close();
            }
        }
    }
    @FXML
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.F5) {
            cortecaja();
        }
    }
}