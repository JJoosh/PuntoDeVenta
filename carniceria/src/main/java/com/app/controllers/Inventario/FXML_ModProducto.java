package com.app.controllers.Inventario;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class FXML_ModProducto implements Initializable {
    @FXML private TextField nombre;
    @FXML private TextField precio;
    @FXML private TextField id;
    @FXML private TextField cantidad;
    @FXML private ComboBox<String> categoria;

   
    @FXML
    public void modificar(){
        System.out.println("Modificar.....");
    }
    

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        FXMLInventarioController categorias=new FXMLInventarioController();
        categorias.cargarCategorias(this.categoria, 0);
       
    }

    public void setDatos(String nombre, BigDecimal cantidad, long id, BigDecimal precio, String categoria) {
        this.nombre.setText(nombre);
        this.cantidad.setText(String.valueOf(cantidad));
        this.id.setText(String.valueOf(id));
        this.precio.setText(String.valueOf(precio));
        this.categoria.setValue(categoria);
    }
}
