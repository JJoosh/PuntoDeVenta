package com.app.controllers.Ventas;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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
import javafx.stage.Stage;

public class VentasController {

    private static String nombreUsser;
    private static String rol;
    private final Clientes[] clienteSeleccionado = {null};

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
    private static Map<Tab, TableView<Productos>> tablaProductosMap = new HashMap<>();
    private static Map<Tab, ObservableList<Productos>> productosDataMap = new HashMap<>();
    private static Map<Tab, BigDecimal> importeTotalMap = new HashMap<>();
    private static int ticketCounter = 2;
    @FXML private ListView<Productos> listProductos;

    private ObservableList<Productos> productosData = FXCollections.observableArrayList();
    private ObservableList<Productos> productosAgregados = FXCollections.observableArrayList();
    private BigDecimal importeTotal = BigDecimal.ZERO;
    private BufferedReader reader;
    private List<ObservableList<Productos>> productosDataList = new ArrayList<>();
    private List<TableView<Productos>> tablaProductosList = new ArrayList<>();
    private static Map<String, List<Productos>> ticketsGuardados = new HashMap<>();

    private bascula peso;

    public void initialize() {
        txtCliente.setVisible(false);
        clienteListView.setVisible(false);
        txtPorcentaje.setVisible(false);
        slider.setVisible(false);
        cargarTicketsGuardados();
        rootPane.setOnKeyPressed(this::handleKeyPressed);
        listProductos.setVisible(false);
        
        tabpane.getTabs().addListener((ListChangeListener<Tab>) c -> {
            while (c.next()) {
                if (c.wasRemoved()) {
                    for (Tab tab : c.getRemoved()) {
                        productosDataMap.remove(tab);
                        tablaProductosMap.remove(tab);
                        importeTotalMap.remove(tab);
                    }
                }
            }
        });
        
        tabpane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                actualizarImporteTotalLabel();
            }
        });
    
        clienteCheckBox.setOnAction(event -> {
            boolean isSelected = clienteCheckBox.isSelected();
            txtCliente.setVisible(isSelected);
            clienteListView.setVisible(false);
            txtPorcentaje.setVisible(isSelected);
            slider.setVisible(isSelected);
        });
    
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
    
                clienteListView.setPrefHeight(filteredItems.size() * 24 + 2);
            }
        });
        
        slider.setBlockIncrement(1);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setSnapToTicks(true);
    
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            txtPorcentaje.setText(String.format("%d%%", newValue.intValue()));
            recalcularImporteTotalConDescuento(newValue.intValue());
        });
    
        txtPorcentaje.setOnAction(event -> {
            try {
                String text = txtPorcentaje.getText().replace("%", "").trim();
                int value = Integer.parseInt(text);
                if (value >= slider.getMin() && value <= slider.getMax()) {
                    slider.setValue(value);
                    recalcularImporteTotalConDescuento(value);
                }
            } catch (NumberFormatException e) {
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
     
        // Si no hay tickets guardados, crear uno por defecto
        if (productosDataMap.isEmpty()) {
            crearTicketDefault();
        }
    
        totalImporteLabel.setText("0.00");
        codigoProductoTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            buscarProductos();
            if(newValue.isEmpty()) listProductos.setVisible(false);
        });
    
        Platform.runLater(() -> codigoProductoTextField.requestFocus());
    }
   
    
    
    private void cargarTicketsGuardados() {
        if (ticketsGuardados.isEmpty()) {
            return; 
        }
    
        for (Map.Entry<String, List<Productos>> entry : ticketsGuardados.entrySet()) {
            String ticketName = entry.getKey();
            List<Productos> productos = entry.getValue();
    
            // Solo cargar el ticket si contiene productos
            if (!productos.isEmpty()) {
                Tab newTab = new Tab(ticketName);
                newTab.setClosable(true);
    
                TableView<Productos> newTablaProductos = crearNuevaTablaProductos();
                
                AnchorPane anchorPane = new AnchorPane();
                anchorPane.getChildren().add(newTablaProductos);
                AnchorPane.setTopAnchor(newTablaProductos, 0.0);
                AnchorPane.setRightAnchor(newTablaProductos, 0.0);
                AnchorPane.setBottomAnchor(newTablaProductos, 0.0);
                AnchorPane.setLeftAnchor(newTablaProductos, 0.0);
    
                newTab.setContent(anchorPane);
    
                ObservableList<Productos> productosData = FXCollections.observableArrayList(productos);
                productosDataMap.put(newTab, productosData);
                tablaProductosMap.put(newTab, newTablaProductos);
                
                // Calcular el importe total del ticket guardado
                BigDecimal importeTotal = BigDecimal.ZERO;
                for (Productos producto : productos) {
                    importeTotal = importeTotal.add(producto.getPrecio().multiply(producto.getCantidad()));
                }
                importeTotalMap.put(newTab, importeTotal);
                
                newTablaProductos.setItems(productosData);
    
                tabpane.getTabs().add(newTab);
            }
        }
        
        // Actualizar el importe total después de cargar todos los tickets
        actualizarImporteTotalLabel();
        
        // Si hay tickets cargados, seleccionar el primero
        if (!tabpane.getTabs().isEmpty()) {
            tabpane.getSelectionModel().select(0);
        }
    }

    private void crearTicketDefault() {
        Tab defaultTab = new Tab("Ticket 1");
        defaultTab.setClosable(true);
        
        TableView<Productos> newTablaProductos = crearNuevaTablaProductos();
        
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.getChildren().add(newTablaProductos);
        AnchorPane.setTopAnchor(newTablaProductos, 0.0);
        AnchorPane.setRightAnchor(newTablaProductos, 0.0);
        AnchorPane.setBottomAnchor(newTablaProductos, 0.0);
        AnchorPane.setLeftAnchor(newTablaProductos, 0.0);
        
        defaultTab.setContent(anchorPane);
        
        tablaProductosMap.put(defaultTab, newTablaProductos);
        productosDataMap.put(defaultTab, FXCollections.observableArrayList());
        importeTotalMap.put(defaultTab, BigDecimal.ZERO);
        
        tabpane.getTabs().add(defaultTab);
    }
    public void guardarTickets() {
        for (Map.Entry<Tab, ObservableList<Productos>> entry : productosDataMap.entrySet()) {
            String ticketName = entry.getKey().getText();
            ObservableList<Productos> productosData = entry.getValue();
            if (!productosData.isEmpty()) {
                recalcularImporteTotal(entry.getKey());
                List<Productos> productos = new ArrayList<>(productosData);
                ticketsGuardados.put(ticketName, productos);
            }
        }
        // Aquí deberías persistir ticketsGuardados (por ejemplo, guardarlo en un archivo o base de datos)
        // Por ejemplo:
        // guardarTicketsEnArchivo(ticketsGuardados);
    }

    private TableView<Productos> crearNuevaTablaProductos() {
        TableView<Productos> newTablaProductos = new TableView<>();
        newTablaProductos.setId("tablaProductos");

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

        return newTablaProductos;
    }

    public void onClose() {
        guardarTickets();
    }

    @FXML
