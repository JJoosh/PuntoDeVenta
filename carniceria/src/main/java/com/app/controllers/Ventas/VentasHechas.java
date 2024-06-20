package com.app.controllers.Ventas;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import com.app.controllers.Producto;
import com.app.models.DetallesVenta;
import com.app.models.Productos;
import com.app.models.Ventas;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class VentasHechas {

    private Session session;
    @FXML
    private Pane rootPane;

    @FXML
    private TableColumn<Ventas, String> detallesColumn;

    @FXML
    private TableView<Ventas> tablaDetallesVenta;
    @FXML
    private TableColumn<Ventas, String> ticketColumn;
    @FXML
    private TableColumn<Ventas, LocalDateTime> fechaColumn;
    @FXML
    private TableColumn<Ventas, BigDecimal> totalColumn;
    @FXML
    private TableColumn<Ventas, String> formaPagoColumn;

    private ObservableList<Ventas> ventasData = FXCollections.observableArrayList();

    public VentasHechas() {
        Configuration configuration = new Configuration().configure();
        configuration.addAnnotatedClass(Ventas.class);
        configuration.addAnnotatedClass(DetallesVenta.class);
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        session = sessionFactory.openSession();
    }

    @FXML
    private void initialize() {
        ticketColumn.setCellValueFactory(new PropertyValueFactory<>("ticket"));
        fechaColumn.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        formaPagoColumn.setCellValueFactory(cellData -> {
            Ventas venta = cellData.getValue();
            session.refresh(venta);
            StringBuilder formasPagoText = new StringBuilder();
            for (DetallesVenta detalle : venta.getDetalles()) {
                formasPagoText.append(detalle.getFormaPago()).append("\n");
            }
            return new SimpleStringProperty(formasPagoText.toString());
        });
        detallesColumn.setCellValueFactory(cellData -> {
            Ventas venta = cellData.getValue();
            session.refresh(venta);
            StringBuilder detallesText = new StringBuilder();
            for (DetallesVenta detalle : venta.getDetalles()) {
                detallesText.append(detalle.getProducto().getNombre()).append(" (")
                        .append(detalle.getCantidad()).append(" kg) - $")
                        .append(detalle.getTotal())
                        .append("\n");
            }
            return new SimpleStringProperty(detallesText.toString());
        });

        cargarVentas();
    }

    private void cargarVentas() {
        Configuration configuration = new Configuration().configure();
        configuration.addAnnotatedClass(Ventas.class);
        configuration.addAnnotatedClass(DetallesVenta.class);
        SessionFactory sessionFactory = null;
        EntityManagerFactory entityManagerFactory = null;
        EntityManager entityManager = null;

        try {
            sessionFactory = configuration.buildSessionFactory();
            entityManagerFactory = sessionFactory.unwrap(EntityManagerFactory.class);
            entityManager = entityManagerFactory.createEntityManager();

            TypedQuery<Ventas> query = entityManager.createQuery("SELECT v FROM Ventas v JOIN FETCH v.detalles",
                    Ventas.class);
            List<Ventas> ventas = query.getResultList();

            ventasData.clear();
            ventasData.addAll(ventas);
            tablaDetallesVenta.setItems(ventasData);
        } catch (Exception e) {
            e.printStackTrace();
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
    private void imprimirTicket() {
        Ventas ventaSeleccionada = tablaDetallesVenta.getSelectionModel().getSelectedItem();
        if (ventaSeleccionada != null) {
            try {
                String contenidoTicket = generarContenidoTicket(ventaSeleccionada);
                if (!contenidoTicket.isEmpty()) {
                    PrintService printService = PrintServiceLookup.lookupDefaultPrintService();
                    DocPrintJob printJob = printService.createPrintJob();
                    PrintRequestAttributeSet attributeSet = new HashPrintRequestAttributeSet();
                    attributeSet.add(new Copies(1)); // Número de copias

                    byte[] bytes = contenidoTicket.getBytes();
                    Doc doc = new SimpleDoc(bytes, DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
                    printJob.print(doc, attributeSet);

                    mostrarAlertaInformacion("Ticket generado", "El ticket se ha generado e impreso correctamente.");
                } else {
                    mostrarAlertaError("Error al imprimir", "No se pudo generar el contenido del ticket.");
                }
            } catch (Exception e) {
                mostrarAlertaError("Error al generar el ticket",
                        "Ocurrió un error inesperado al generar el ticket. Por favor, intente nuevamente.");
                e.printStackTrace();
                System.out.println(e);
            }
        } else {
            mostrarAlertaError("Error al generar el ticket", "Debe seleccionar una venta de la tabla.");
        }
    }

    public Productos obtenerProductoPorId(long productoId) {
        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");
        configuration.addAnnotatedClass(Productos.class);

        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        Productos producto = null;

        try {
            transaction = session.beginTransaction();
            producto = session.get(Productos.class, productoId);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
            sessionFactory.close();
        }

        return producto;
    }

    private String generarContenidoTicket(Ventas venta) {
        TextFlow ticketContent = new TextFlow();
        Text header = new Text("Detalles de la Venta:\n");

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("       ----SuKarne----\n");
        stringBuilder.append("Tenosique, Tabasco\n");
        stringBuilder.append("Prolongacion Calle 28, Carretera la Palma\n");
        stringBuilder.append("------------------------------------------------\n");
        Text content = new Text("Ticket: " + venta.getTicket() + "\n" +
                "Fecha: " + venta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n" +
                "Total: $" + venta.getTotal().toPlainString() + "\n");

        for (DetallesVenta detalle : venta.getDetalles()) {
            Productos producto = obtenerProductoPorId(detalle.getProducto().getId());
            String nombreProducto = producto != null ? producto.getNombre() : "Producto desconocido";
            content.setText(content.getText() + "Producto: " + nombreProducto + " (" + detalle.getCantidad()
                    + " kg) - $" + detalle.getTotal() + "\n");
        }

        content.setText(content.getText() + "Forma de pago: " + venta.getDetalles().get(0).getFormaPago() + "\n\n");

        ticketContent.getChildren().addAll(header, content);

        stringBuilder.append("\n\n\n\n\n\n");
        stringBuilder.append("\n\n\n\n\n\n");
        for (Node node : ticketContent.getChildren()) {
            if (node instanceof Text) {
                stringBuilder.append(((Text) node).getText());
            }
        }

        return stringBuilder.toString();
    }

    private boolean imprimir(Node node) {
        if (node == null || node.getScene() == null || node.getScene().getWindow() == null) {
            return false; 
        }

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(node.getScene().getWindow())) {
            boolean success = job.printPage(node);
            job.endJob();
            return success;
        }
        return false;
    }

    private void mostrarAlertaInformacion(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAlertaError(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    public void regresar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Ventas.fxml"));
            Pane nuevoContenido = loader.load();
            VentasController ventasController = loader.getController();
            rootPane.getChildren().setAll(nuevoContenido);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cerrar() {
        if (session != null) {
            session.close();
        }
    }
}