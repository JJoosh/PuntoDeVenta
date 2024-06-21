package com.app.controllers.Configuracion;
import java.io.IOException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.app.models.Productos;
import com.app.models.Usuarios;
import javafx.scene.control.TableCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


public class UsuariosController {
    @FXML
    private Pane rootPane;
    @FXML
    private TextField lblNombre;
    @FXML
    private TextField lblPassword;
    @FXML
    private ComboBox<String> boxRoles;
    @FXML
    private TableView<Usuarios> tableUsuarios;
    @FXML
    private TableColumn<Usuarios, String> clmName;
    @FXML
    private TableColumn<Usuarios, String> clmRol;
    @FXML
    private Button borrararticulo;

    private Stage stage;

    @FXML TableColumn<Usuarios, Void> ColumAcciones;
    @FXML
    private void initialize() {
        ObservableList<String> listRoles = FXCollections.observableArrayList(
    "administrador",
    "Vendedor"
    );
       boxRoles.setItems(listRoles);
        
       cargarTabla();

       ColumAcciones.setCellFactory(param -> new TableCell<>() {
        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                Usuarios usuarios = getTableView().getItems().get(getIndex());

                Button deleteButton = new Button("Eliminar");
                deleteButton.getStyleClass().add("btn_eli");
                deleteButton.setOnAction(event -> {
                    System.out.println("Eliminar: " + usuarios.getNombreUsuario());
                    // Aquí va la lógica para eliminar el usuariProductos
                });

                Button editButton = new Button("Modificar");
                editButton.getStyleClass().add("btn_mod");
                editButton.setOnAction(event -> {
                    System.out.println("Modificar: " + usuarios.getNombreUsuario());
                    // Aquí va la lógica para abrir la ventana de modificación del producto
                });

                setGraphic(new HBox(5, editButton, deleteButton)); 
            }
        }
    });
}
    

    @FXML
private void agregar() {
    if (!lblNombre.getText().isEmpty() && !lblPassword.getText().isEmpty() && boxRoles.getValue().toString().isEmpty()==false) {
        String nombreUsuario = lblNombre.getText();
        String contrasena = lblPassword.getText();
        
        
        Usuarios nuevoUsuario = new Usuarios();
        nuevoUsuario.setNombreUsuario(nombreUsuario);
        nuevoUsuario.setContrasena(contrasena);
        nuevoUsuario.setRol(boxRoles.getValue().toString()); 

        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");
        configuration.addAnnotatedClass(Usuarios.class);
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.openSession();
        
        try {
           
            session.beginTransaction();
            
           
            session.save(nuevoUsuario);
            
         
            session.getTransaction().commit();
            
         
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setHeaderText(null);
            alert.setContentText("Usuario agregado correctamente");
            alert.showAndWait();
            
            lblNombre.clear();
            lblPassword.clear();
            cargarTabla();
        } catch (Exception e) {
            e.printStackTrace();
            
          
            session.getTransaction().rollback();
       
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Error al agregar el usuario");
            alert.showAndWait();
        } finally {
        
            session.close();
            sessionFactory.close();
        }
    } else {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Faltó un campo por escribir");
        alert.showAndWait();
    }
}

public void cargarTabla() {
    
  
    Configuration configuration = new Configuration();
    configuration.configure("hibernate.cfg.xml");
    configuration.addAnnotatedClass(Usuarios.class);

    SessionFactory sessionFactory = configuration.buildSessionFactory();
    Session session = sessionFactory.openSession();

    try {
     
        session.beginTransaction();

        ObservableList<Usuarios> usuariosList = FXCollections.observableArrayList(
            session.createQuery("from Usuarios", Usuarios.class).list()
        );

        tableUsuarios.setItems(usuariosList);
        clmName.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        clmRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
   
        session.getTransaction().commit();
    } catch (Exception e) {
        e.printStackTrace();

      
        session.getTransaction().rollback();

        
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Error al cargar la tabla de usuarios");
        alert.showAndWait();
    } finally {
        session.close();
        sessionFactory.close();
    }
}

public void abrirModUser(){
    Usuarios selectedUser = tableUsuarios.getSelectionModel().getSelectedItem();
    if (selectedUser != null) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/modUser.fxml"));
            Parent root = loader.load();
            
            modUsser cantidadController = loader.getController();
            cantidadController.setData(selectedUser);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("No se pudo cargar la vista de modUser");
            alert.showAndWait();
        }
    } else {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertencia");
        alert.setHeaderText(null);
        alert.setContentText("Por favor seleccione un usuario de la tabla.");
        alert.showAndWait();
    }
}

}