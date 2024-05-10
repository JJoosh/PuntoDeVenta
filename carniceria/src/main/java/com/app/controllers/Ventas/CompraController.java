package com.app.controllers.Ventas;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.app.models.DetallesVenta;
import com.app.models.Movimientos;
import com.app.models.Productos;
import com.app.models.Ventas;
import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


public class CompraController {

    private static final Font NORMAL_FONT = new Font(Font.FontFamily.COURIER, 3);
    private static final Font FUENTE = new Font(Font.FontFamily.COURIER, 9);
    private static final Font FUENTE_DOS = new Font(Font.FontFamily.HELVETICA, 11);
    private static final Font NEGRITA = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
    private static final Font NEGRITA_DOS = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    private DecimalFormat formatoDinero = new DecimalFormat("$#,##0.00");

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

    @FXML
    private Pane rootPane;

    private Ventas venta;
    // private Productos id;
    private BigDecimal importeTotal;

    private ObservableList<Productos> productosData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        nombreProductoColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        precioColumn.setCellValueFactory(new PropertyValueFactory<>("precio"));
        cantidadColumn.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        // Modificar la forma en que se muestra el importe total en la columna
        totalColumn.setCellValueFactory(cellData -> {
            BigDecimal total = cellData.getValue().getPrecio().multiply(cellData.getValue().getCantidad());
            return new SimpleObjectProperty<>(total);
        });

        // Formatear la columna total con el signo de $ y dos decimales
        totalColumn.setCellFactory(column -> new TableCell<Productos, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    DecimalFormat formato = new DecimalFormat("$#,##0.00");
                    setText(formato.format(item));
                }
            }
        });

        insertarPagoTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            calcularCambio();
        });

        rootPane.setOnKeyPressed(this::handleKeyPressed);

        Platform.runLater(() -> insertarPagoTextField.requestFocus());
    }
    

    @FXML
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            borrarArticuloSeleccionado();
        }

        if (event.getCode() == KeyCode.F2) {
            finalizarCompra();
        }

        if (event.getCode() == KeyCode.F1) {
            regresar();
        }
    }

    public void initData(ObservableList<Productos> productosData, BigDecimal importeTotal) {
        setProductosData(productosData);
        setImporteTotal(importeTotal);
        actualizarTablaDetallesVenta();

        // Formatear el importe total con dos decimales y el signo de dólar
        DecimalFormat formato = new DecimalFormat("$#,##0.00");
        String importeFormateado = formato.format(importeTotal);

        totalImporteLabel.setText(importeFormateado);
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
            String montoIngresadoTexto = insertarPagoTextField.getText();
            if (montoIngresadoTexto.isEmpty()) {
                mostrarAlertaError("Monto de pago no ingresado",
                        "Por favor, ingrese el monto de pago antes de finalizar la compra.");
                return;
            }

            BigDecimal montoIngresado = new BigDecimal(montoIngresadoTexto);
            BigDecimal cambio = montoIngresado.subtract(importeTotal);
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

            // Crear una instancia de BigDecimal para el total
            BigDecimal total = importeTotal != null ? importeTotal : BigDecimal.ZERO;
            

            // Crear una instancia de la clase Ventas
            Ventas venta = new Ventas();
            venta.setTicket(String.format("%06d", (int) (Math.random() * 1000000)));
            venta.setFecha(LocalDateTime.now());
            venta.setTotal(total);

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
               
                Movimientos movimiento = new Movimientos();
                movimiento.setIdProducto(producto);
                movimiento.setTipoMovimiento("Salida");
                movimiento.setCantidad(cantidad);
                LocalDateTime fechaHoraActual = LocalDateTime.now();
                movimiento.setFecha(fechaHoraActual);
                guardarMovimiento(movimiento);
            }

            entityManager.persist(venta);
            transaction.commit();

            mostrarAlertaInformacion("Venta generada",
                    "La venta se ha generado correctamente. Ticket: " + venta.getTicket());

            this.venta = venta;
            totalImporteLabel.setText("Ticket: " + venta.getTicket());
        } catch (HibernateException e) {
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

    private void regresarAVenta() {
        // Limpiar la lista de productos
        productosData.clear();
    
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Ventas.fxml"));
            Pane nuevoContenido = loader.load();
    
            // Obtener el controlador del nuevo contenido
            VentasController ventasController = loader.getController();
    
            // Actualizar los datos en el controlador de ventas
            ventasController.actualizarDatos(productosData, importeTotal);
            ventasController.actualizarProductosAgregados(productosData);
            ventasController.actualizarTotalImporte();
    
            rootPane.getChildren().setAll(nuevoContenido);
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlertaError("Error al regresar", "Ocurrió un error al regresar. Por favor, intente nuevamente.");
        }
    }
    @FXML
