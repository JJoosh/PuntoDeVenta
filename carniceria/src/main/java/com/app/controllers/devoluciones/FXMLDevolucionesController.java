package com.app.controllers.devoluciones;

import java.util.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import com.app.models.Ventas;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import com.app.controllers.Producto;
import com.app.controllers.Inventario.FXMLInventarioController;
import com.app.controllers.Inventario.FXML_NewProducto;
import com.app.controllers.Ventas.VentasController;
import com.app.controllers.devoluciones.tabledata;

import com.app.models.Categoria;
import com.app.models.DetallesVenta;
import com.app.models.Devoluciones;
import com.app.models.Productos;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import javafx.fxml.Initializable;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

public class FXMLDevolucionesController implements Initializable {
    @FXML
    private TextField cantidadDevuelta;
    @FXML
    private TextField ticket;
    @FXML
    private TableView<tabledata> tabladev;
    @FXML
    private TableColumn<tabledata, String> ticket1;

    @FXML
    private TableColumn<tabledata, Date> fecha;

    @FXML
    private TableColumn<tabledata, Float> total;

    @FXML
    private TableColumn<tabledata, BigDecimal> cantidad;

    @FXML
    private TableColumn<tabledata, String> nombre;

    @FXML
    private TableColumn<tabledata, Long> detalles;

    @FXML
    private Spinner<Double> spinner;

    @FXML
    private TextArea textoArea;

    @FXML
    private ComboBox<String> motivos;
    
    @FXML 
    private Pane rootPane;

    private ObservableList<tabledata> datosTabla = FXCollections.observableArrayList();

    List<Ventas> listaVentas;
    List<Productos> ListaProducto;
    List<DetallesVenta> Listadetalles;
    List<DetallesVenta> detallesventasfiltrado;
    List<Ventas> ventasfiltrado;
    List<Productos> productofiltrado;
    String ticketglobal="";
    String opcion3="";
    @Override
public void initialize(URL url, ResourceBundle rb) {
    cargarCategorias(this.motivos, 1);
    ticket1.setCellValueFactory(new PropertyValueFactory<>("ticket1"));
    fecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
    total.setCellValueFactory(new PropertyValueFactory<>("total"));
    cantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
    nombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
    detalles.setCellValueFactory(new PropertyValueFactory<>("detalle"));
    iniciarcomponentes();
    SpinnerValueFactory<Double> valueFactory1 = new SpinnerValueFactory.DoubleSpinnerValueFactory(
            Double.MIN_VALUE, Double.MAX_VALUE, 0.0, 0.1);
    spinner.setValueFactory(valueFactory1);

    tabladev.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
        if (newValue != null) {

            actualizarSpinnerMaximo(newValue.getCantidad());
           
        }
    });
    if (rootPane != null) {
        rootPane.setOnKeyPressed(this::handleKeyPressed);
    } else {
        // Manejar el caso en el que rootPane sea nulo
    }

}


