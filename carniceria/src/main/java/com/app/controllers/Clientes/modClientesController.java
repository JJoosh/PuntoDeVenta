package com.app.controllers.Clientes;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.app.models.Clientes;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

public class modClientesController implements Initializable {
    
    private String nombre;
    private String apellido;
    private int descuento;
    private int ID;
    
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private Spinner<Double> txtPorcentaje;
    @FXML Pane rootPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SpinnerValueFactory<Double> valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 100.0, 0.0, 1.0);
        txtPorcentaje.setValueFactory(valueFactory);
    }

    public void getData(int ID, String nombre, String apellido, int descuento) {
        this.ID = ID;
        this.nombre = nombre;
        this.apellido = apellido;
        this.descuento = descuento;
        
        txtNombre.setText(nombre);
        txtApellido.setText(apellido);
        txtPorcentaje.getValueFactory().setValue((double) descuento);
    }

    @FXML
    public void modificarCliente() {

        if (txtNombre.getText().isEmpty()==false && txtApellido.getText().isEmpty()==false) {
            try {
                Clientes cliente = new Clientes();
                double porcentaje = txtPorcentaje.getValue();
                cliente.modCliente(this.ID, this.txtNombre.getText(), this.txtApellido.getText(), (int) porcentaje);
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
