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
import com.app.controllers.Inventario.FXML_NewProducto;
import com.app.models.Productos;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class VentasController {

    private static String nombreUsser;
    private static String rol;

    public void setNombreUsser(String nombreUsser){
        this.nombreUsser=nombreUsser;
        
    }

    public void setRol(String rol){
        this.rol=rol;
    }

    public String getNombreUsser(){
        return this.nombreUsser;
    }

    public String getRol(){
        return this.rol;
    }

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
    private Label importe_total;
    @FXML
    private TextField cantidadTextField;
    
    @FXML private Label menu_ventas;
    @FXML private Label menu_inventario;
    @FXML private Label menu_kardex;
    @FXML private Label menu_devoluciones;
    @FXML private Label usuario;
    @FXML private Label importetotal;
    private ObservableList<Productos> productosData = FXCollections.observableArrayList();
    private ObservableList<Productos> productosAgregados = FXCollections.observableArrayList();
    private BigDecimal importeTotal = BigDecimal.ZERO;

    @FXML 
    private Pane rootPane;
    

    public void initialize() {
        
        
        Cbarra.setCellValueFactory(new PropertyValueFactory<>("id"));
        Descriptions.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        PrecioV.setCellValueFactory(new PropertyValueFactory<>("precio"));
        Cantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        tablaProductos.setItems(productosData);
        importe_total.setText("0.00");
        codigoProductoTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            buscarProductos();
        });
        rootPane.setOnKeyPressed(this::handleKeyPressed);
        menu_inventario.setOnMouseClicked(event -> {
            // Código que se ejecutará cuando se haga clic en el label
            abrirInventario();
        });
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.F2) {
            
            abrirInventario();
        
        }

        if(event.getCode()==KeyCode.F6){
            System.out.println("F6");
        }
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
                    BigDecimal cantidadIngresada = new BigDecimal(cantidadTexto);
                    BigDecimal cantidadDisponible = productoSeleccionado.getCantidad();

                    if (cantidadIngresada.compareTo(cantidadDisponible) <= 0) {
                        Productos nuevoProducto = new Productos(); // Crear una copia del producto
                        nuevoProducto.setCantidad(cantidadIngresada); // Establecer la cantidad ingresada
                        productosAgregados.add(nuevoProducto);
                        actualizarTotalImporte();
                        cantidadTextField.clear();
                    } else {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.setContentText("La cantidad ingresada excede la cantidad disponible");
                        alert.showAndWait();
                    }
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
        importe_total.setText(importeTotal.toString());
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
        importe_total.setText(importeTotal.toString());
     
    }
 
    
    public void abrirInventario() {
        try {
            // Cargar el archivo FXML con el nuevo contenido
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Inventario.fxml"));
            Pane nuevoContenido = loader.load();
            
            // Obtener el controlador del nuevo contenido
            FXMLInventarioController inventarioController = loader.getController();
            
            // Reemplazar el contenido del contenedor principal con el nuevo contenido
            rootPane.getChildren().setAll(nuevoContenido);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}