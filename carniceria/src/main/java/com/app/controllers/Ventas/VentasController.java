package com.app.controllers.Ventas;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.TooManyListenersException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.app.controllers.Inventario.FXMLInventarioController;
import com.app.controllers.devoluciones.FXMLDevolucionesController;
import com.app.models.Productos;
import com.ibm.icu.text.DecimalFormat;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class VentasController {

    private static String nombreUsser;
    private static String rol;

    public void setNombreUsser(String nombreUsser) {
        this.nombreUsser = nombreUsser;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getNombreUsser() {
        return this.nombreUsser;
    }

    public String getRol() {
        return this.rol;
    }

    @FXML
    private Button btnbuscarcode1;

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
    private TextField pesoTextField;

    @FXML 
    private Pane rootPane;
    

    private ObservableList<Productos> productosData = FXCollections.observableArrayList();
    private ObservableList<Productos> productosAgregados = FXCollections.observableArrayList();
    private BigDecimal importeTotal = BigDecimal.ZERO;
    private BufferedReader reader;

    private bascula peso;

    public void initialize() {
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
        



        rootPane.setOnKeyPressed(this::handleKeyPressed);

        
        Platform.runLater(() -> codigoProductoTextField.requestFocus());

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
            String cantidadTexto = pesoTextField.getText();
            if (cantidadTexto.isEmpty()) {
                try {
                    obtenerPesoBascula();
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Error al obtener el peso de la báscula: " + e.getMessage());
                    alert.showAndWait();
                    return;
                }
            } else {
                try {
                    BigDecimal cantidadIngresada = new BigDecimal(cantidadTexto);
                    BigDecimal cantidadDisponible = productoSeleccionado.getCantidad();

                    if (cantidadIngresada.compareTo(cantidadDisponible) <= 0) {
                        Productos nuevoProducto = new Productos(productoSeleccionado); // Crear una copia del producto
                        nuevoProducto.setCantidad(cantidadIngresada); // Establecer la cantidad ingresada
                        productosAgregados.add(nuevoProducto);
                        actualizarTotalImporte(); // Actualizar el importe con la cantidad ingresada
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.setContentText("La cantidad ingresada excede la cantidad disponible");
                        alert.showAndWait();
                    }
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Cantidad inválida");
                    alert.showAndWait();
                }
            }
        } else {
            String codigoProductoTexto = codigoProductoTextField.getText();

            if (!codigoProductoTexto.isEmpty()) {
                try {
                    Long codigoProducto = Long.parseLong(codigoProductoTexto);
                    Productos producto = buscarProductoPorCodigo(codigoProducto);

                    if (producto != null) {
                        productosAgregados.add(producto);
                        actualizarTotalImporte(); // Actualizar el importe con el precio del producto
                        codigoProductoTextField.clear();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.setContentText("Producto no encontrado");
                        alert.showAndWait();
                    }
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Código de producto inválido");
                    alert.showAndWait();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Debe ingresar un código de producto o seleccionar uno de la tabla");
                alert.showAndWait();
            }
        }

        Platform.runLater(() -> btnbuscarcode1.requestFocus());
    }

    public void actualizarTotalImporte() {
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

    @FXML
    public void borrarArticulo() {
        productosAgregados.clear(); // Limpiar la lista de productos agregados
        actualizarTotalImporte(); // Actualizar el importe total a cero
        totalImporteLabel.setText("0"); // Limpiar la etiqueta del importe total
    }

    @FXML
private void cobrar() {
    cerrarBascula();
    if (importeTotal.compareTo(BigDecimal.ZERO) == 0) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Debe agregar al menos un producto y su cantidad");
        alert.showAndWait();
    } else {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Compra.fxml"));
            Pane nuevoContenido = loader.load();
            
            // Obtener el controlador de CompraController
            CompraController compraController = loader.getController();
            
            // Pasar los datos necesarios a CompraController
            compraController.initData(productosAgregados, importeTotal);
            
            rootPane.getChildren().setAll(nuevoContenido);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

    public void actualizarDatos(ObservableList<Productos> productosData, BigDecimal importeTotal) {
        this.productosData = productosData;
        this.importeTotal = importeTotal;
        totalImporteLabel.setText(importeTotal.toString());
    
        // Actualizar la tabla de productos
        tablaProductos.setItems(productosData);
    }
    public void obtenerPesoBascula() {
        try {
            if (peso == null) {
                peso = new bascula("COM3"); // Suponiendo que el puerto de la báscula es COM3
                peso.setVentasController(this); // Establecer la referencia a VentasController en bascula
            }
            peso.sendCommand("P");
        } catch (PortInUseException | NoSuchPortException | UnsupportedCommOperationException | IOException
                | TooManyListenersException e) {
            e.printStackTrace();
            System.out.println("Error al comunicarse con la báscula: " + e.getMessage());
        }
    }

    public void actualizarPesoDesdeBascula(String peso) {
        pesoTextField.setText(peso);
    }

    public void cerrarBascula() {
        if (peso != null) {
            peso.close();
            System.err.println("No jalo aca");
        }
    }

    public void actualizarProductosAgregados(ObservableList<Productos> productosData) {
        this.productosAgregados.clear();
        this.productosAgregados.addAll(productosData);
    }
    

    public BigDecimal getImporteTotal() {
        return this.importeTotal;
    }

    public ObservableList<Productos> getProductosAgregados() {
        return productosAgregados;
    }

    public void cargarProductos() {
        Configuration configuration = new Configuration().configure();
        configuration.addAnnotatedClass(Productos.class);
        SessionFactory sessionFactory = null;
        EntityManagerFactory entityManagerFactory = null;
        EntityManager entityManager = null;

        try {
            sessionFactory = configuration.buildSessionFactory();
            entityManagerFactory = sessionFactory.unwrap(EntityManagerFactory.class);
            entityManager = entityManagerFactory.createEntityManager();

            // Realizar la consulta para obtener todos los productos
            TypedQuery<Productos> query = entityManager.createQuery("SELECT p FROM Productos p", Productos.class);
            List<Productos> productos = query.getResultList();

            // Actualizar la tabla de productos
            productosData.clear();
            productosData.addAll(productos);
            tablaProductos.setItems(productosData);
        } catch (Exception e) {
            e.printStackTrace();
            // Manejar la excepción adecuadamente, mostrar un mensaje de error, etc.
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
            if (entityManagerFactory != null) {
                entityManagerFactory.close();
            }
            if (sessionFactory != null) {
                sessionFactory.close();
            }
        }
    }

    @FXML
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.F6) {
            cobrar();
        }

        if (event.getCode() == KeyCode.F5) {
            agregarProducto();

        }
        if (event.getCode() == KeyCode.F7) {
            obtenerPesoBascula();
        }

        if (event.getCode() == KeyCode.F8) {
            borrarArticulo();
        }

        if(event.getCode()==KeyCode.F2){
            abrirInventario();
        }
        if(event.getCode()==KeyCode.F4){
            abrirCorteCaja();
        }

        if(event.getCode()==KeyCode.F3){
            abrirDevoluciones();
        }
    }

    public void abrirDevoluciones(){
        try {
            // Cargar el archivo FXML con el nuevo contenido
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FXMLDevolucion.fxml"));
            Pane nuevoContenido = loader.load();
            
            // Obtener el controlador del nuevo contenido
            FXMLDevolucionesController inventarioController = loader.getController();
            
            // Reemplazar el contenido del contenedor principal con el nuevo contenido
            rootPane.getChildren().setAll(nuevoContenido);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void abrirCorteCaja(){
        try {
            // Cargar el archivo FXML con el nuevo contenido
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FXMLDevolucion.fxml"));
            Pane nuevoContenido = loader.load();
            
            // Obtener el controlador del nuevo contenido
            FXMLDevolucionesController inventarioController = loader.getController();
            
            // Reemplazar el contenido del contenedor principal con el nuevo contenido
            rootPane.getChildren().setAll(nuevoContenido);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void abrirInventario(){
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