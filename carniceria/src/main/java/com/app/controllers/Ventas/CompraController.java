package com.app.controllers.Ventas;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.app.models.DetallesVenta;
import com.app.models.Productos;
import com.app.models.Ventas;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;

import javafx.beans.property.SimpleObjectProperty;
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
import javafx.stage.Stage;
public class CompraController {

    private static final Font TITLE_FONT = new Font(Font.FontFamily.COURIER, 3);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.COURIER, 3);
    private VentasController ventasController1;
    @FXML
    private Label totalImporteLabel;
    @FXML
    private Label ticketLabel;
    @FXML
    private TableView<Productos> tablaDetallesVenta;
    @FXML
    private TableColumn<Productos, String> nombreProductoColumn;
    @FXML
    private TableColumn<Productos, BigDecimal> precioColumn;
    @FXML
    private TableColumn<Productos, BigDecimal> cantidadColumn;
    @FXML
    private TableColumn<Productos, BigDecimal> totalColumn;

    @FXML
    private TextField insertarPagoTextField;

    @FXML
    private Button borrararticulo;

    @FXML
    private Label cambioLabel;

    private Ventas venta;
    private BigDecimal importeTotal;

    private ObservableList<Productos> productosData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        nombreProductoColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        precioColumn.setCellValueFactory(new PropertyValueFactory<>("precio"));
        cantidadColumn.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        totalColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(
                cellData.getValue().getPrecio().multiply(cellData.getValue().getCantidad())));

        insertarPagoTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            calcularCambio();
        });

        
    }

    public void initData(ObservableList<Productos> productosData, BigDecimal importeTotal) {
        setProductosData(productosData);
        setImporteTotal(importeTotal);
        actualizarTablaDetallesVenta();
        totalImporteLabel.setText(importeTotal.toString());
    }

    private void actualizarTablaDetallesVenta() {
        tablaDetallesVenta.setItems(productosData);
    }

    public void setProductosData(ObservableList<Productos> productosData) {
        this.productosData = productosData;
        tablaDetallesVenta.setItems(productosData);
    }

    public void setImporteTotal(BigDecimal importeTotal) {
        this.importeTotal = importeTotal;
    }

    @FXML
    private void finalizarCompra() {
        try {
            BigDecimal cambio = new BigDecimal(cambioLabel.getText());
            if (cambio.compareTo(BigDecimal.ZERO) < 0) {
                mostrarAlertaError("Cambio insuficiente",
                        "El monto ingresado es insuficiente para realizar la compra.");
                return;

            }
            guardarVenta();
            generarPDF();
            actualizarInventario();
            importeTotal = BigDecimal.ZERO;
            regresarAVenta();
      // Restablecer el valor del cambio a 0
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlertaError("Error al finalizar la compra",
                    "Ocurrió un error al finalizar la compra. Por favor, intente nuevamente.");
        }
    }

    private void guardarVenta() {
        Configuration configuration = new Configuration().configure();
        configuration.addAnnotatedClass(Productos.class);
        configuration.addAnnotatedClass(Ventas.class);
        configuration.addAnnotatedClass(DetallesVenta.class);

        SessionFactory sessionFactory = null;
        EntityManagerFactory entityManagerFactory = null;
        EntityManager entityManager = null;

        try {
            sessionFactory = configuration.buildSessionFactory();
            entityManagerFactory = sessionFactory.unwrap(EntityManagerFactory.class);
            entityManager = entityManagerFactory.createEntityManager();

            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();

            if (productosData == null || productosData.isEmpty()) {
                mostrarAlertaError("Error al guardar la venta", "No se encontraron productos en la venta.");
                return;
            }

            Ventas venta = new Ventas();
            venta.setTicket(String.format("%06d", (int) (Math.random() * 1000000)));
            venta.setFecha(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
            venta.setTotal(importeTotal != null ? importeTotal.floatValue() : 0.0f);

            for (Productos producto : productosData) {
                if (producto == null) {
                    mostrarAlertaError("Error al guardar la venta", "Se encontró un producto nulo en la venta.");
                    return;
                }

                DetallesVenta detalle = new DetallesVenta();
                detalle.setProducto(producto);

                BigDecimal cantidad = producto.getCantidad();
                if (cantidad == null) {
                    mostrarAlertaError("Error al guardar la venta", "Se encontró una cantidad nula en un producto.");
                    return;
                }
                detalle.setCantidad(cantidad);

                BigDecimal precio = producto.getPrecio();
                if (precio == null) {
                    mostrarAlertaError("Error al guardar la venta", "Se encontró un precio nulo en un producto.");
                    return;
                }

                detalle.setTotal(precio.multiply(cantidad));
                venta.addDetalle(detalle);
            }

            entityManager.persist(venta);
            transaction.commit();

            mostrarAlertaInformacion("Venta generada",
                    "La venta se ha generado correctamente. Ticket: " + venta.getTicket());

            this.venta = venta;
            totalImporteLabel.setText("Ticket: " + venta.getTicket());
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlertaError("Error al guardar la venta",
                    "Ocurrió un error al guardar la venta. Por favor, intente nuevamente.");
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

    private void generarPDF() {
        try {
            if (venta == null) {
                mostrarAlertaError("Error al generar el PDF",
                        "No se encontró información de la venta. Por favor, intente nuevamente.");
                return;
            }

            Document document = new Document(new Rectangle(100, 170));
            String numeroTicket = venta.getTicket();
            String nombreArchivo = "comprobante_" + numeroTicket + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(nombreArchivo));
            document.open();

            Paragraph title = new Paragraph("Comprobante de Venta", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Ticket: " + venta.getTicket(), NORMAL_FONT));
            document.add(new Paragraph("Fecha: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(venta.getFecha()), NORMAL_FONT));
            document.add(new Paragraph("Total: " + venta.getTotal(), NORMAL_FONT));

            document.add(Chunk.NEWLINE);

            List<DetallesVenta> detalles = venta.getDetalles();
            for (DetallesVenta detalle : detalles) {
                String itemInfo = detalle.getProducto().getNombre() + " - Precio: " + detalle.getProducto().getPrecio()
                        +
                        ", Cantidad: " + detalle.getCantidad() + ", Total: " + detalle.getTotal();

                document.add(new Paragraph(itemInfo, NORMAL_FONT));

                document.add(Chunk.NEWLINE);
            }

            document.close();

            System.out.println("PDF generado exitosamente con el nombre: " + nombreArchivo);
        } catch (Exception e) {
            mostrarAlertaError("Error al generar el PDF",
                    "Ocurrió un error al generar el PDF. Por favor, intente nuevamente.");
        }
    }

    @FXML
    private void regresarAVenta() {
        // Cerrar la comunicación con la báscula antes de regresar a la vista de Ventas
        VentasController ventasController1 = new VentasController();
        ventasController1.cerrarBascula();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Ventas.fxml"));
            Scene scene = new Scene(loader.load());
            VentasController ventasController = loader.getController();
            ventasController.actualizarDatos(productosData, importeTotal);
            ventasController.cargarProductos(); // Cargar los productos actualizados desde la base de datos

            Stage stage = (Stage) tablaDetallesVenta.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlertaError("Error al regresar", "Ocurrió un error al regresar. Por favor, intente nuevamente.");
        }
    }

    @FXML
    public void regresar() {
        // Cerrar la comunicación con la báscula antes de regresar a la vista de Ventas
        VentasController ventasController1 = new VentasController();
        ventasController1.cerrarBascula(); // Asumiendo que tienes un método para cerrar la báscula en VentasController
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Ventas.fxml"));
            Scene scene = new Scene(loader.load());
            VentasController ventasController = loader.getController();
            ventasController.actualizarDatos(productosData, importeTotal);
            ventasController.actualizarProductosAgregados(productosData); // Actualizar los productos agregados
            ventasController.actualizarTotalImporte(); // Actualizar el importe total

            Stage stage = (Stage) tablaDetallesVenta.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlertaError("Error al regresar", "Ocurrió un error al regresar. Por favor, intente nuevamente.");
        }
    }

    

    

    private void mostrarAlertaInformacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAlertaError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    private void calcularCambio() {
        try {
            BigDecimal montoIngresado = new BigDecimal(insertarPagoTextField.getText());
            BigDecimal cambio = montoIngresado.subtract(importeTotal);
            cambioLabel.setText(cambio.toString());
        } catch (NumberFormatException e) {
            cambioLabel.setText("0.00");
        }
    }

    private void actualizarInventario() {
        try {
            Configuration configuration = new Configuration().configure();
            configuration.addAnnotatedClass(Productos.class);

            SessionFactory sessionFactory = null;
            EntityManagerFactory entityManagerFactory = null;
            EntityManager entityManager = null;

            try {
                sessionFactory = configuration.buildSessionFactory();
                entityManagerFactory = sessionFactory.unwrap(EntityManagerFactory.class);
                entityManager = entityManagerFactory.createEntityManager();

                EntityTransaction transaction = entityManager.getTransaction();
                transaction.begin();

                for (Productos producto : productosData) {
                    Productos productoBD = entityManager.find(Productos.class, producto.getId());
                    BigDecimal cantidadVendida = producto.getCantidad();
                    BigDecimal cantidadActualizada = productoBD.getCantidad().subtract(cantidadVendida);
                    productoBD.setCantidad(cantidadActualizada);
                    entityManager.merge(productoBD);
                }

                transaction.commit();

            } catch (Exception e) {
                e.printStackTrace();
                mostrarAlertaError("Error al actualizar inventario",
                        "Ocurrió un error al actualizar el inventario. Por favor, intente nuevamente.");
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
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlertaError("Error al actualizar inventario",
                    "Ocurrió un error al actualizar el inventario. Por favor, intente nuevamente.");
        }
    }

    @FXML
    public void borrarArticuloSeleccionado() {
        Productos productoSeleccionado = tablaDetallesVenta.getSelectionModel().getSelectedItem();
        if (productoSeleccionado != null) {
            productosData.remove(productoSeleccionado);
            actualizarTotalImporte();
            totalImporteLabel.setText(importeTotal.toString());
        } else {
            mostrarAlertaError("Error al borrar artículo", "Debe seleccionar un artículo de la tabla para eliminar.");
        }
    }

    private void actualizarTotalImporte() {
        importeTotal = productosData.stream()
                .map(producto -> producto.getPrecio().multiply(producto.getCantidad()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
