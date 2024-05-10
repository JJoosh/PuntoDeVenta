package com.app.controllers.Inventario;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.app.models.Movimientos;

import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

public class MovimientosController implements Initializable {
    @FXML private TableView<Movimientos> tablaDevolu;
    @FXML private TableColumn<Movimientos, Date> fecha;
    @FXML private TableColumn<Movimientos, String> productos;
    @FXML private TableColumn<Movimientos, String> movimiento;
    @FXML private TableColumn<Movimientos, BigDecimal> cantidad;
    @FXML private TableColumn<Movimientos, LocalTime> hora;
    @FXML private TableColumn<Movimientos, String> columCategoria;
    @FXML private DatePicker fechas;
    @FXML private ComboBox<String> categorias;
    @FXML private ComboBox<String> boxMovimiento;
    @FXML private TextField fproducto;

    private Stage stage;

    private ObservableList<Movimientos> tablaDevoluciones;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Inicializar la lista observable
        tablaDevoluciones = FXCollections.observableArrayList();

        // Asignar la lista observable a la tabla
        tablaDevolu.setItems(tablaDevoluciones);

        // Configurar las celdas de la tabla
        fecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        productos.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        movimiento.setCellValueFactory(new PropertyValueFactory<>("tipoMovimiento"));
        cantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        hora.setCellValueFactory(new PropertyValueFactory<>("hora") );
        columCategoria.setCellValueFactory(new PropertyValueFactory<>("nombreCategoria"));

        cargarTabla();

        FXMLInventarioController loadCat= new FXMLInventarioController();

        loadCat.cargarCategorias(this.categorias, 1);
        filtrarCategorias();
        ObservableList<String> movimientos = FXCollections.observableArrayList(
            "Todos",  "Entrada", "Salida"
        );
        boxMovimiento.setItems(movimientos);
        boxMovimiento.setValue("Todos");
        LocalDate fechaActual = LocalDate.now();
        fechas.setValue(fechaActual);

