package com.app.controllers.Inventario;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import javafx.scene.Parent;


import com.app.models.Categoria;
import com.app.models.Productos;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;

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
    private ComboBox<String> categorias;


    private FXMLInventarioController inventarioController;
    
    
    public void setInventarioController(FXMLInventarioController inventarioController) {
        this.inventarioController = inventarioController;
    }

    @FXML
    public void agregar() {
        if (layout1.getText().isEmpty() == false && layout2.getText().isEmpty() == false && layout4.getText().isEmpty() == false
                && spinner1.getValue() != 4.9E-324 && spinner2.getValue() != 4.9E-324 && spinner3.getValue() != 4.9E-324
                && spinner1.getValue() > 0 && spinner2.getValue() > 0 && spinner3.getValue() > 0) {
            try {
                long codigoBarras = Integer.parseInt(layout1.getText());
                String descripcion = layout2.getText();
                Double precioCosto = spinner1.getValue();
                double precioVenta = spinner2.getValue();
                double precioMayoreo = spinner3.getValue();
                double cantidadKg = Double.parseDouble(layout4.getText());

                String Categoria=categorias.getValue().toString();
                System.out.println("Categoria: " +Categoria);
                Categoria id= new Categoria();
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
                 
                

                    Productos productosbd = new Productos();
                    productosbd.setId(codigoBarras);
                    productosbd.setNombre(descripcion);
                    productosbd.setCosto(BigDecimal.valueOf(precioCosto));
                    productosbd.setPrecio(BigDecimal.valueOf(precioVenta));
                    productosbd.setCantidad(BigDecimal.valueOf(cantidadKg));
                    productosbd.setCategoria(categoria);

                    session.save(productosbd);
                    tx.commit();
                    System.out.println("Producto insertado correctamente con ID: " + productosbd.getId());
                    System.out.println("Categoria: " +Categoria);
        
            inventarioController.actualizarTabla();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            
            alert.setHeaderText(null);
            alert.setContentText("Se guardo el producto correctamente\nID:"+productosbd.getId()+"\nNombre: "+productosbd.getNombre());
            alert.showAndWait();
                layout1.setText("");
                layout2.setText("");
                spinner1.getValueFactory().setValue((double) 0);
                spinner2.getValueFactory().setValue((double)0);
                spinner3.getValueFactory().setValue((double) 0);
                layout4.setText("");;
                    

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
                alert.setContentText("Por favor escribe los valores correctamente");
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Falto algo por escribir");
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
}