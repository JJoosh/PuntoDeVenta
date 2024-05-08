package com.app.controllers.devoluciones;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import com.app.models.Ventas;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import com.app.controllers.Inventario.FXMLInventarioController;
import com.app.controllers.Inventario.FXML_NewProducto;
import com.app.controllers.devoluciones.tabledata;

import com.app.models.Categoria;
import com.app.models.DetallesVenta;
import com.app.models.Devoluciones;
import com.app.models.Productos;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
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
    private Spinner<Double> spinner;

    @FXML
    private DatePicker date;

    @FXML
    private TextArea textoArea;

    private ObservableList<tabledata> datosTabla = FXCollections.observableArrayList();

    List<Ventas> listaVentas;
    List<Productos> ListaProducto;
    List<DetallesVenta> Listadetalles;
    List<DetallesVenta> detallesventasfiltrado;
    List<Ventas> ventasfiltrado;
    String ticketglobal="";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ticket1.setCellValueFactory(new PropertyValueFactory<>("ticket1"));
        fecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        total.setCellValueFactory(new PropertyValueFactory<>("total"));
        cantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        nombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        listaVentas = obtenerListaDeVentas();
        ListaProducto = obtenerListaDeProductos();
        Listadetalles = obtenerListaDeDetalleVentas();
        compararTicketConTextField(listaVentas, ListaProducto, Listadetalles);
        mostartabla(listaVentas, Listadetalles, ListaProducto);

        SpinnerValueFactory<Double> valueFactory1 = new SpinnerValueFactory.DoubleSpinnerValueFactory(
                Double.MIN_VALUE, Double.MAX_VALUE, 0.0, 0.1);
        spinner.setValueFactory(valueFactory1);
    }

    @SuppressWarnings("exports")
    public List<Ventas> obtenerListaDeVentas() {
        Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = emf.createEntityManager();
        TypedQuery<Ventas> query = entityManager.createQuery("SELECT v FROM Ventas v", Ventas.class);
        List<Ventas> ventas = query.getResultList();

        for (Ventas venta : ventas) {
            Date fechaTimestamp = venta.getFecha();
            Date fechaDate = new Date(fechaTimestamp.getTime());
            venta.setFecha(fechaDate);
        }
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

    public void compararTicketConTextField(List<Ventas> listaVentas, List<Productos> listaProductos, List<DetallesVenta> listaDetalles) {
        ticket.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                // Mostrar todos los datos cuando el campo de texto está vacío
                mostartabla(listaVentas, listaDetalles, listaProductos);
            } else {
                // Filtrar los datos según el valor del campo de texto
                List<Productos> productofiltrado = new ArrayList<>();
                detallesventasfiltrado = new ArrayList<>();
                ventasfiltrado = new ArrayList<>();

                for (Ventas venta : listaVentas) {
                    String ticketVenta = venta.getTicket();
                    if (ticketVenta.startsWith(newValue)) {
                        Long idVenta = venta.getId();
                        Date fechaVenta = venta.getFecha();
                        String ticket = venta.getTicket();
                        float totalVenta = venta.getTotal();

                        Ventas ventasn = new Ventas(idVenta, ticket, fechaVenta, totalVenta);
                        ventasfiltrado.add(ventasn);

                        for (DetallesVenta detalles : listaDetalles) {
                            Ventas idventasDetalle = detalles.getVenta();
                            if (idVenta.equals(idventasDetalle.getId())) {
                                Productos idproductoDetalle = detalles.getProducto();
                                BigDecimal cantidad = detalles.getCantidad();
                                BigDecimal totalDetallesVenta = detalles.getTotal();

                                DetallesVenta detallesn = new DetallesVenta(idventasDetalle, idproductoDetalle, cantidad, totalDetallesVenta);
                                detallesventasfiltrado.add(detallesn);

                                for (Productos producto : listaProductos) {
                                    Long idproducto = producto.getId();
                                    if (idproductoDetalle.getId().equals(idproducto)) {
                                        BigDecimal cantidadProducto = producto.getCantidad();
                                        BigDecimal precioProducto = producto.getPrecio();
                                        String NombreProducto = producto.getNombre();
                                        Categoria categoria = producto.getCategoria();
                                        Productos productoN = new Productos(NombreProducto, categoria, cantidadProducto, precioProducto);
                                        productofiltrado.add(productoN);
                                    }
                                }
                            }
                        }
                    }
                }

                mostartabla(ventasfiltrado, detallesventasfiltrado, productofiltrado);
            }
        });
    }

    private void mostartabla(List<Ventas> ventasfiltrado, List<DetallesVenta> detallesventasfiltrado, List<Productos> productofiltrado) {
        datosTabla.clear(); // Limpiar los datos existentes en la tabla

        for (Ventas venta : ventasfiltrado) {
            String ticket1 = venta.getTicket();
            Date fecha = venta.getFecha();
            float total = venta.getTotal();
            ticketglobal=ticket1;

            String nombre = "";
            BigDecimal cantidadventa = null;
            for (DetallesVenta detalle : detallesventasfiltrado) {
                if (detalle.getVenta().getId().equals(venta.getId())) {
                    Productos producto = detalle.getProducto();
                    nombre = producto.getNombre();
                    cantidadventa = detalle.getCantidad();
                    break;
                }
            }

            datosTabla.add(new tabledata(ticket1, fecha, total, cantidadventa, nombre));
        }

        tabladev.setItems(datosTabla);
    }

    public void calculo(BigDecimal cantidadventa,Long idVenta) {
        LocalDate fechaSeleccionada = date.getValue();
        Date fechaSQL = Date.valueOf(fechaSeleccionada);
        String texto = textoArea.getText();
        Double Devolucion = spinner.getValue();
        BigDecimal resultadoResta = cantidadventa.subtract(BigDecimal.valueOf(Devolucion));
        actualizarCantidadDetalleVenta(idVenta,resultadoResta);
        llenartabladevolucion(resultadoResta,fechaSQL,texto,idVenta);
    }

    private void llenartabladevolucion(BigDecimal resultadoResta, Date fechaSQL, String texto, Long idVenta) {
        double doubleValue = resultadoResta.doubleValue();
        Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        Timestamp timestamp = new Timestamp(fechaSQL.getTime());
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
    private void actualizarCantidadDetalleVenta(Long idVenta, BigDecimal nuevaCantidad) {
        Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        
        try {
            // Buscar el detalle de venta por ID_Venta
            TypedQuery<DetallesVenta> query = entityManager.createQuery(
                "SELECT dv FROM DetallesVenta dv WHERE dv.venta.id = :idVenta",
                DetallesVenta.class);
            query.setParameter("idVenta", idVenta);
            DetallesVenta detalleVenta = query.getSingleResult();
            
            // Actualizar la cantidad del detalle de venta
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
        tabledata ticketseleccionado = tabladev.getSelectionModel().getSelectedItem();

        if (ticketseleccionado  != null && ticket.getText().isEmpty()==true) {
            String ticket = ticketseleccionado.getTicket1();
            ticketglobal= ticket;

        } 
        if (ticketseleccionado  == null && ticket.getText().isEmpty()==true) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("ningun campo o ticket seleccionado");
        }
        if (ticketseleccionado  == null && ticket.getText().isEmpty()==true) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("campo o valor no seleccionado");
        }
        else{

        } 
        if(date.getValue() != null && textoArea.getText().isEmpty() == false &&spinner.getValue()>0 && ticket.getText().isEmpty()==false) {
    
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Exitosa");
            alert.setHeaderText(null);
            alert.setContentText("seguro? el siguiente ticket "+ticketglobal+" se le aplicara una devolucion");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Alert secondAlert = new Alert(Alert.AlertType.INFORMATION);
                secondAlert.setTitle("Devolucion exitosa");
                secondAlert.setHeaderText(null);
                secondAlert.setContentText("ticket "+ticketglobal);
                secondAlert.showAndWait();

                for(Ventas venta : ventasfiltrado){
                    Long idVenta = venta.getId();
                    for (DetallesVenta detalle : detallesventasfiltrado){
                        BigDecimal cantidadventa= detalle.getCantidad();
                        calculo(cantidadventa, idVenta);
                    }
                }
            }
        }
        else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Llena todos los campos correctamente");
            alert.showAndWait();
        }

    }

    @FXML
    private void vertabla(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FXMLTablaDevolucion.fxml"));
            Parent root = loader.load();
            FXMLVerTabla newProductoController = loader.getController();
            newProductoController.setDevolucionesController(this);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}