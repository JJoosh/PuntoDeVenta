package com.app.controllers.Clientes;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.app.models.Clientes;
import com.app.utils.HibernateUtil;

public class ClientesController implements Initializable {

    @FXML
    private TableView<Clientes> tableView;
    @FXML
    private TableColumn<Clientes, String> nombre, apellido;
    @FXML
    private TableColumn<Clientes, Number> descuento;
    @FXML
    private TableColumn<Clientes, Void> acciones;
    @FXML Pane rootPane;
    @FXML TextField txtNombre;
    @FXML TextField txtApellido;
    @FXML TextField txtDescuento;

    @FXML Button eliminar;
    @FXML Button modificar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
       mostrarTabla();
    }


    public void mostrarTabla(){
         // Configurar las columnas
         nombre.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
         apellido.setCellValueFactory(cellData -> cellData.getValue().apellidoProperty());
         descuento.setCellValueFactory(cellData -> cellData.getValue().descuentoProperty());
 
         // Cargar datos desde la base de datos usando Hibernate
         ObservableList<Clientes> data = FXCollections.observableArrayList();
         try (Session session = HibernateUtil.getSessionFactory().openSession()) {   
             List<Clientes> clientesList = session.createQuery("from Clientes where activo = 'A'", Clientes.class).list();
             data.addAll(clientesList);
         }
 
         tableView.setItems(data);
 
         // Configurar la columna de acciones con los botones existentes
         acciones.setCellFactory(new Callback<TableColumn<Clientes, Void>, TableCell<Clientes, Void>>() {
             @Override
             public TableCell<Clientes, Void> call(final TableColumn<Clientes, Void> param) {
                 final TableCell<Clientes, Void> cell = new TableCell<Clientes, Void>() {
 
                     @Override
                     public void updateItem(Void item, boolean empty) {
                         super.updateItem(item, empty);
                         if (empty) {
                             setGraphic(null);
                         } else {
                             Button deleteButton = new Button("Eliminar");
                             deleteButton.getStyleClass().add("btn_eli");
                             deleteButton.setOnAction(event -> {
                                 Clientes cliente = getTableView().getItems().get(getIndex());
                                 System.out.println("Eliminar: " + cliente.getNombre() + " " + cliente.getApellido());
                                 cliente.setActivo("N"); 
                                 mostrarTabla();
                             });
 
                             Button editButton = new Button("Modificar");
                             editButton.getStyleClass().add("btn_mod");
                             editButton.setOnAction(event -> {
                                 Clientes cliente = getTableView().getItems().get(getIndex());
                                 // Lógica para modificar el cliente
                                 System.out.println("Modificar: " + cliente.getNombre() + " " + cliente.getApellido());
                             });
 
                             setGraphic(new HBox(5, editButton, deleteButton)); // 5 es el espacio entre los botones
                         }
                     }
                 };
                 return cell;
             }
         });
    }


   public void agregarCliente(){
    System.out.println("Iniciando agregarxd");

    if (txtApellido.getText().isEmpty()==false && txtNombre.getText().isEmpty()==false && txtDescuento.getText().isEmpty()==false) {
        String apellido="";
        String nombre="";
        int descuento=0;
        try {
            apellido = this.txtApellido.getText().toString();
            nombre= this.txtNombre.getText().toString();
            descuento = Integer.parseInt(this.txtDescuento.getText().toString());
            
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                Clientes nuevoCliente = new Clientes();
                nuevoCliente.setNombre(nombre);
                nuevoCliente.setApellido(apellido);
                nuevoCliente.setDescuento(descuento);
                nuevoCliente.setActivo("A");
    
                session.save(nuevoCliente);
    
                tx.commit();
                this.txtApellido.setText("");
                this.txtNombre.setText("");
                this.txtDescuento.setText("");
                mostrarTabla();
            } catch (HibernateException e) {
                
            }
        } catch (Exception e) {
                Alert alert=new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Escriba los campos correctamente");
                alert.showAndWait();
        }
        
        
    }
    else{
        Alert alert=new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Complete los campos faltantes para ingresar un nuevo cliente");
        alert.showAndWait();
    }
    
}

}
