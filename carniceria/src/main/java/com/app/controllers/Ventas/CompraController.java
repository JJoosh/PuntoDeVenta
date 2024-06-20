package com.app.controllers.Ventas;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.app.models.Clientes;
import com.app.models.DetallesVenta;
import com.app.models.Movimientos;
import com.app.models.Productos;
import com.app.models.Ventas;
import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.itextpdf.text.Font;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;


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

    @FXML TableColumn<Productos, Void> ColumAcciones;

    @FXML
    private ComboBox<String> formaPagoComboBox;


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
        formaPagoComboBox.getItems().addAll("Efectivo", "Tarjeta");
        insertarPagoTextField.setVisible(false);
        

        // Agregar listener al ComboBox de forma de pago
        formaPagoComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            handlePaymentMethodChange(newValue);
        });

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

        ColumAcciones.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Productos producto = getTableView().getItems().get(getIndex());
        
                    Button deleteButton = new Button("Eliminar");
                    deleteButton.getStyleClass().add("btn_eli");
                    deleteButton.setOnAction(event -> {
                        System.out.println("Eliminar: " + producto.getNombre());
                        // Aquí va la lógica para eliminar el producto
                    });
        
                    setGraphic(new HBox(5, deleteButton)); 
                }
            }
        });
        
         rootPane.setOnKeyPressed(this::handleKeyPressed);
        Platform.runLater(() -> insertarPagoTextField.requestFocus());
    }


    private void handlePaymentMethodChange(String paymentMethod) {
        if ("Efectivo".equals(paymentMethod)) {
            insertarPagoTextField.setVisible(true);
        } else if ("Tarjeta".equals(paymentMethod)) {
            insertarPagoTextField.setVisible(false);
            calcularCambio();
        }
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
            String formaPago = formaPagoComboBox.getSelectionModel().getSelectedItem();
            if (formaPago == null) {
                mostrarAlertaError("Forma de pago no seleccionada",
                        "Por favor, seleccione una forma de pago antes de finalizar la compra.");
                return;
            }

            if ("Efectivo".equals(formaPago)) {
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
            }

            guardarVenta(formaPago);
            ImprimirTicket();
            actualizarInventario();

            importeTotal = BigDecimal.ZERO;

            regresarAVenta();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlertaError("Error al finalizar la compra",
                    "Ocurrió un error al finalizar la compra. Por favor, intente nuevamente.");
        }
    }

    private void guardarVenta(String formaPago) {
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
                detalle.setFormaPago(formaPago); // Guardar la forma de pago en el detalle de venta
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
        if (formaPagoComboBox.getSelectionModel().getSelectedItem().equals("Efectivo")) {
            try {
                BigDecimal montoIngresado = new BigDecimal(insertarPagoTextField.getText());
                BigDecimal cambio = montoIngresado.subtract(importeTotal);
                cambioLabel.setText(formatoDinero.format(cambio));
            } catch (NumberFormatException e) {
                cambioLabel.setText("0.00");
            }
        } else {
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

    private void ImprimirTicket() {
    try {
        if (venta == null) {
            mostrarAlertaError("Error al generar el ticket", "No se encontró información de la venta. Por favor, intente nuevamente.");
            return;
        }

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
    sb.append("       ----SuKarne----\n");
    sb.append("Tenosique, Tabasco\n");
    sb.append("Prolongacion Calle 28, Carretera la Palma\n");
    sb.append("Ficha de compra\n");
    sb.append("No. ticket: ").append(venta.getTicket()).append("\n");
    sb.append("Fecha: ").append(venta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
            .append("\n");
    sb.append("------------------------------------------------\n");
    sb.append(String.format("%-10s %-20s %10s\n", "Cantidad", "Descripcion", "Monto"));

    for (DetallesVenta detalle : venta.getDetalles()) {
        sb.append(String.format("%-10s %-20s %10s\n", detalle.getCantidad() + "Kg", detalle.getProducto().getNombre(),
                formatoDinero.format(detalle.getTotal())));
    }

    sb.append("------------------------------------------------\n");
    String formaPago = venta.getDetalles().get(0).getFormaPago();
    sb.append("Forma de Pago: ").append(formaPago).append("\n");

    if ("Efectivo".equals(formaPago)) {
        String montoIngresadoTexto = insertarPagoTextField.getText();
        BigDecimal montoIngresado = new BigDecimal(montoIngresadoTexto);
        BigDecimal cambio = montoIngresado.subtract(venta.getTotal());
        sb.append("Su Pago: ").append(formatoDinero.format(montoIngresado.doubleValue())).append("\n");
        sb.append("Su Cambio: ").append(formatoDinero.format(cambio.doubleValue())).append("\n");
    }

    sb.append("TOTAL: ").append(formatoDinero.format(venta.getTotal().doubleValue())).append("\n");
    sb.append("\n¡GRACIAS, VUELVA PRONTO!\n");
    sb.append("\n\n\n\n\n\n");
    sb.append("\n\n\n\n\n\n");

    return sb.toString();
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
}   