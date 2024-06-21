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
import com.app.models.Clientes;
import com.app.models.Productos;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;

public class VentasController {

    private static String nombreUsser;
    private static String rol;
    private final Clientes[] clienteSeleccionado = {null}; // Array de una sola posición para almacenar el cliente seleccionado

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
    private TextField txtCliente;

    @FXML private Slider slider;

    @FXML private TextField txtPorcentaje;
    @FXML
    private ListView<Clientes> clienteListView;

    @FXML
    private ToggleButton clienteCheckBox;

    private ObservableList<Clientes> clientesData = FXCollections.observableArrayList();
    @FXML 
    private Pane rootPane;
    

    private ObservableList<Productos> productosData = FXCollections.observableArrayList();
    private ObservableList<Productos> productosAgregados = FXCollections.observableArrayList();
    private BigDecimal importeTotal = BigDecimal.ZERO;
    private BufferedReader reader;

    private bascula peso;

    public void initialize() {

        txtCliente.setVisible(false);
        clienteListView.setVisible(false);
        txtPorcentaje.setVisible(false);
        slider.setVisible(false);
    
        // Configurar el CheckBox para mostrar/ocultar el TextField y el Slider
        clienteCheckBox.setOnAction(event -> {
            boolean isSelected = clienteCheckBox.isSelected();
            txtCliente.setVisible(isSelected);
            clienteListView.setVisible(false);
            txtPorcentaje.setVisible(isSelected);
            slider.setVisible(isSelected);
        });
    
        // Configurar el TextField para filtrar los clientes
        txtCliente.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            String text = txtCliente.getText();
            if (text.isEmpty()) {
                clienteListView.setVisible(false);
            } else {
                ObservableList<Clientes> filteredItems = FXCollections.observableArrayList();
                for (Clientes cliente : clientesData) {
                    if (cliente.getNombre().toLowerCase().contains(text.toLowerCase())) {
                        filteredItems.add(cliente);
                    }
                }
                clienteListView.setItems(filteredItems);
                clienteListView.setVisible(!filteredItems.isEmpty());
    
                // Ajustar la altura de la lista según el número de elementos filtrados
                clienteListView.setPrefHeight(filteredItems.size() * 24 + 2);
            }
        });
    
        // Configurar el Slider para que suba en incrementos enteros
        slider.setBlockIncrement(1);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setSnapToTicks(true);
    
        // Agregar un ChangeListener al Slider para actualizar el TextField en tiempo real
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            txtPorcentaje.setText(String.format("%d%%", newValue.intValue()));
        });
    
        // Agregar un manejador de eventos para actualizar el Slider cuando se presione Enter en el TextField
        txtPorcentaje.setOnAction(event -> {
            try {
                // Remover el símbolo de porcentaje y convertir a número entero
                String text = txtPorcentaje.getText().replace("%", "").trim();
                int value = Integer.parseInt(text);
                // Actualizar el Slider solo si el valor está dentro de los límites del Slider
                if (value >= slider.getMin() && value <= slider.getMax()) {
                    slider.setValue(value);
                }
            } catch (NumberFormatException e) {
                // Manejar la excepción si la entrada no es un número válido
                System.out.println("Invalid input: " + txtPorcentaje.getText());
            }
        });
    
        // Cargar los datos de los clientes
        cargarClientes();
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
    
    private void cargarClientes() {
        Configuration configuration = new Configuration().configure();
        configuration.addAnnotatedClass(Clientes.class);
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory entityManagerFactory = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        String query = "SELECT c FROM Clientes c WHERE c.activo = 'A'";
        TypedQuery<Clientes> typedQuery = entityManager.createQuery(query, Clientes.class);
        List<Clientes> clientesEncontrados = typedQuery.getResultList();

        entityManager.close();
        entityManagerFactory.close();
        clientesData.addAll(clientesEncontrados);
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
        String query = "SELECT p FROM Productos p WHERE (p.id LIKE :consultaTexto OR p.nombre LIKE :consultaTexto) AND p.activo = 'S'";
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
            
            CompraController compraController = loader.getController();

            // Obtener el ID del cliente seleccionado si el checkbox está seleccionado
            Long clienteId = null;
            if (clienteCheckBox.isSelected() && clienteListView.getSelectionModel().getSelectedItem() != null) {
                clienteId = (long) clienteListView.getSelectionModel().getSelectedItem().getId();
                compraController.getIDandDescuento( clienteListView.getSelectionModel().getSelectedItem(), 2);  //AQUI TERMINAR
                System.out.println("ID del cliente seleccionado: " + clienteId);
            } else {
                System.out.println("No se ha seleccionado ningún cliente.");
            }

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
                peso = new bascula("COM4"); // Suponiendo que el puerto de la báscula es COM3
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
            TypedQuery<Productos> query = entityManager.createQuery("SELECT p FROM Productos p WHERE p.activo='S'", Productos.class);
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

    
   @FXML 
    public void abrirVentasHechas() {
        try {
            // Cargar el archivo FXML con el nuevo contenido
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/VentasHechas.fxml"));
            Pane nuevoContenido = loader.load();

            // Obtener el controlador del nuevo contenido
            VentasHechas ventasHechasController = loader.getController();

            // Reemplazar el contenido del contenedor principal con el nuevo contenido
            rootPane.getChildren().setAll(nuevoContenido);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


private void mostrarAlertaError(String titulo, String mensaje) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(titulo);
    alert.setHeaderText(null);
    alert.setContentText(mensaje);
    alert.showAndWait();
}
}