private void cargarCategorias(ComboBox<String> motivos, int opcion){
    motivos.getItems().clear();
    motivos.getItems().addAll("producto en mal estado", "Error de pedido", "otro");
    motivos.setOnAction(event -> {
        String selectedOption = motivos.getSelectionModel().getSelectedItem();
        if (selectedOption ==null) {
            System.out.println("nada");
        }
        else{
            if (motivos.getSelectionModel().getSelectedItem().equals("otro")) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Motivo");
                dialog.setHeaderText("Ingrese el Motivo");
                dialog.setContentText("Motivo:");

                dialog.showAndWait().ifPresent(valor -> {
                    opcion3=valor;
                });
            }
            else{
                opcion3="";
                System.out.println("ummm");
            }
        }
        });
}
private void actualizarSpinnerMaximo(BigDecimal cantidadVenta) {
    SpinnerValueFactory<Double> valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(
        Double.MIN_VALUE, cantidadVenta.doubleValue(), 0.0, 0.1);
    spinner.setValueFactory(valueFactory);
}
    public void iniciarcomponentes() { 
        listaVentas = obtenerListaDeVentas();
        ListaProducto = obtenerListaDeProductos();
        Listadetalles = obtenerListaDeDetalleVentas();
        compararTicketConTextField(listaVentas, ListaProducto, Listadetalles);
        mostartabla(listaVentas, Listadetalles, ListaProducto);
    }

    public List<Ventas> obtenerListaDeVentas() {
        Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = emf.createEntityManager();
        TypedQuery<Ventas> query = entityManager.createQuery("SELECT v FROM Ventas v", Ventas.class);
        List<Ventas> ventas = query.getResultList();
        entityManager.close();
        emf.close();
    
        return ventas;
    }

    @SuppressWarnings("exports")
    public List<Productos> obtenerListaDeProductos() {
        Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = emf.createEntityManager();
        TypedQuery<Productos> query = entityManager.createQuery("SELECT p FROM Productos p", Productos.class);
        List<Productos> productos = query.getResultList();

        entityManager.close();
        emf.close();

        return productos;
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

    public void compararTicketConTextField(List<Ventas> listaVentas, List<Productos> listaProductos,
            List<DetallesVenta> listaDetalles) {
        ticket.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                // Mostrar todos los datos cuando el campo de texto está vacío
                mostartabla(listaVentas, listaDetalles, listaProductos);
            } else {
                // Filtrar los datos según el valor del campo de texto
                List<Ventas> ventasFiltradas = new ArrayList<>();
                List<DetallesVenta> detallesVentaFiltrados = new ArrayList<>();
                List<Productos> productosFiltrados = new ArrayList<>();

                for (Ventas venta : listaVentas) {
                    String ticketVenta = venta.getTicket();
                    if (ticketVenta.startsWith(newValue)) {
                        ventasFiltradas.add(venta);

                        for (DetallesVenta detalle : listaDetalles) {
                            if (venta.equals(detalle.getVenta())) {
                                detallesVentaFiltrados.add(detalle);

                                Productos producto = detalle.getProducto();
                                if (!productosFiltrados.contains(producto)) {
                                    productosFiltrados.add(producto);
                                }
                            }
                        }
                    }
                }

                mostartabla(ventasFiltradas, detallesVentaFiltrados, productosFiltrados);
            }
        });
    }

    private void mostartabla(List<Ventas> ventasfiltrado, List<DetallesVenta> detallesventasfiltrado, List<Productos> productofiltrado) {
        datosTabla.clear(); // Limpiar los datos existentes en la tabla

        for (Ventas venta : ventasfiltrado) {
            String ticket1 = venta.getTicket();
            LocalDateTime fecha = venta.getFecha();
            BigDecimal total = venta.getTotal();

            String nombre = "";
            Long id=null;
            BigDecimal cantidadventa = null;
            for (DetallesVenta detalle : detallesventasfiltrado) {
                id = detalle.getId();
                
                if (detalle.getVenta().getId().equals(venta.getId())) {
                    Productos producto = detalle.getProducto();
                    nombre = producto.getNombre();
                    cantidadventa = detalle.getCantidad();
                    break;
                }
            }
            DateTimeFormatter formatoSinT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String fechaFormateada = fecha.format(formatoSinT);
            datosTabla.add(new tabledata(ticket1, fechaFormateada, total, cantidadventa, nombre,id));
            tabladev.setItems(datosTabla);
        }

        
    }

    public void calculo(LocalDateTime fechaHoraActual,BigDecimal cantidadventa, Long idVenta, Long idDetalle, String texto,Double Devolucion) {
        BigDecimal resultadoResta = cantidadventa.subtract(BigDecimal.valueOf(Devolucion));
        actualizarCantidadDetalleVenta(idVenta, idDetalle, resultadoResta,cantidadventa);
        llenartabladevolucion(resultadoResta, fechaHoraActual, texto, idVenta);
    }

    private void llenartabladevolucion(BigDecimal resultadoResta,LocalDateTime fechaSQL, String texto, Long idVenta) {
        double doubleValue = resultadoResta.doubleValue();
        Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        LocalDate localDate = LocalDate.now();
        LocalDateTime localDateTime = LocalDateTime.of(localDate, LocalDateTime.now().toLocalTime());
        Timestamp timestamp = Timestamp.valueOf(fechaSQL);
        System.out.println(fechaSQL+"vamosss ");
        System.out.println(timestamp+"vamosss ");
        try {
            // Buscar la entidad Ventas por su ID
            Ventas venta = entityManager.find(Ventas.class, idVenta);
    
            // Crear una nueva instancia de la entidad Devoluciones
            Devoluciones devolucion = new Devoluciones();
            devolucion.setCantidadDevuelta(doubleValue);
            devolucion.setFechaDevolucion(timestamp);
            devolucion.setMotivo(texto);
            devolucion.setVenta(venta);
    
            // Guardar la entidad Devoluciones en la base de datos
            entityManager.persist(devolucion);
            entityManager.getTransaction().commit();
    
            System.out.println("Devolución agregada correctamente");
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            entityManager.close();
            emf.close();
        }
    }
    private void actualizarCantidadDetalleVenta(Long idVenta, Long idDetalle, BigDecimal nuevaCantidad, BigDecimal cantidadventa) {
        Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            // Buscar el detalle de venta por ID_Venta e ID_Detalle
            TypedQuery<DetallesVenta> query = entityManager.createQuery(
                "SELECT dv FROM DetallesVenta dv WHERE dv.venta.id = :idVenta AND dv.id = :idDetalle",
                DetallesVenta.class);
            query.setParameter("idVenta", idVenta);
            query.setParameter("idDetalle", idDetalle);
            DetallesVenta detalleVenta = query.getSingleResult();
            Productos producto = detalleVenta.getProducto();
            Ventas venta= detalleVenta.getVenta();

            BigDecimal precioporProducto=producto.getPrecio();
            BigDecimal resultadoDetalle = precioporProducto.multiply(nuevaCantidad);
            
            venta.setTotal(resultadoDetalle);
            detalleVenta.setTotal(resultadoDetalle);
            detalleVenta.setCantidad(nuevaCantidad);
            entityManager.merge(detalleVenta);
    
            // Guardar los cambios en la base de datos
            entityManager.getTransaction().commit();
            System.out.println("Actualización realizada correctamente");
        } catch (NoResultException e) {
            System.out.println("No se encontró el detalle de venta correspondiente");
            entityManager.getTransaction().rollback();
        } finally {
            entityManager.close();
            emf.close();
        }
    }
    @FXML
    private void enviar(ActionEvent event) {
        proceso();
    }

    @FXML
    private void regresarventa(ActionEvent event) {
        regresar();
    }


    private void proceso(){
tabledata ticketseleccionado = tabladev.getSelectionModel().getSelectedItem();
        LocalDateTime fechaHoraActual = LocalDateTime.now();
        String selectedItem = motivos.getSelectionModel().getSelectedItem();
        Double Devolucion = spinner.getValue();
    
        if (ticketseleccionado == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Campo o valor no seleccionado");
            alert.showAndWait();
            return;
        } else {
            ticketglobal = ticketseleccionado.getTicket1();
            Long idDetalle = ticketseleccionado.getDetalle();
            BigDecimal cantidadVenta = ticketseleccionado.getCantidad();
            String fechaVenta = ticketseleccionado.getFecha();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime fechaLocalDateTime = LocalDateTime.parse(fechaVenta, formatter);

            LocalDateTime fechaVentaMas24Horas = fechaLocalDateTime.plusHours(24);

            if (fechaHoraActual.isAfter(fechaVentaMas24Horas)) {
                // Se pasaron más de 24 horas desde la venta
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Advertencia");
                alert.setHeaderText(null);
                alert.setContentText("Se pasaron más de 24 horas desde la venta. No se puede realizar la devolución.");
                alert.showAndWait();
                motivos.getSelectionModel().clearSelection();

                spinner.getValueFactory().setValue(0.0);
            } else {
                // No se pasaron más de 24 horas desde la venta
                if (selectedItem != null && spinner.getValue() > 0) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Exitosa");
                    alert.setHeaderText(null);
                    alert.setContentText("¿Seguro? El siguiente ticket " + ticketglobal + " se le aplicará una devolución con la ID " + idDetalle);
    
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        Long idVenta = buscarIdVentaPorDetalle(idDetalle);
                        if (idVenta != null) {
                            String selectedOption = motivos.getSelectionModel().getSelectedItem();
                            System.out.println(selectedOption+"--------ya");
                            if(motivos.getSelectionModel().getSelectedItem().equals("producto en mal estado") && selectedOption!= null){
                                System.out.println("no se agrega a productos");
                            }
                            else{
                                if(motivos.getSelectionModel().getSelectedItem().equals("otro")){
                                    selectedItem=opcion3;
                                }
                                actualizarproductos(Devolucion, idVenta, idDetalle);
                            }
                            calculo(fechaHoraActual,cantidadVenta, idVenta, idDetalle,selectedItem,Devolucion);
                            listaVentas = null;
                            ListaProducto = null;
                            Listadetalles = null;
                            iniciarcomponentes();
    
                            Alert secondAlert = new Alert(Alert.AlertType.INFORMATION);
                            secondAlert.setTitle("Devolución exitosa");
                            secondAlert.setHeaderText(null);
                            secondAlert.setContentText("Ticket " + ticketglobal + " con la ID " + idDetalle);
                            secondAlert.showAndWait();

                            motivos.getSelectionModel().clearSelection();

                            spinner.getValueFactory().setValue(0.0);
                        } else {
                            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                            errorAlert.setTitle("Error");
                            errorAlert.setHeaderText(null);
                            errorAlert.setContentText("No se encontró la venta correspondiente al detalle seleccionado");
                            errorAlert.showAndWait();
                        }
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Llena todos los campos correctamente");
                    alert.showAndWait();
                }
            }
        }
    }
    @FXML
    private void vertabla(ActionEvent event) {
        abrir();
    }
    private void abrir(){
        try {
            // Cargar el archivo FXML con el nuevo contenido
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FXMLTablaDevolucion.fxml"));
            Pane nuevoContenido = loader.load();
            
            // Obtener el controlador del nuevo contenido
            FXMLVerTabla inventarioController = loader.getController();
            
            // Reemplazar el contenido del contenedor principal con el nuevo contenido
            rootPane.getChildren().setAll(nuevoContenido);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Long buscarIdVentaPorDetalle(Long idDetalle) {
        Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = emf.createEntityManager();
    
        try {
            TypedQuery<DetallesVenta> query = entityManager.createQuery(
                "SELECT dv FROM DetallesVenta dv WHERE dv.id = :idDetalle",
                DetallesVenta.class);
            query.setParameter("idDetalle", idDetalle);
            DetallesVenta detalleVenta = query.getSingleResult();
            return detalleVenta.getVenta().getId();
        } catch (NoResultException e) {
            System.out.println("No se encontró el detalle de venta correspondiente");
            return null;
        } finally {
            entityManager.close();
            emf.close();
        }
        
    }
    public void actualizarproductos(Double Devolucion, Long idVenta, Long idDetalle){

        Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
try {
    // Consulta para obtener el detalle de venta por ID_Venta e ID_Detalle
    TypedQuery<DetallesVenta> query = entityManager.createQuery(
        "SELECT dv FROM DetallesVenta dv JOIN FETCH dv.producto p JOIN dv.venta v WHERE v.id = :idVenta AND dv.id = :idDetalle",
        DetallesVenta.class);
    query.setParameter("idVenta", idVenta);
    query.setParameter("idDetalle", idDetalle);
    DetallesVenta detalleVenta = query.getSingleResult();


    Productos producto = detalleVenta.getProducto();
    
    BigDecimal cantidad =producto.getCantidad();
    BigDecimal doubleComoBigDecimal = BigDecimal.valueOf(Devolucion);
    BigDecimal resultado = cantidad.add(doubleComoBigDecimal);
    
    producto.setCantidad(resultado);
    
    entityManager.merge(producto);
    entityManager.getTransaction().commit();
    System.out.println("Actualización realizada correctamente");
} catch (NoResultException e) {
    System.out.println("No se encontró el detalle de venta correspondiente");
    entityManager.getTransaction().rollback();
} finally {
    entityManager.close();
    emf.close();
}

    }

    @FXML
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.F5) {
           proceso();
        }

        if (event.getCode() == KeyCode.F6) {
            abrir();
        }
    }
    public void regresar(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Ventas.fxml"));
            Parent root = loader.load();
            VentasController newProductoController = loader.getController();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("dff");
        }
    }
}