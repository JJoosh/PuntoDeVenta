package com.app.controllers.Clientes;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.app.models.Clientes;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

public class modClientesController implements Initializable {
    
    private String nombre;
    private String apellido;
    private String descuento;
    private int ID;
    
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtNumero;
    @FXML Pane rootPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        
    }

    public void getData(int ID, String nombre, String apellido, String descuento) {
        this.ID = ID;
        this.nombre = nombre;
        this.apellido = apellido;
        this.descuento = descuento;
        
        txtNombre.setText(nombre);
        txtApellido.setText(apellido);
        txtNumero.setText(descuento);
    }

    @FXML
    public void modificarCliente() {

        if (txtNombre.getText().isEmpty()==false && txtApellido.getText().isEmpty()==false) {
            try {
                Clientes cliente = new Clientes();
                
                cliente.modCliente(this.ID, this.txtNombre.getText(), this.txtApellido.getText(), txtNumero.getText().toString());
                Alert aler=new Alert(Alert.AlertType.CONFIRMATION);
                aler.setTitle(null);
                aler.setHeaderText(null);
                aler.setContentText("Se realizo el cambio correctamente");
                aler.showAndWait();

                cerrar();

            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
       
    }

    @FXML
    public void cerrar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/clientes2.fxml"));
            Pane nuevoContenido = loader.load();
            ClientesController clienteController = loader.getController();
            rootPane.getChildren().setAll(nuevoContenido);
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }
}