public void regresar() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Ventas.fxml"));
        Pane nuevoContenido = loader.load();

        // Obtener el controlador del nuevo contenido
        VentasController ventasController = loader.getController();

        // Actualizar los datos en el controlador de ventas
        ventasController.actualizarDatos(productosData, importeTotal);
        ventasController.actualizarProductosAgregados(productosData);
        ventasController.actualizarTotalImporte();

        rootPane.getChildren().setAll(nuevoContenido);
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
            cambioLabel.setText(formatoDinero.format(cambio));
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
            totalImporteLabel.setText(formatoDinero.format(importeTotal));
        } else {
            mostrarAlertaError("Error al borrar artículo", "Debe seleccionar un artículo de la tabla para eliminar.");
        }
    }

    private void actualizarTotalImporte() {
        importeTotal = productosData.stream()
                .map(producto -> producto.getPrecio().multiply(producto.getCantidad()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void generarPDF() {
        try {
            if (venta == null) {
                mostrarAlertaError("Error al generar el PDF",
                        "No se encontró información de la venta. Por favor, intente nuevamente.");
                return;
            }
    
            Document document = new Document(new Rectangle(200f, 500f));
            document.setMargins(3, 2, 10, 3);
            String numeroTicket = venta.getTicket();
            String nombreArchivo = "comprobante_" + numeroTicket + ".pdf";
    
            // Obtener la ruta de la carpeta de descargas
            String rutaDescargas = System.getProperty("user.home") + "/Downloads/";
            String rutaPDF = rutaDescargas + nombreArchivo;
    
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(rutaPDF));
            document.open();
    
            // Agregar logo de la empresa
            String rutaLogo = "carniceria/src/main/java/com/app/controllers/Ventas/ticket/LOGO.png";
            Image logo = Image.getInstance(rutaLogo);
            logo.scaleToFit(100, 100); // Ajusta el tamaño del logo según tus necesidades
            logo.setAlignment(Element.ALIGN_CENTER);
            document.add(logo);
            Paragraph saltolinea = new Paragraph('\n');
            document.add(saltolinea);
    
            // Agregar información de la venta
            Paragraph textoIzquierda = new Paragraph("Ficha de compra\n",
                    new Font(Font.FontFamily.COURIER, 9, Font.NORMAL));
            textoIzquierda.setAlignment(Element.ALIGN_CENTER);
            textoIzquierda.setLeading(1f, 1f);
            document.add(textoIzquierda);
    
            textoIzquierda = new Paragraph("No. ticket: " + numeroTicket,
                    new Font(Font.FontFamily.COURIER, 9, Font.NORMAL));
            textoIzquierda.setAlignment(Element.ALIGN_LEFT);
            textoIzquierda.setLeading(1f, 1f);
            document.add(textoIzquierda);
    
            textoIzquierda = new Paragraph(
                    "Fecha: " + venta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                    new Font(Font.FontFamily.COURIER, 9, Font.NORMAL));
            textoIzquierda.setAlignment(Element.ALIGN_LEFT);
            textoIzquierda.setLeading(1f, 1f);
            document.add(textoIzquierda);
    
            Paragraph lineaSeparadora = new Paragraph(
                    "________________________________________________________________________________",
                    new Font(Font.FontFamily.COURIER, 4, Font.BOLD));
    
            lineaSeparadora.setAlignment(Element.ALIGN_CENTER);
            lineaSeparadora.setLeading(1f, 1f);
            document.add(lineaSeparadora);
    
            PdfPTable tablaDetalles = new PdfPTable(3);
    
            // Obtener el ancho disponible para la tabla
            float anchoHoja = 200f;
            float anchoDisponible = anchoHoja - document.leftMargin() - document.rightMargin();
    
            // Establecer el ancho total de la tabla al ancho disponible
            tablaDetalles.setTotalWidth(anchoDisponible);
            tablaDetalles.setLockedWidth(true);
    
            // Ajustar los anchos proporcionales de las columnas
            tablaDetalles.setWidths(new float[] { 0.4f, 0.4f, 0.3f });
    
            tablaDetalles.setHorizontalAlignment(Element.ALIGN_LEFT);
            tablaDetalles.getDefaultCell().setBorder(0);
    
            PdfPCell cellDerecha = new PdfPCell();
            cellDerecha.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellDerecha.setBorder(0);
            cellDerecha.setLeading(0.5f, 0.8f);
    
            PdfPCell cellIzquierda = new PdfPCell();
            cellIzquierda.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellIzquierda.setLeading(0.5f, 0.8f);
            cellIzquierda.setBorder(0);
    
            // Agregar cabeceras a la tabla
            PdfPCell cabeceraCantidad = new PdfPCell(
                    new Phrase("Cantidad:", new Font(Font.FontFamily.COURIER, 9, Font.BOLD)));
            cabeceraCantidad.setHorizontalAlignment(Element.ALIGN_LEFT);
            cabeceraCantidad.setBorder(0);
            tablaDetalles.addCell(cabeceraCantidad);
    
            PdfPCell cabecerapDescripcion = new PdfPCell(
                    new Phrase("Descripción", new Font(Font.FontFamily.COURIER, 9, Font.BOLD)));
            cabecerapDescripcion.setHorizontalAlignment(Element.ALIGN_CENTER);
            cabecerapDescripcion.setBorder(0);
            tablaDetalles.addCell(cabecerapDescripcion);
    
            PdfPCell cabeceraMonto = new PdfPCell(new Phrase("Monto", new Font(Font.FontFamily.COURIER, 9, Font.BOLD)));
            cabeceraMonto.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cabeceraMonto.setBorder(0);
            tablaDetalles.addCell(cabeceraMonto);
    
            // Agregar los detalles de la venta
            for (DetallesVenta detalle : venta.getDetalles()) {
                cellDerecha.setPhrase(
                        new Phrase(detalle.getCantidad() + "Kg", new Font(Font.FontFamily.COURIER, 9, Font.NORMAL)));
                cellDerecha.setPaddingRight(0f);
                cellDerecha.setHorizontalAlignment(Element.ALIGN_LEFT); // Eliminar el espacio a la derecha
                tablaDetalles.addCell(cellDerecha);
    
                String descripcionProducto = detalle.getProducto().getNombre();
                cellIzquierda.setPhrase(
                        new Phrase(descripcionProducto, new Font(Font.FontFamily.COURIER, 9, Font.NORMAL)));
                tablaDetalles.addCell(cellIzquierda);
    
                cellDerecha.setPhrase(new Phrase(formatoDinero.format(detalle.getTotal()),
                        new Font(Font.FontFamily.COURIER, 9, Font.NORMAL)));
                cellDerecha.setPaddingRight(0f);
                cellDerecha.setHorizontalAlignment(Element.ALIGN_RIGHT); // Eliminar el espacio a la derecha
                tablaDetalles.addCell(cellDerecha);
            }
    
            document.add(tablaDetalles);
            document.add(lineaSeparadora);
    
            String montoIngresadoTexto = insertarPagoTextField.getText();
            BigDecimal montoIngresado;
            try {
                montoIngresado = new BigDecimal(montoIngresadoTexto);
            } catch (NumberFormatException e) {
                mostrarAlertaError("Error al generar el PDF",
                        "El monto ingresado no es válido. Por favor, ingrese un número válido.");
                document.close();
                writer.close();
                return;
            }
    
            BigDecimal cambio = montoIngresado.subtract(venta.getTotal());
    
            // Formatear monto ingresado, cambio y total como texto con formato de moneda
            String montoIngresadoFormateado = formatoDinero.format(montoIngresado.doubleValue());
            String cambioFormateado = formatoDinero.format(cambio.doubleValue());
            String totalFormateado = formatoDinero.format(venta.getTotal().doubleValue());
    
            Paragraph pagoYCambio = new Paragraph();
            pagoYCambio.add(new Chunk("Su Pago: " + montoIngresadoFormateado + "\n",
                    new Font(Font.FontFamily.COURIER, 9, Font.NORMAL)));
            pagoYCambio.add(
                    new Chunk("Su Cambio: " + cambioFormateado,
                            new Font(Font.FontFamily.COURIER, 9, Font.NORMAL)));
            pagoYCambio.setAlignment(Element.ALIGN_LEFT);
            pagoYCambio.setLeading(1f, 1f);
            document.add(pagoYCambio);
    
            // Mostrar el total sin el IVA
            Paragraph textoTotal = new Paragraph("TOTAL: " + totalFormateado,
                    new Font(Font.FontFamily.COURIER, 10, Font.BOLD));
            textoTotal.setAlignment(Element.ALIGN_CENTER);
            textoTotal.setLeading(1f, 1f);
            document.add(textoTotal);
    
            Paragraph gracias = new Paragraph("\n¡GRACIAS, VUELVA PRONTO!",
                    new Font(Font.FontFamily.COURIER, 9, Font.NORMAL));
            gracias.setAlignment(Element.ALIGN_CENTER);
            gracias.setLeading(1f, 1f);
            document.add(gracias);
    
            document.close();
            writer.close();
    
            System.out.println("PDF generado exitosamente en la carpeta de descargas: " + rutaPDF);
            printTicket(rutaPDF);
        } catch (FileNotFoundException e) {
            mostrarAlertaError("Error al generar el PDF",
                    "No se encontró el archivo de la imagen del logo. Verifique la ruta.");
            e.printStackTrace();
        } catch (IOException e) {
            mostrarAlertaError("Error al generar el PDF", "Ocurrió un error al escribir el archivo PDF.");
            e.printStackTrace();
        } catch (DocumentException e) {
            mostrarAlertaError("Error al generar el PDF", "Ocurrió un error en la creación del documento PDF.");
            e.printStackTrace();
        } catch (Exception e) {
            mostrarAlertaError("Error al generar el PDF",
                    "Ocurrió un error inesperado al generar el PDF. Por favor, intente nuevamente.");
            e.printStackTrace();
        }
    }

    private void guardarMovimiento(Movimientos movimiento) {
        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");
        configuration.addAnnotatedClass(Movimientos.class);

        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = emf.createEntityManager();

        entityManager.getTransaction().begin();
        entityManager.persist(movimiento);
        entityManager.getTransaction().commit();

        entityManager.close();
        emf.close();
    }

    private void printTicket(String url) {
        try {
          
            // Obtener la ruta del archivo PDF generado
            String numeroTicket = venta.getTicket();
            String nombreArchivo = "comprobante_" + numeroTicket + ".pdf";

            FileInputStream psStream = new FileInputStream(url);
            DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
            PrintService service = PrintServiceLookup.lookupDefaultPrintService();

            if (service == null) {
                System.out.println("No se encontró ninguna impresora disponible.");
                return;
            }

            Doc myDoc = new SimpleDoc(psStream, flavor, null);
            DocPrintJob job = service.createPrintJob();
            PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
            pras.add(new Copies(1));
            job.print(myDoc, pras);
        } catch (FileNotFoundException e) {
            mostrarAlertaError("Error al imprimir el ticket",
                    "No se encontró el archivo PDF. Por favor, genere el PDF primero.");
            e.printStackTrace();
        } catch (PrintException e) {
            mostrarAlertaError("Error al imprimir el ticket", "Ocurrió un error al imprimir el ticket.");
            e.printStackTrace();
        }
    }

}