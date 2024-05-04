package com.app.controllers.Ventas;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class CompraController {

    private static final Font TITLE_FONT = new Font(Font.FontFamily.COURIER, 3);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.COURIER, 3);

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

    private Ventas venta;
    private BigDecimal importeTotal;

    private ObservableList<Productos> productosData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        nombreProductoColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        precioColumn.setCellValueFactory(new PropertyValueFactory<>("precio"));
        cantidadColumn.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        totalColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPrecio().multiply(new BigDecimal(20))));
    }


    public void initData(ObservableList<Productos> productosData, BigDecimal importeTotal) {
        setProductosData(productosData);
        setImporteTotal(importeTotal);
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
    guardarVenta();
    generarPDF();
    regresarAVenta();
}

private void guardarVenta() {
    Configuration configuration = new Configuration().configure();
    configuration.addAnnotatedClass(Productos.class);
    configuration.addAnnotatedClass(Ventas.class);
    configuration.addAnnotatedClass(DetallesVenta.class);

    SessionFactory sessionFactory = configuration.buildSessionFactory();
    EntityManagerFactory entityManagerFactory = sessionFactory.unwrap(EntityManagerFactory.class);
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();

    transaction.begin();

    // Crear una nueva venta con el ticket generado
    Ventas venta = new Ventas();
    venta.setTicket(String.format("%06d", (int) (Math.random() * 1000000)));
    venta.setFecha(java.sql.Date.valueOf(LocalDate.now()));
    venta.setTotal(importeTotal.floatValue());

    // Agregar los detalles de la venta
    for (Productos producto : productosData) {
        DetallesVenta detalle = new DetallesVenta();
        detalle.setVenta(venta);
        detalle.setProducto(producto);
        BigDecimal cantidad = new BigDecimal(20);
        detalle.setCantidad(cantidad);
        detalle.setTotal(producto.getPrecio().multiply(cantidad));
        venta.addDetalle(detalle);
    }

    entityManager.persist(venta);
    transaction.commit();

    // Mostrar una alerta utilizando JavaFX Alert
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Venta generada");
    alert.setHeaderText(null);
    alert.setContentText("La venta se ha generado correctamente. Ticket: " + venta.getTicket());
    alert.show();

    this.venta = venta;
    ticketLabel.setText("Ticket: " + venta.getTicket());

    entityManager.close();
    entityManagerFactory.close();
}
    private void generarPDF() {
        try {
            // Crear un nuevo documento PDF con tamaño personalizado (10 x 17 cm)
            Document document = new Document(new Rectangle(100, 170));

            // Obtener el número de ticket
            String numeroTicket = venta.getTicket();

            // Nombre del archivo PDF basado en el número de ticket
            String nombreArchivo = "comprobante_" + numeroTicket + ".pdf";

            // Crear el archivo PDF con el nombre específico
            PdfWriter.getInstance(document, new FileOutputStream(nombreArchivo));
            document.open();

            // Agregar contenido al documento PDF
            Paragraph title = new Paragraph("Comprobante de Venta", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(Chunk.NEWLINE); // Espacio entre el título y los detalles

            // Información de la venta
            document.add(new Paragraph("Ticket: " + venta.getTicket(), NORMAL_FONT));
            document.add(new Paragraph("Fecha: " + venta.getFecha(), NORMAL_FONT));
            document.add(new Paragraph("Total: " + venta.getTotal(), NORMAL_FONT));

            document.add(Chunk.NEWLINE); // Espacio entre la información general y los detalles de la venta

            // Agregar detalles de la venta (productos, precios, cantidades y totales)
            List<DetallesVenta> detalles = venta.getDetalles();
            for (DetallesVenta detalle : detalles) {
                // Concatenar la información del producto en una sola línea
                String itemInfo = detalle.getProducto().getNombre() + " - Precio: " + detalle.getProducto().getPrecio() +
                                ", Cantidad: " + detalle.getCantidad() + ", Total: " + detalle.getTotal();

                document.add(new Paragraph(itemInfo, NORMAL_FONT));

                document.add(Chunk.NEWLINE); // Espacio entre cada detalle de venta
            }

            // Cerrar el documento PDF
            document.close();

            System.out.println("PDF generado exitosamente con el nombre: " + nombreArchivo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void regresarAVenta() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Ventas.fxml"));
            Scene scene = new Scene(loader.load());

            VentasController ventasController = loader.getController();
            Stage stage = (Stage) ticketLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
    }
}

@FXML
public void regresar(){
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Ventas.fxml"));
        Scene scene = new Scene(loader.load());

        VentasController ventasController = loader.getController();
        ventasController.actualizarDatos(productosData, importeTotal);

        Stage stage = (Stage) tablaDetallesVenta.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    } catch ( IOException e ) {
        e.printStackTrace();
    }
}




    
} 