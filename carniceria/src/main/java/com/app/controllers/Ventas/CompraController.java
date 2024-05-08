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
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;


public class CompraController {

    private static final Font TITLE_FONT = new Font(Font.FontFamily.COURIER, 3);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.COURIER, 3);

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
        VentasController usuario = new VentasController();

        System.out.println(usuario.getNombreUsser());
        insertarPagoTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            calcularCambio();
        });
    }

    public void initData(ObservableList<Productos> productosData, BigDecimal importeTotal) {
        setProductosData(productosData);
        setImporteTotal(importeTotal);

        totalImporteLabel.setText(importeTotal.toString());

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
            regresarAVenta();
            actualizarInventario();
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

            // Verificar si hay productos en la venta
            if (productosData == null || productosData.isEmpty()) {
                mostrarAlertaError("Error al guardar la venta", "No se encontraron productos en la venta.");
                return;
            }

            // Crear una nueva venta con el ticket generado
            Ventas venta = new Ventas();
            venta.setTicket(String.format("%06d", (int) (Math.random() * 1000000)));
            venta.setFecha(java.sql.Date.valueOf(LocalDate.now()));
            venta.setTotal(importeTotal != null ? importeTotal.floatValue() : 0.0f);

            // Agregar los detalles de la venta
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

            // Mostrar una alerta utilizando JavaFX Alert
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
            // Verificar si la venta ha sido inicializada
            if (venta == null) {
                mostrarAlertaError("Error al generar el PDF",
                        "No se encontró información de la venta. Por favor, intente nuevamente.");
                return;
            }

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
                String itemInfo = detalle.getProducto().getNombre() + " - Precio: " + detalle.getProducto().getPrecio()
                        +
                        ", Cantidad: " + detalle.getCantidad() + ", Total: " + detalle.getTotal();

                document.add(new Paragraph(itemInfo, NORMAL_FONT));

                document.add(Chunk.NEWLINE); // Espacio entre cada detalle de venta
            }

            // Cerrar el documento PDF
            document.close();

            System.out.println("PDF generado exitosamente con el nombre: " + nombreArchivo);
        } catch (Exception e) {
            mostrarAlertaError("Error al generar el PDF",
                    "Ocurrió un error al generar el PDF. Por favor, intente nuevamente.");
        }
    }

    @FXML
    private void regresarAVenta() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Ventas.fxml"));
            Scene scene = new Scene(loader.load());
            VentasController ventasController = loader.getController();
            ventasController.actualizarDatos(productosData, importeTotal);

            Stage stage = (Stage) tablaDetallesVenta.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlertaError("Error al regresar a la venta",
                    "Ocurrió un error al regresar a la venta. Por favor, intente nuevamente.");
        }
    }
    @FXML
    public void regresar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Ventas.fxml"));
            Scene scene = new Scene(loader.load());

            VentasController ventasController = loader.getController();
            ventasController.actualizarDatos(productosData, importeTotal);

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
                    // Obtener el producto desde la base de datos para asegurar la última versión
                    Productos productoBD = entityManager.find(Productos.class, producto.getId());
                    BigDecimal cantidadVendida = producto.getCantidad();

                    // Restar la cantidad vendida del inventario actual
                    BigDecimal cantidadActualizada = productoBD.getCantidad().subtract(cantidadVendida);

                    // Actualizar la cantidad en el objeto persistente
                    productoBD.setCantidad(cantidadActualizada);

                    // Actualizar el objeto persistente en la base de datos
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

}