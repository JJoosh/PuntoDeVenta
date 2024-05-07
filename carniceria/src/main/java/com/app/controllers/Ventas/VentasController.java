package com.app.controllers.Ventas;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.app.models.Productos;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

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
    private Label totalImporteLabel;
    @FXML
    private TextField cantidadTextField;

    private ObservableList<Productos> productosData = FXCollections.observableArrayList();
    private ObservableList<Productos> productosAgregados = FXCollections.observableArrayList();
    private BigDecimal importeTotal = BigDecimal.ZERO;

    public void initialize() {
        // Inicialización de la pantalla de ventas
        codigoProductoTextField.setText("");
        cantidadTextField.setText(""); // Inicializar el campo de texto vacío

        Cbarra.setCellValueFactory(new PropertyValueFactory<>("id"));
        Descriptions.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        PrecioV.setCellValueFactory(new PropertyValueFactory<>("precio"));
        Cantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        tablaProductos.setItems(productosData);
        totalImporteLabel.setText("0.00");

        codigoProductoTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            buscarProductos();
        });
    }

    @FXML
    private void buscarProductos() {
        String consultaTexto = codigoProductoTextField.getText();
        List<Productos> productosEncontrados = buscarProductosPorCodigoONombre(consultaTexto);
        actualizarTablaProductos(productosEncontrados);
    }

    private List<Productos> buscarProductosPorCodigoONombre(String consultaTexto) {
        Configuration configuration = new Configuration().configure();
        configuration.addAnnotatedClass(Productos.class);
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory entityManagerFactory = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        // Realizar la consulta en la base de datos
        String query = "SELECT p FROM Productos p WHERE p.id LIKE :consultaTexto OR p.nombre LIKE :consultaTexto";
        TypedQuery<Productos> typedQuery = entityManager.createQuery(query, Productos.class);
        typedQuery.setParameter("consultaTexto", "%" + consultaTexto + "%");
        List<Productos> productosEncontrados = typedQuery.getResultList();

        entityManager.close();
        entityManagerFactory.close();
        return productosEncontrados;
    }

    private void actualizarTablaProductos(List<Productos> productos) {
        productosData.clear();
        productosData.addAll(productos);
        tablaProductos.setItems(productosData);
    }

    @FXML
    private void agregarProducto() {
        Productos productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();

        if (productoSeleccionado != null) {
            String cantidadTexto = cantidadTextField.getText();
            if (!cantidadTexto.isEmpty()) {
                try {
                    BigDecimal cantidad = new BigDecimal(cantidadTexto);
                    Productos nuevoProducto = new Productos(productoSeleccionado); // Crear una copia del producto
                    nuevoProducto.setCantidad(cantidad); // Establecer la cantidad ingresada
                    restarDeInventario(productoSeleccionado.getId(), cantidad); // Restar del inventario
                    productosAgregados.add(nuevoProducto);
                    actualizarTotalImporte();
                    cantidadTextField.clear();
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Cantidad inválida");
                    alert.showAndWait();
                }
            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Debe ingresar una cantidad");
                alert.showAndWait();
            }
        } else {
            String codigoProductoTexto = codigoProductoTextField.getText();

            if (!codigoProductoTexto.isEmpty()) {
                try {
                    Long codigoProducto = Long.parseLong(codigoProductoTexto);
                    Productos producto = buscarProductoPorCodigo(codigoProducto);

                    if (producto != null) {
                        productosAgregados.add(producto);
                        actualizarTotalImporte();
                        codigoProductoTextField.clear();
                    } else {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.setContentText("Producto no encontrado");
                        alert.showAndWait();
                    }
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Código de producto inválido");
                    alert.showAndWait();
                }
            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Debe ingresar un código de producto o seleccionar uno de la tabla");
                alert.showAndWait();
            }
        }
    }

    private void actualizarTotalImporte() {
        BigDecimal total = BigDecimal.ZERO;
        for (Productos producto : productosAgregados) {
            BigDecimal cantidad = producto.getCantidad();
            BigDecimal precio = producto.getPrecio();
            BigDecimal subtotal = cantidad.multiply(precio);
            total = total.add(subtotal);
        }
        importeTotal = total;
        totalImporteLabel.setText(importeTotal.toString());
    }

    private Productos buscarProductoPorCodigo(Long codigoProducto) {
        Configuration configuration = new Configuration().configure();
        configuration.addAnnotatedClass(Productos.class);
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory entityManagerFactory = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        Productos producto = entityManager.find(Productos.class, codigoProducto);

        entityManager.close();
        entityManagerFactory.close();
        return producto;
    }

    public void borrarArticulo() {
        Productos productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado != null) {
            productosAgregados.remove(productoSeleccionado);
            actualizarTotalImporte();
            // actualizarTablaProductosAgregados();
        } else {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Debe seleccionar un artículo para eliminar.");
            alert.showAndWait();
        }
    }

    @FXML
    private void cobrar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Compra.fxml"));
            Scene scene = new Scene(loader.load());

            CompraController compraController = loader.getController();
            compraController.initData(productosAgregados, importeTotal);

            Stage stage = (Stage) codigoProductoTextField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void actualizarDatos(ObservableList<Productos> productosData, BigDecimal importeTotal) {
        this.productosData = productosData;
        this.importeTotal = importeTotal;
        totalImporteLabel.setText(importeTotal.toString());
        tablaProductos.setItems(productosData);
    }

    private void restarDeInventario(Long codigoProducto, BigDecimal cantidad) {
        Configuration configuration = new Configuration().configure();
        configuration.addAnnotatedClass(Productos.class);
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory entityManagerFactory = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try {
            entityManager.getTransaction().begin();

            // Buscar el producto por su código
            Productos producto = entityManager.find(Productos.class, codigoProducto);

            if (producto != null) {
                BigDecimal cantidadActual = producto.getCantidad();

                if (cantidadActual.compareTo(cantidad) >= 0) {
                    // Si hay suficiente cantidad en el inventario, restar la cantidad
                    BigDecimal nuevaCantidad = cantidadActual.subtract(cantidad);
                    producto.setCantidad(nuevaCantidad);
                    entityManager.merge(producto);
                } else {
                    // Si no hay suficiente cantidad en el inventario, mostrar un error
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("No hay suficiente cantidad en el inventario para este producto.");
                    alert.showAndWait();
                }
            } else {
                // Si no se encuentra el producto, mostrar un error
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("No se encontró el producto con el código especificado.");
                alert.showAndWait();
            }

            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            entityManager.close();
            entityManagerFactory.close();
        }
    }
}