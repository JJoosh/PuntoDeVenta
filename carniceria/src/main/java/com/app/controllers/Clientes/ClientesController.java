package com.app.controllers.Clientes;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.app.models.Clientes;
import com.app.models.Productos;
import com.app.models.Usuarios;
import com.app.utils.HibernateUtil;

public class ClientesController implements Initializable {

    @FXML
    private TableView<Clientes> tableView;
    @FXML
    private TableColumn<Clientes, String> nombre, apellido;
    @FXML
    private TableColumn<Clientes, String> descuento;
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
         acciones.setCellFactory(param -> new TableCell<>() {
    @Override
    protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            Clientes inventUsuarios = getTableView().getItems().get(getIndex());


            Button editButton = new Button();
            ImageView iconoEditar = new ImageView(new Image(getClass().getResourceAsStream("/img/edit.png")));
            iconoEditar.setFitWidth(23); // Establecer el ancho deseado
            iconoEditar.setFitHeight(23); // Establecer el alto dese
            editButton.setGraphic(iconoEditar);
            editButton.getStyleClass().add("btn_mod");
            editButton.setOnAction(event -> {
                getTableView().getSelectionModel().select(getIndex());
                Clientes cliente=tableView.getSelectionModel().getSelectedItem();
                abrirMod(cliente.getId(), cliente.getNombre(), cliente.getApellido(), cliente.getDescuento());
                
            });

            Button deleteButton = new Button();
            ImageView iconoEliminar = new ImageView(new Image(getClass().getResourceAsStream("/img/elim.png")));
            iconoEliminar.setFitWidth(23); // Establecer el ancho deseado
            iconoEliminar.setFitHeight(23); // Establecer el alto dese
            deleteButton.setGraphic(iconoEliminar);
            deleteButton.getStyleClass().add("btn_eli");
            deleteButton.setOnAction(event -> {
                getTableView().getSelectionModel().select(getIndex());
                Clientes clienteSeleccionado = getTableView().getItems().get(getIndex());
                eliminarCliente(clienteSeleccionado);

                
            });
            setGraphic(new HBox(10, editButton, deleteButton) {{
                setAlignment(Pos.CENTER);
                setSpacing(10);
            }});
        }
    }
});

    }

    public void eliminarCliente(Clientes cliente) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            
            cliente.setActivo("N");
            session.update(cliente);
            
            tx.commit();
            
            mostrarTabla(); // Actualizar la tabla después de eliminar
            
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Cliente Eliminado");
            alert.setHeaderText(null);
            alert.setContentText("El cliente ha sido eliminado exitosamente.");
            alert.showAndWait();
        } catch (HibernateException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Ocurrió un error al eliminar el cliente: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }
   public void agregarCliente(){
    System.out.println("Iniciando agregarxd");

    if (txtApellido.getText().isEmpty()==false && txtNombre.getText().isEmpty()==false && txtDescuento.getText().isEmpty()==false) {
        String apellido="";
        String nombre="";
        String numero;
        try {
            apellido = this.txtApellido.getText().toString();
            nombre= this.txtNombre.getText().toString();
            numero=this.txtDescuento.getText().toString();            
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                Clientes nuevoCliente = new Clientes();
                nuevoCliente.setNombre(nombre);
                nuevoCliente.setApellido(apellido);
                nuevoCliente.setDescuento(numero);
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

public void abrirMod(int ID, String nombre, String apellido, String descuento) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/mod_cli.fxml"));
        Pane nuevoContenido = loader.load();
        modClientesController clienteController = loader.getController();
        rootPane.getChildren().setAll(nuevoContenido);
        clienteController.getData(ID, nombre, apellido, descuento);
    } catch (IOException e) {
        e.printStackTrace();
    }
}


public void archivoclientes() {
    try {
        // Cargar el archivo FXML con el nuevo contenido
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FXMLClientesEliminados.fxml"));
        Pane nuevoContenido1 = loader.load();
        
        // Obtener el controlador del nuevo contenido
        Object controller = loader.getController();

        if (controller instanceof FXMLClienteEliminados) {
            FXMLClienteEliminados clienteEliminadosController = (FXMLClienteEliminados) controller;
            // Aquí puedes usar clienteEliminadosController si necesitas realizar alguna acción específica
        } else {
            System.err.println("Error: El controlador no es una instancia de FFXMLProductosEliminados");
            // Opcional: Lanza una excepción si es un caso crítico
            throw new IllegalStateException("El controlador no es una instancia deFXMLProductosEliminados");
        }

        rootPane.getChildren().setAll(nuevoContenido1);
    } catch (IOException e) {
        e.printStackTrace();
    }
}
}
