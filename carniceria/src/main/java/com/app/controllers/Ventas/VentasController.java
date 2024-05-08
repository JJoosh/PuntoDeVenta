package com.app.controllers.Ventas;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;


import com.app.controllers.Inventario.FXMLInventarioController;
import com.app.controllers.devoluciones.FXMLDevolucionesController;
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
import javafx.scene.Parent;

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

    private ObservableList<Productos> productosData = FXCollections.observableArrayList();
    private ObservableList<Productos> productosAgregados = FXCollections.observableArrayList();
    private BigDecimal importeTotal = BigDecimal.ZERO;

    public void initialize() {
        // Inicialización de la pantalla de ventas
        codigoProductoTextField.setText("");

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
            productosAgregados.add(productoSeleccionado);
            actualizarTotalImporte();
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
            total = total.add(producto.getPrecio());
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

   @FXML
    public void abrirInventario(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FXMLVista.fxml"));
            Parent root = loader.load();

            FXMLInventarioController newProductoController = loader.getController();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("dff");
        }
    }

    @FXML
    public void Devoluciones(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FXMLDevolucion.fxml"));
            Parent root = loader.load();
            FXMLDevolucionesController newProductoController = loader.getController();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("dff");
        }
    }
} 