        fechas.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                List<Movimientos> movimientosFiltrados = obtenerMovimientosPorFecha(newValue);
                tablaDevoluciones.clear();
                tablaDevoluciones.addAll(movimientosFiltrados);
            }
        });

        boxMovimiento.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                List<Movimientos> movimientosFiltrados = obtenerMovimientosPorTipo(newValue);
                tablaDevoluciones.clear();
                tablaDevoluciones.addAll(movimientosFiltrados);
            }
        });


        fproducto.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                List<Movimientos> movimientosFiltrados = obtenerMovimientosPorNombreProducto(newValue);
                tablaDevoluciones.clear();
                tablaDevoluciones.addAll(movimientosFiltrados);
            } else {
                cargarTabla();
            }
        });
    }

    @FXML
    private void cargarTabla() {
        List<Movimientos> movimientos = obtenerMovimientos();
        tablaDevoluciones.clear();
        tablaDevoluciones.addAll(movimientos);
    }

    private List<Movimientos> obtenerMovimientos() {
        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");
        configuration.addAnnotatedClass(Movimientos.class);

        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = emf.createEntityManager();

        TypedQuery<Movimientos> query = entityManager.createQuery("SELECT m FROM Movimientos m", Movimientos.class);
        List<Movimientos> movimientos = query.getResultList();

        entityManager.close();
        emf.close();

        return movimientos;
    }

    private List<Movimientos> obtenerMovimientosPorFecha(LocalDate fecha) {
        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");
        configuration.addAnnotatedClass(Movimientos.class);
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = emf.createEntityManager();

        // Convertir la fecha seleccionada al formato de fecha utilizado en la base de datos
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaString = fecha.format(formatter);

        TypedQuery<Movimientos> query = entityManager.createQuery(
            "SELECT m FROM Movimientos m WHERE m.fecha = :fecha", Movimientos.class);
        query.setParameter("fecha", fechaString);

        List<Movimientos> movimientosFiltrados = query.getResultList();

        entityManager.close();
        emf.close();

        return movimientosFiltrados;
    }

    public void filtrarCategorias() {
        categorias.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals("Todos")) {
                cargarTabla();
            } else {
                List<Movimientos> movimientosFiltrados = obtenerMovimientosPorCategoria(newValue);
                tablaDevoluciones.clear();
                tablaDevoluciones.addAll(movimientosFiltrados);
            }
        });
    }

    private List<Movimientos> obtenerMovimientosPorCategoria(String categoria) {
        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");
        configuration.addAnnotatedClass(Movimientos.class);
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = emf.createEntityManager();

        TypedQuery<Movimientos> query = entityManager.createQuery(
            "SELECT m FROM Movimientos m WHERE m.id_producto.categoria.nombreCategoria = :categoria", Movimientos.class);
        query.setParameter("categoria", categoria);

        List<Movimientos> movimientosFiltrados = query.getResultList();

        entityManager.close();
        emf.close();

        return movimientosFiltrados;
    }

    private List<Movimientos> obtenerMovimientosPorTipo(String tipoMovimiento) {
        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");
        configuration.addAnnotatedClass(Movimientos.class);
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = emf.createEntityManager();
    
        TypedQuery<Movimientos> query;
        if (tipoMovimiento.equals("Todos")) {
            query = entityManager.createQuery("SELECT m FROM Movimientos m", Movimientos.class);
        } else {
            query = entityManager.createQuery(
                "SELECT m FROM Movimientos m WHERE m.tipo_movimiento = :tipoMovimiento", Movimientos.class);
            query.setParameter("tipoMovimiento", tipoMovimiento);
        }
    
        List<Movimientos> movimientosFiltrados = query.getResultList();
    
        entityManager.close();
        emf.close();
    
        return movimientosFiltrados;
    }
    private List<Movimientos> obtenerMovimientosPorNombreProducto(String nombreProducto) {
        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");
        configuration.addAnnotatedClass(Movimientos.class);
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = emf.createEntityManager();
    
        TypedQuery<Movimientos> query = entityManager.createQuery(
            "SELECT m FROM Movimientos m WHERE m.id_producto.nombre LIKE :nombreProducto", Movimientos.class);
        query.setParameter("nombreProducto", "%" + nombreProducto + "%");
    
        List<Movimientos> movimientosFiltrados = query.getResultList();
        entityManager.close();
        emf.close();
    
        return movimientosFiltrados;
    }




    public void exportarExcell() {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Movimientos");

    // Crear la fila de encabezado
    Row headerRow = sheet.createRow(0);
    headerRow.createCell(0).setCellValue("Fecha");
    headerRow.createCell(1).setCellValue("Producto");
    headerRow.createCell(2).setCellValue("Movimiento");
    headerRow.createCell(3).setCellValue("Cantidad");
    headerRow.createCell(4).setCellValue("Hora");
    headerRow.createCell(5).setCellValue("Categoría");

    // Agregar los datos de la tabla a la hoja de Excel
    int rowNum = 1;
    for (Movimientos movimiento : tablaDevoluciones) {
        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(movimiento.getFecha());
        row.createCell(1).setCellValue(movimiento.getNombreProducto());
        row.createCell(2).setCellValue(movimiento.getTipoMovimiento());
        row.createCell(3).setCellValue(movimiento.getCantidad().doubleValue());
        row.createCell(4).setCellValue(movimiento.getHora().toString());
        row.createCell(5).setCellValue(movimiento.getNombreCategoria());
    }

    // Obtener la fecha actual en la zona horaria deseada
    ZoneId zonaHoraria = ZoneId.of("America/Mexico_City"); // Reemplaza con la zona horaria deseada
    LocalDate fechaActual = LocalDate.now(zonaHoraria);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    String fechaActualStr = fechaActual.format(formatter);

    // Crear el nombre del archivo con la fecha actual
    String nombreArchivo = "Movimientos " + fechaActualStr + ".xlsx";

    // Guardar el archivo de Excel
    try (FileOutputStream outputStream = new FileOutputStream(nombreArchivo)) {
        workbook.write(outputStream);
        workbook.close();
        Alert alert=new Alert(AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText("Se exporto correctamente el documento Excell");
        alert.showAndWait();
    } catch (IOException e) {
        e.printStackTrace();
    }
}

public void setStage(Stage stage){
    this.stage=stage;
}
public void regresar(){
    this.stage.close();
}

}