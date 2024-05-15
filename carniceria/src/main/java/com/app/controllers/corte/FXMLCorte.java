package com.app.controllers.corte;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.io.InputStream;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
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

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.app.models.Ventas;
import com.ibm.icu.text.DecimalFormat;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.app.models.DetallesVenta;
import com.app.models.Productos;
import com.app.models.CortedeCaja;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FXMLCorte {
    private DecimalFormat formatoDinero = new DecimalFormat("$#,##0.00");
    @FXML
    private TableView<tablecorte> Corte;
    @FXML
    private TableColumn<tablecorte, String> Ticket;
    @FXML
    private TableColumn<tablecorte, String> Producto;
    @FXML
    private TableColumn<tablecorte, String> Nombre;
    @FXML
    private TableColumn<tablecorte, String> Fecha;
    @FXML
    private TableColumn<tablecorte, BigDecimal> Total;
    @FXML
    private TableColumn<tablecorte, String> Pago;
    @FXML
    private TableColumn<tablecorte, BigDecimal> cantidad;

    @FXML
    private DatePicker datePicker;

    @FXML
    private Pane rootPane;

    private ObservableList<tablecorte> datosTabla = FXCollections.observableArrayList();
    private List<Ventas> listaVentas;
    private List<CortedeCaja> cortesDeCaja = new ArrayList<>();
    private LocalDate fechadocumento = LocalDate.now(); // Inicializar fechadocumento con la fecha actual

    public void initialize() {
        configurarTabla();
        listaVentas = obtenerListaDeVentas();
        if (datePicker.getValue() == null) {
            LocalDate fecha = LocalDate.now();
            filtrarCortePorFecha(fecha);
        }

        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                datosTabla.clear();
                Corte.setItems(datosTabla);
            } else {
                LocalDate fecha = newValue;
                filtrarCortePorFecha(fecha);
            }
        });
        if (rootPane != null) {
            rootPane.setOnKeyPressed(this::handleKeyPressed);
        } else {
            // Manejar el caso en el que rootPane sea nulo
        }
    }

    private void configurarTabla() {
        Ticket.setCellValueFactory(new PropertyValueFactory<>("ticket1"));
        Producto.setCellValueFactory(new PropertyValueFactory<>("detalle"));
        Nombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        Fecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        Total.setCellValueFactory(new PropertyValueFactory<>("total"));
        Pago.setCellValueFactory(new PropertyValueFactory<>("pago"));
        cantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        datosTabla = FXCollections.observableArrayList();
    }

    public List<Ventas> obtenerListaDeVentas() {
        Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = emf.createEntityManager();
        TypedQuery<Ventas> query = entityManager.createQuery("SELECT v FROM Ventas v JOIN FETCH v.detalles", Ventas.class);
        List<Ventas> ventas = query.getResultList();
        entityManager.close();
        emf.close();
        return ventas;
    }

    private void filtrarCortePorFecha(LocalDate fecha) {
        List<tablecorte> cortesFiltrados = obtenerCorteDeCaja(fecha);
        datosTabla.clear();
        datosTabla.addAll(cortesFiltrados);
        Corte.setItems(datosTabla);
    }

    private List<tablecorte> obtenerCorteDeCaja(LocalDate fecha) {
        List<tablecorte> cortesDeCaja = new ArrayList<>();
        BigDecimal totalVentas = BigDecimal.ZERO;
        BigDecimal totalCantidadProductos = BigDecimal.ZERO;

        for (Ventas venta : listaVentas) {
            String ticket = venta.getTicket();
            LocalDate fechaVenta = venta.getFecha().toLocalDate();
            LocalDateTime fechaVenta1 = venta.getFecha();
            if (fecha == null || fechaVenta.equals(fecha)) {
                totalVentas = totalVentas.add(venta.getTotal());

                for (DetallesVenta detalle : venta.getDetalles()) {
                    Productos producto = detalle.getProducto();
                    String nombreProducto = producto.getNombre();
                    BigDecimal cantidad = detalle.getCantidad();
                    String formaPago = detalle.getFormaPago();
                    System.out.println(formaPago + "---");
                    System.out.println(fechaVenta + "---");

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // Formato de fecha y hora sin 'T'

                    // Ahora puedes formatear la fecha y la hora como una cadena de texto sin la 'T'
                    String fechaYHoraSinT = fechaVenta1.format(formatter);

                    BigDecimal totalVenta = detalle.getTotal();

                    tablecorte corte = new tablecorte(ticket, producto.getId(), nombreProducto, cantidad, fechaYHoraSinT, totalVenta, formaPago);
                    cortesDeCaja.add(corte);

                    totalCantidadProductos = totalCantidadProductos.add(cantidad);
                }
            }
        }

        if (!cortesDeCaja.isEmpty()) {
            LocalDateTime fechaCorte = listaVentas.get(0).getFecha();
            CortedeCaja corteCaja = new CortedeCaja(null, fechaCorte, totalVentas, totalCantidadProductos);
            this.cortesDeCaja.add(corteCaja);
        }

        return cortesDeCaja;
    }

    @FXML
    private void corte(ActionEvent event) {
        proceso();
    }

    private void proceso() {
        if (datePicker.getValue() != null) {
            Imprimir();
            fechadocumento = datePicker.getValue();
        } else {
            Imprimir();
            fechadocumento = LocalDate.now();
        }
    }

    private void Imprimir() {
        try {
            String contenidoTicket = generarContenidoTicket();
            PrintService printService = PrintServiceLookup.lookupDefaultPrintService();
            DocPrintJob printJob = printService.createPrintJob();
            PrintRequestAttributeSet attributeSet = new HashPrintRequestAttributeSet();
            attributeSet.add(new Copies(1)); // Número de copias
            byte[] bytes = contenidoTicket.getBytes();
            Doc doc = new SimpleDoc(bytes, DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
            printJob.print(doc, attributeSet);
            mostrarAlertaInformacion("Ticket generado", "El ticket se ha generado e imprimido correctamente.");
        } catch (Exception e) {
            mostrarAlertaError("Error al generar el ticket", "Ocurrió un error inesperado al generar el ticket. Por favor, intente nuevamente.");
            e.printStackTrace();
            System.out.println(e);
        }
    }

    private String generarContenidoTicket() {
        StringBuilder sb = new StringBuilder();
        sb.append("SuKarne\n");
        sb.append("Tenosique, Tabasco\n");
        sb.append("Prolongacion Calle 28, Carretera la Palma\n");

        // Agregar el título del corte de caja
        sb.append("Corte de Caja\n");

        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate != null) {
            sb.append("Fecha: ").append(selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        } else {
            sb.append("Fecha: ").append(fechadocumento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        }

        sb.append("------------------------------------------------\n");
        sb.append(String.format("%-10s %-20s %10s\n", "Cantidad", "Producto", "Total"));

        // Imprimir los detalles de cada venta directamente desde la tabla Corte
        for (tablecorte corte : Corte.getItems()) {
            sb.append(String.format("%-10s %-20s %10s\n",
                    corte.getCantidad(),
                    corte.getNombre(),
                    formatoDinero.format(corte.getTotal())));
        }

        sb.append("------------------------------------------------\n");
        BigDecimal totalVentas = BigDecimal.ZERO;
        BigDecimal totalCantidadProductos = BigDecimal.ZERO;

        for (tablecorte corte : Corte.getItems()) {
            totalVentas = totalVentas.add(corte.getTotal());
            totalCantidadProductos = totalCantidadProductos.add(corte.getCantidad());
        }

        sb.append("Total Ventas: ").append(formatoDinero.format(totalVentas)).append("\n");
        sb.append("Total Cantidad Productos: ").append(totalCantidadProductos).append("\n");
        sb.append("\n\n\n\n\n\n");
        sb.append("\n\n\n\n\n\n");

        // Generar el archivo PDF con el contenido del ticket
        generarPDF(sb);

        return sb.toString();
    }

    private void generarPDF(StringBuilder sb) {
        String descargas = System.getProperty("user.home") + "/Downloads/";
        LocalDate fechaParaNombreArchivo;
        if (datePicker.getValue() != null) {
            fechaParaNombreArchivo = datePicker.getValue();
        } else {
            fechaParaNombreArchivo = fechadocumento;
        }
        String nombreArchivo = "corte_caja_" + fechaParaNombreArchivo.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
        String rutaCompleta = descargas + nombreArchivo;
    
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(rutaCompleta));
            document.open();
    
            // Agregar el contenido del StringBuilder al documento PDF
            document.add(new Paragraph(sb.toString()));
    
            document.close();
            System.out.println("El archivo PDF se ha guardado en: " + rutaCompleta);
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.F5) {
            proceso();
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
}