private void addNewTicket() {
    Tab newTab = new Tab("Ticket " + ticketCounter++);
    newTab.setClosable(true);
    
    TableView<Productos> newTablaProductos = crearNuevaTablaProductos();
    
    AnchorPane anchorPane = new AnchorPane();
    anchorPane.getChildren().add(newTablaProductos);
    AnchorPane.setTopAnchor(newTablaProductos, 0.0);
    AnchorPane.setRightAnchor(newTablaProductos, 0.0);
    AnchorPane.setBottomAnchor(newTablaProductos, 0.0);
    AnchorPane.setLeftAnchor(newTablaProductos, 0.0);
    
    newTab.setContent(anchorPane);

    tablaProductosMap.put(newTab, newTablaProductos);
    productosDataMap.put(newTab, FXCollections.observableArrayList());
    importeTotalMap.put(newTab, BigDecimal.ZERO);

    tabpane.getTabs().add(newTab);
    
    tabpane.getSelectionModel().select(newTab);
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
            BigDecimal cantidadNueva = new BigDecimal(pesoTextField.getText().trim());
            
            Tab selectedTab = tabpane.getSelectionModel().getSelectedItem();
            if (selectedTab == null) {
                selectedTab = tabpane.getTabs().get(0);
            }

            ObservableList<Productos> productosData = productosDataMap.get(selectedTab);
            if (productosData == null) {
                productosData = FXCollections.observableArrayList();
                productosDataMap.put(selectedTab, productosData);
                tablaProductosMap.put(selectedTab, crearNuevaTablaProductos());
            }

            BigDecimal cantidadExistente = BigDecimal.ZERO;
            for (Productos p : productosData) {
                if (p.getId().equals(producto.getId())) {
                    cantidadExistente = p.getCantidad();
                    break;
                }
            }

            BigDecimal cantidadTotal = cantidadExistente.add(cantidadNueva);

            if (cantidadTotal.compareTo(BigDecimal.ZERO) > 0 && cantidadTotal.compareTo(producto.getCantidad()) <= 0) {
                boolean productoExistente = false;
                recalcularImporteTotal(selectedTab);
                actualizarImporteTotalLabel();
                for (Productos p : productosData) {
                    if (p.getId().equals(producto.getId())) {
                        p.setCantidad(cantidadTotal);
                        productoExistente = true;
                        break;
                    }
                }

                if (!productoExistente) {
                    producto.setCantidad(cantidadNueva);
                    productosData.add(producto);
                }

                BigDecimal subtotal = producto.getPrecio().multiply(cantidadNueva);
                BigDecimal nuevoTotal = importeTotalMap.getOrDefault(selectedTab, BigDecimal.ZERO).add(subtotal);
                importeTotalMap.put(selectedTab, nuevoTotal);

                actualizarImporteTotalLabel();

                TableView<Productos> tablaProductos = tablaProductosMap.get(selectedTab);
                if (tablaProductos != null) {
                    tablaProductos.setItems(productosData);
                    tablaProductos.refresh();
                }

                codigoProductoTextField.clear();
                pesoTextField.clear();
                codigoProductoTextField.requestFocus();
            } else {
                mostrarAlertaError("Error", "No hay suficiente cantidad del producto para realizar la venta");
            }
        } catch (NumberFormatException e) {
            mostrarAlertaError("Error", "La cantidad ingresada no es válida.");
        }
    } else {
        mostrarAlertaError("Error", "No se encontró ningún producto con el código ingresado.");
    }
}
    
   
private void actualizarImporteTotalLabel() {
    Tab selectedTab = tabpane.getSelectionModel().getSelectedItem();
    if (selectedTab != null) {
        BigDecimal importeTotal = importeTotalMap.get(selectedTab);
        if (importeTotal != null) {
            int porcentajeDescuento = (int) slider.getValue();
            BigDecimal descuento = importeTotal.multiply(BigDecimal.valueOf(porcentajeDescuento).divide(BigDecimal.valueOf(100)));
            BigDecimal importeConDescuento = importeTotal.subtract(descuento);
            totalImporteLabel.setText(importeConDescuento.setScale(2, RoundingMode.HALF_UP).toString());
        } else {
            totalImporteLabel.setText("0.00");
        }
    } else {
        totalImporteLabel.setText("0.00");
    }
}
    @FXML
    public void borrarArticulo() {
        Tab selectedTab = tabpane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            ObservableList<Productos> productosData = productosDataMap.get(selectedTab);
            if (productosData != null) {
                productosData.clear();
                recalcularImporteTotal(selectedTab);
                TableView<Productos> tablaProductos = tablaProductosMap.get(selectedTab);
                if (tablaProductos != null) {
                    tablaProductos.refresh();
                }
                actualizarImporteTotalLabel();
    
                ticketsGuardados.put(selectedTab.getText(), new ArrayList<>());
                guardarTickets();
            }
        }
    }

    private void recalcularImporteTotal(Tab tab) {
        ObservableList<Productos> productosData = productosDataMap.get(tab);
        BigDecimal importeTotal = BigDecimal.ZERO;
        if (productosData != null) {
            for (Productos producto : productosData) {
                importeTotal = importeTotal.add(producto.getPrecio().multiply(producto.getCantidad()));
            }
        }
        importeTotalMap.put(tab, importeTotal);
    }
    @FXML
