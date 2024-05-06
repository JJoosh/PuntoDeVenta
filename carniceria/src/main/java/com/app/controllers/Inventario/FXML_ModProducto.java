package com.app.controllers.Inventario;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;



import com.app.models.Categoria;
import com.app.models.Productos;

public class FXML_ModProducto implements Initializable {
    @FXML private TextField nombre;
    @FXML private TextField precio;
    @FXML private TextField id;
    @FXML private TextField cantidad;
    @FXML private TextField costo;
    @FXML private ComboBox<String> categoria;
    private FXMLInventarioController table;
    private Stage stage; 
   
   public void setInventarioController(FXMLInventarioController inventarioController) {
        this.table = inventarioController;
    }

      public void setStage(Stage stage) {
        this.stage = stage;
    }
    @FXML
    public void modificar(){
        
        Productos modProducto=new Productos();
        Categoria categorias=new Categoria();
        
        
        try {
            
            modProducto.modificarProducto(
                Long.parseLong(this.id.getText()), 
                nombre.getText(), 
                BigDecimal.valueOf(Double.parseDouble(costo.getText())), 
                categorias.getIDconName(categoria.getValue()), 
                BigDecimal.valueOf(Double.parseDouble(cantidad.getText())), 
                BigDecimal.valueOf(Double.parseDouble(precio.getText()))
            );

            table.actualizarTabla();
            System.out.println("Esta es la IDDD"+categorias.getIDconName(categoria.getValue()));
        Alert alert=new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Producto Modificado");
        alert.setHeaderText(null);
        alert.setContentText("Se modifico el producto correctamente");
        alert.showAndWait();
        stage.close();
        } catch (Exception e) {
            System.out.println(e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Error al modificar el producto, revise que todo este correctamente");
            alert.showAndWait();
        }
        
        }
       
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        FXMLInventarioController categorias=new FXMLInventarioController();
        categorias.cargarCategorias(this.categoria, 0);
    }

    public void setDatos(String nombre, BigDecimal costo, BigDecimal cantidad, long id, BigDecimal precio, String categoria) {
        
        
        this.nombre.setText(nombre);
        this.costo.setText(String.valueOf(costo));
        this.cantidad.setText(String.valueOf(cantidad));
        this.id.setText(String.valueOf(id));
        this.precio.setText(String.valueOf(precio));
        this.categoria.setValue(categoria);
    }
}
