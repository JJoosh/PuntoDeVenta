package com.app.controllers.Ventas;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
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
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
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
    @FXML TabPane tabpane;
    @FXML private Slider slider;

    @FXML private TextField txtPorcentaje;
    @FXML
    private ListView<Clientes> clienteListView;

    @FXML
    private ToggleButton clienteCheckBox;

    private ObservableList<Clientes> clientesData = FXCollections.observableArrayList();
    @FXML 
    private Pane rootPane;
    
    @FXML private ListView<Productos> listProductos;

    private ObservableList<Productos> productosData = FXCollections.observableArrayList();
    private ObservableList<Productos> productosAgregados = FXCollections.observableArrayList();
    private BigDecimal importeTotal = BigDecimal.ZERO;
    private BufferedReader reader;
    private List<ObservableList<Productos>> productosDataList = new ArrayList<>();
    private List<TableView<Productos>> tablaProductosList = new ArrayList<>();

    private bascula peso;
    private void initializeTab(TableView<Productos> tablaProductos) {
        int index = tablaProductosList.indexOf(tablaProductos);
        if (index >= 0 && index < productosDataList.size()) {
            tablaProductos.setItems(productosDataList.get(index));
        }
    }
    public void initialize() {

        txtCliente.setVisible(false);
        clienteListView.setVisible(false);
        txtPorcentaje.setVisible(false);
        slider.setVisible(false);
        tablaProductosList.add(tablaProductos);
        productosDataList.add(FXCollections.observableArrayList());
        // Configurar el CheckBox para mostrar/ocultar el TextField y el 
        initializeTab(tablaProductos);

        listProductos.setVisible(false);
        tabpane.getTabs().addListener((ListChangeListener<Tab>) c -> {
        while (c.next()) {
            if (c.wasRemoved()) {
                for (Tab tab : c.getRemoved()) {
                    int index = tablaProductosList.indexOf(tab.getContent());
                    if (index != -1) {
                        tablaProductosList.remove(index);
                        productosDataList.remove(index);
                    }
                }
            }
        }
    });
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
        
        slider.setBlockIncrement(1);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setSnapToTicks(true);
    
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

        codigoProductoTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DOWN) {
                listProductos.requestFocus(); 
                listProductos.getSelectionModel().selectFirst(); 
            }
        });
    
      
        listProductos.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
               
                String selectedProductName = listProductos.getSelectionModel().getSelectedItem().getNombre();
                codigoProductoTextField.setText(selectedProductName); 
                listProductos.setVisible(false); 
            }
        });
        
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
            if(newValue.isEmpty())listProductos.setVisible(false);
        });
    
        rootPane.setOnKeyPressed(this::handleKeyPressed);
    
        Platform.runLater(() -> codigoProductoTextField.requestFocus());
    }
    @FXML
private void addNewTicket() {
    Tab newTab = new Tab("Ticket " + (tabpane.getTabs().size() + 1));
    newTab.setClosable(true);
    
    TableView<Productos> newTablaProductos = new TableView<>();
    newTablaProductos.setId("tablaProductos");
    
    // Configurar las columnas de la nueva tabla
    TableColumn<Productos, Long> newCbarra = new TableColumn<>("Código de barras");
    newCbarra.setCellValueFactory(new PropertyValueFactory<>("id"));

    TableColumn<Productos, String> newDescriptions = new TableColumn<>("Descripción producto");
    newDescriptions.setCellValueFactory(new PropertyValueFactory<>("nombre"));

    TableColumn<Productos, BigDecimal> newPrecioV = new TableColumn<>("Precio de venta");
    newPrecioV.setCellValueFactory(new PropertyValueFactory<>("precio"));

    TableColumn<Productos, BigDecimal> newCantidad = new TableColumn<>("Cantidad");
    newCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

    newTablaProductos.getColumns().addAll(newCbarra, newDescriptions, newPrecioV, newCantidad);
    newTablaProductos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    
    AnchorPane anchorPane = new AnchorPane();
    anchorPane.getChildren().add(newTablaProductos);
    AnchorPane.setTopAnchor(newTablaProductos, 0.0);
    AnchorPane.setRightAnchor(newTablaProductos, 0.0);
    AnchorPane.setBottomAnchor(newTablaProductos, 0.0);
    AnchorPane.setLeftAnchor(newTablaProductos, 0.0);
    
    newTab.setContent(anchorPane);

    tabpane.getTabs().add(newTab);
    tablaProductosList.add(newTablaProductos);
    productosDataList.add(FXCollections.observableArrayList());
    
    // Seleccionar la nueva pestaña
    tabpane.getSelectionModel().select(newTab);

    // Inicializar la tabla
    initializeTab(newTablaProductos);
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
        ObservableList<Productos> productosEncontrados = buscarProductosPorCodigoONombre(consultaTexto);
        
        listProductos.setItems(productosEncontrados);
        listProductos.setVisible(!productosEncontrados.isEmpty());
    
        // Ajustar la altura de la lista según el número de elementos filtrados
        listProductos.setPrefHeight(productosEncontrados.size() * 24 + 2);
    }

    private ObservableList<Productos> buscarProductosPorCodigoONombre(String consultaTexto) {
        Configuration configuration = new Configuration().configure();
        configuration.addAnnotatedClass(Productos.class);
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory entityManagerFactory = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        // Realizar la consulta en la base de datos
        String query = "SELECT p FROM Productos p WHERE (p.id LIKE :consultaTexto OR p.nombre LIKE :consultaTexto) AND p.activo = 'S'";
        TypedQuery<Productos> typedQuery = entityManager.createQuery(query, Productos.class);
        typedQuery.setParameter("consultaTexto", "%" + consultaTexto + "%");
        List<Productos> productosList = typedQuery.getResultList();

        entityManager.close();
        entityManagerFactory.close();

        // Convertir la lista a ObservableList
        ObservableList<Productos> productosEncontrados = FXCollections.observableArrayList(productosList);
        return productosEncontrados;
    }
    @FXML
public void agregarProducto() {
    String codigoProducto = codigoProductoTextField.getText();
    ObservableList<Productos> productosEncontrados = buscarProductosPorCodigoONombre(codigoProducto);

    if (!productosEncontrados.isEmpty()) {
        Productos producto = productosEncontrados.get(0);

        try {
            BigDecimal cantidad = new BigDecimal(pesoTextField.getText().trim());
            producto.setCantidad(cantidad);
        } catch (NumberFormatException e) {
            mostrarAlertaError("Error", "La cantidad ingresada no es válida.");
            return;
        }

        Tab selectedTab = tabpane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            int tabIndex = tabpane.getTabs().indexOf(selectedTab);
            if (tabIndex >= 0 && tabIndex < tablaProductosList.size()) {
                TableView<Productos> tablaProductos = tablaProductosList.get(tabIndex);
                ObservableList<Productos> productosTab = productosDataList.get(tabIndex);
                productosTab.add(producto);
                tablaProductos.setItems(productosTab);
            } else {
                mostrarAlertaError("Error", "Índice de pestaña inválido.");
            }
        } else {
            mostrarAlertaError("Error", "No hay ningún Ticket abierto.");
        }
    } else {
        mostrarAlertaError("Error", "No se encontró ningún producto con el código ingresado.");
    }

    codigoProductoTextField.clear();
    pesoTextField.clear();
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
            // agregarProducto();

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