private void cobrar() {
    cerrarBascula();

    Tab selectedTab = tabpane.getSelectionModel().getSelectedItem();
    if (selectedTab == null) {
        mostrarAlertaError("Error", "No hay ninguna pestaña seleccionada.");
        return;
    }

    ObservableList<Productos> productosData = productosDataMap.get(selectedTab);
    BigDecimal importeTotal = importeTotalMap.get(selectedTab);
    
    if (productosData == null || importeTotal == null || importeTotal.compareTo(BigDecimal.ZERO) == 0) {
        mostrarAlertaError("Error", "Debe agregar al menos un producto y su cantidad");
        return;
    }

    // Verificar si el toggleButton de clientes está seleccionado pero no se ha elegido un cliente
    if (clienteCheckBox.isSelected() && clienteListView.getSelectionModel().getSelectedItem() == null) {
        mostrarAlertaError("Error", "Ha seleccionado aplicar descuento de cliente pero no ha seleccionado ningún cliente. Por favor, seleccione un cliente o desactive la opción de descuento.");
        return;
    }

    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Compra.fxml"));
        Parent root = loader.load();
        
        CompraController compraController = loader.getController();
        compraController.setVentasController(this);

        Scene scene = new Scene(root);

        Stage newStage = new Stage();
        newStage.setScene(scene);
        newStage.centerOnScreen();

        newStage.show();

        Clientes clienteSeleccionado = null;
        String descuento = "0";
        if (clienteCheckBox.isSelected()) {
            clienteSeleccionado = clienteListView.getSelectionModel().getSelectedItem();
            descuento = txtPorcentaje.getText().replace("%", "");
        }

        // Calcular el importe con descuento
        BigDecimal descuentoPorcentaje = new BigDecimal(descuento).divide(new BigDecimal("100"));
        BigDecimal importeConDescuento = importeTotal.subtract(importeTotal.multiply(descuentoPorcentaje));

        compraController.initData(productosData, importeConDescuento, selectedTab);
        compraController.getIDandDescuento(clienteSeleccionado, descuento);
        
    } catch (IOException e) {
        e.printStackTrace();
    }
}
public void limpiarTablaProductos(Tab selectTab) {
    if (selectTab != null) {
        ObservableList<Productos> productosData = productosDataMap.get(selectTab);
        if (productosData != null) {
            productosData.clear();
            TableView<Productos> tablaProductos = tablaProductosMap.get(selectTab);
            if (tablaProductos != null) {
                tablaProductos.refresh();
            }
            importeTotalMap.put(selectTab, BigDecimal.ZERO);
            actualizarImporteTotalLabel();
            
            // Eliminar el ticket del mapa ticketsGuardados
            ticketsGuardados.remove(selectTab.getText());
            guardarTickets(); // Actualiza los tickets guardados inmediatamente
        }
    } else {
        System.err.println("El Tab seleccionado es nulo.");
    }
}
    

    
    
    public void actualizarDatos(ObservableList<Productos> productosData, BigDecimal importeTotal) {
        Tab selectedTab = tabpane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            ObservableList<Productos> currentProductosData = productosDataMap.get(selectedTab);
            if (currentProductosData != null) {
                currentProductosData.setAll(productosData);
            } else {
                productosDataMap.put(selectedTab, FXCollections.observableArrayList(productosData));
            }
            
            importeTotalMap.put(selectedTab, importeTotal);
            
            TableView<Productos> tablaProductos = tablaProductosMap.get(selectedTab);
            if (tablaProductos != null) {
                tablaProductos.refresh();
            }
            
            actualizarImporteTotalLabel();
        }
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
    
            // Actualizar la tabla de productos de la pestaña seleccionada
            Tab selectedTab = tabpane.getSelectionModel().getSelectedItem();
            if (selectedTab != null) {
                ObservableList<Productos> productosData = productosDataMap.get(selectedTab);
                if (productosData != null) {
                    productosData.clear();
                    productosData.addAll(productos);
                    
                    TableView<Productos> tablaProductos = tablaProductosMap.get(selectedTab);
                    if (tablaProductos != null) {
                        tablaProductos.setItems(productosData);
                        tablaProductos.refresh();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlertaError("Error", "Hubo un problema al cargar los productos.");
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
            onClose();
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
            onClose();
            abrirInventario();
        }
        if(event.getCode()==KeyCode.F4){
            onClose();
            abrirCorteCaja();
        }

        if(event.getCode()==KeyCode.F3){
            onClose();
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
            onClose();
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
public void refrescarVistaVentas() {
    Platform.runLater(() -> {
        // Guarda el índice de la pestaña seleccionada actualmente
        int selectedIndex = tabpane.getSelectionModel().getSelectedIndex();

        // Limpia todas las tabs
        tabpane.getTabs().clear();

        // Limpia los mapas
        productosDataMap.clear();
        tablaProductosMap.clear();
        importeTotalMap.clear();

        // Recarga los tickets guardados
        cargarTicketsGuardados();

        // Si no hay tabs después de cargar, crea una nueva
        if (tabpane.getTabs().isEmpty()) {
            addNewTicket();
        }

        // Selecciona la pestaña que estaba seleccionada anteriormente, o la primera si no es posible
        if (selectedIndex >= 0 && selectedIndex < tabpane.getTabs().size()) {
            tabpane.getSelectionModel().select(selectedIndex);
        } else {
            tabpane.getSelectionModel().selectFirst();
        }

        // Actualiza la vista de cada tabla de productos
        for (Tab tab : tabpane.getTabs()) {
            TableView<Productos> tablaProductos = tablaProductosMap.get(tab);
            if (tablaProductos != null) {
                tablaProductos.refresh();
            }
        }

        actualizarImporteTotalLabel();
    });
}

private void recalcularImporteTotalConDescuento(int porcentajeDescuento) {
    Tab selectedTab = tabpane.getSelectionModel().getSelectedItem();
    if (selectedTab != null) {
        BigDecimal importeTotal = importeTotalMap.get(selectedTab);
        if (importeTotal != null) {
            BigDecimal descuento = importeTotal.multiply(BigDecimal.valueOf(porcentajeDescuento).divide(BigDecimal.valueOf(100)));
            BigDecimal importeConDescuento = importeTotal.subtract(descuento);
            totalImporteLabel.setText(importeConDescuento.setScale(2, RoundingMode.HALF_UP).toString());
        }
    }
}
public void eliminarTicketCompletamente(Tab tab) {
    Platform.runLater(() -> {
        ObservableList<Productos> productosData = productosDataMap.get(tab);
        if (productosData != null) {
            productosData.clear();
        }
        productosDataMap.remove(tab);
        tablaProductosMap.remove(tab);
        importeTotalMap.remove(tab);
        tabpane.getTabs().remove(tab);
        ticketsGuardados.remove(tab.getText());
        guardarTickets(); // Actualiza los tickets guardados inmediatamente
        
        // Si no quedan tabs, crea uno nuevo
        if (tabpane.getTabs().isEmpty()) {
            addNewTicket();
        }
        
        // Actualiza la vista
        tabpane.requestLayout();
    });
}

} 



