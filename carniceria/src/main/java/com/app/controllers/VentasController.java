package com.app.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.app.models.DetallesVenta;
import com.app.models.Productos;
import com.app.models.Ventas;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class VentasController {

    @FXML
    private TextField codigoProductoTextField;
    @FXML
    private TableView<Productos> tablaProductos;
    @FXML
    private TableColumn<Productos, Long> Cbarra;
    @FXML
    private TableColumn<Productos, String> Descriptions;
    @FXML
    private TableColumn<Productos, BigDecimal> PrecioV;
    @FXML
    private TableColumn<Productos, BigDecimal> Cantidad;
    @FXML
    private TableColumn<Productos, BigDecimal> importe;
    @FXML
    private Button btnFinalizarVenta;
    @FXML
    private Label totalImporteLabel;
    private ObservableList<Productos> productosData;
    private BigDecimal importeTotal = BigDecimal.ZERO;

    public void initialize() {
        codigoProductoTextField.setText("");

        Cbarra.setCellValueFactory(new PropertyValueFactory<>("id"));
        Descriptions.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        PrecioV.setCellValueFactory(new PropertyValueFactory<>("precio"));
        Cantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        importe.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPrecio().multiply(cellData.getValue().getCantidad())));

        productosData = FXCollections.observableArrayList();
        tablaProductos.setItems(productosData);
        totalImporteLabel.setText("0.00");

        codigoProductoTextField.setOnKeyPressed(this::buscarProducto);
    }

    @FXML
    private void buscarProducto(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            agregarProducto();
        }
    }

    @FXML
    private void agregarProducto() {
        String codigoProductoTexto = codigoProductoTextField.getText();

        Configuration configuration = new Configuration().configure();
        configuration.addAnnotatedClass(Productos.class);
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory entityManagerFactory = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        if (!codigoProductoTexto.isEmpty()) {
            Long codigoProducto = Long.parseLong(codigoProductoTexto);
            Productos producto = entityManager.find(Productos.class, codigoProducto);

            if (producto != null) {
                // Cantidad fija de 20 kilogramos
                BigDecimal cantidad = new BigDecimal(20);

                producto.setCantidad(cantidad);
                BigDecimal importeProducto = producto.getPrecio().multiply(cantidad);
                importeTotal = importeTotal.add(importeProducto);
                totalImporteLabel.setText(importeTotal.toString());
                productosData.add(producto);
                tablaProductos.setItems(productosData);
                codigoProductoTextField.clear();
            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Producto no encontrado");
                alert.showAndWait();
            }
        }

        entityManager.close();
        entityManagerFactory.close();
    }

    @FXML
    private void finalizarVenta() {
        Configuration configuration = new Configuration().configure();
        configuration.addAnnotatedClass(Productos.class);
        configuration.addAnnotatedClass(Ventas.class);
        configuration.addAnnotatedClass(DetallesVenta.class);
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory entityManagerFactory = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();

        // Crear una nueva venta con el ticket generado
        Ventas venta = new Ventas();
        venta.setTicket(String.format("%06d", (int) (Math.random() * 1000000)));
        venta.setFecha(java.sql.Date.valueOf(LocalDate.now()));
        venta.setTotal(importeTotal.floatValue());
        entityManager.persist(venta);

        // Crear los detalles de la venta
        for (Productos producto : productosData) {
            DetallesVenta detalle = new DetallesVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(producto.getCantidad());
            detalle.setTotal(producto.getPrecio().multiply(producto.getCantidad()));
            entityManager.persist(detalle);
        }

        transaction.commit();

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText("Venta registrada con éxito. Ticket: " + venta.getTicket());
        alert.showAndWait();

        // Limpiar la tabla después de finalizar la venta
        productosData.clear();
        tablaProductos.setItems(productosData);
        totalImporteLabel.setText("0.00");
        importeTotal = BigDecimal.ZERO;

        entityManager.close();
        entityManagerFactory.close();
    }
}