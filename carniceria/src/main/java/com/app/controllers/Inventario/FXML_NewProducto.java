package com.app.controllers.Inventario;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.exception.ConstraintViolationException;

import com.app.controllers.Ventas.VentasController;
import com.app.models.Categoria;
import com.app.models.Movimientos;
import com.app.models.Productos;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class FXML_NewProducto {
    @FXML
    private TextField layout1;
    @FXML
    private TextField layout4;
    @FXML
    private TextField layout2;
    @FXML
    private Spinner<Double> spinner1;
    @FXML
    private Spinner<Double> spinner2;
    @FXML
    private Spinner<Double> spinner3;
    @FXML
    private TextField layout5;
    @FXML
    private ComboBox<String> categorias;

    private Stage stage;
    private FXMLInventarioController inventarioController;
    
    
    public void setInventarioController(FXMLInventarioController inventarioController) {
        this.inventarioController = inventarioController;
    }

    public void setStage(Stage stage){
        this.stage=stage;
    }

    @FXML
   public void agregar() {
    LocalDateTime fechaHoraActual = LocalDateTime.now();

    if (layout1.getText().isEmpty() == false && layout2.getText().isEmpty() == false && layout4.getText().isEmpty() == false
            && spinner1.getValue() != 4.9E-324 && spinner2.getValue() != 4.9E-324 && spinner3.getValue() != 4.9E-324
            && spinner1.getValue() > 0 && spinner2.getValue() > 0 && spinner3.getValue() > 0) {
        try {
            long codigoBarras = Integer.parseInt(layout1.getText());
            String descripcion = layout2.getText();
            Double precioCosto = spinner1.getValue();
            double precioVenta = spinner2.getValue();
            double invMinimo = spinner3.getValue();
            int cantidadCajas = Integer.parseInt(layout4.getText());
            BigDecimal pesoCaja= BigDecimal.valueOf(Double.parseDouble(layout5.getText()));

            BigDecimal cantidadKg = pesoCaja.multiply(BigDecimal.valueOf(cantidadCajas));

            String Categoria = categorias.getValue().toString();
            System.out.println("Categoria: " + Categoria);
            Categoria id = new Categoria();
            Long categoriaId = id.getIDconName(Categoria);
            Categoria categoria = null;
            Configuration configuration = new Configuration().configure();
            SessionFactory sessionFactory = configuration.buildSessionFactory();
            Session session = sessionFactory.openSession();
            Transaction tx = null;

            try {
                tx = session.beginTransaction();

                // Buscar la categoría por su ID
                categoria = session.get(Categoria.class, categoriaId);

                Movimientos movimientos = new Movimientos();

                Productos productosbd = new Productos();
                productosbd.setId(codigoBarras);
                productosbd.setNombre(descripcion);
                productosbd.setCosto(BigDecimal.valueOf(precioCosto));
                productosbd.setPrecio(BigDecimal.valueOf(precioVenta));
                productosbd.setCantidad(cantidadKg);
                productosbd.setCategoria(categoria);
                productosbd.setProductosBajos_inventario(BigDecimal.valueOf(invMinimo));
                productosbd.setPesoCaja(pesoCaja);
                productosbd.setActivo("S");

                session.save(productosbd);
                System.out.println("FECHA DE PRUEBA"+fechaHoraActual);
                movimientos.setIdProducto(productosbd);
                movimientos.setTipoMovimiento("Entrada");
                movimientos.setCantidad(cantidadKg);
                movimientos.setFecha(fechaHoraActual);
                session.save(movimientos);
                tx.commit();
                System.out.println("Producto insertado correctamente con ID: " + productosbd.getId());
                System.out.println("Categoria: " + Categoria);
                
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText(null);
                alert.setContentText("Se guardo el producto correctamente\nID:" + productosbd.getId() + "\nNombre: " + productosbd.getNombre());
                alert.showAndWait();
                layout1.setText("");
                layout2.setText("");
                spinner1.getValueFactory().setValue((double) 0);
                spinner2.getValueFactory().setValue((double) 0);
                spinner3.getValueFactory().setValue((double) 0);
                layout4.setText("");
            } catch (ConstraintViolationException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("El ID del producto ya existe en la base de datos.");
                alert.showAndWait();
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            } finally {
                session.close();
                sessionFactory.close();
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Por favor escribe los valores correctamente.");
            alert.showAndWait();
        }
    } else {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Faltó algo por escribir.");
        alert.showAndWait();
    }
}

  

    public void initialize() {
        SpinnerValueFactory<Double> valueFactory1 = new SpinnerValueFactory.DoubleSpinnerValueFactory(
                Double.MIN_VALUE, Double.MAX_VALUE, 0.0, 0.1);
        spinner1.setValueFactory(valueFactory1);

        SpinnerValueFactory<Double> valueFactory2 = new SpinnerValueFactory.DoubleSpinnerValueFactory(
                Double.MIN_VALUE, Double.MAX_VALUE, 0.0, 0.1);
        spinner2.setValueFactory(valueFactory2);

        SpinnerValueFactory<Double> valueFactory3 = new SpinnerValueFactory.DoubleSpinnerValueFactory(
                Double.MIN_VALUE, Double.MAX_VALUE, 0.0, 0.1);
        spinner3.setValueFactory(valueFactory3);

        FXMLInventarioController loadCat= new FXMLInventarioController();

        loadCat.cargarCategorias(this.categorias, 0);


    }
    @FXML
    private Pane rootPane;
    public void cerrar(){
        try {
        // Cargar el archivo FXML con el nuevo contenido
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Inventario.fxml"));
        Pane nuevoContenido = loader.load();
        
        
        FXMLInventarioController inventarioController = loader.getController();
       
        rootPane.getChildren().setAll(nuevoContenido);
    } catch (IOException e) {
        e.printStackTrace();
    }
    }
}