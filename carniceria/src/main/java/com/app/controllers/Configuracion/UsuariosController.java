package com.app.controllers.Configuracion;
import java.io.IOException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.app.models.Clientes;
import com.app.models.Productos;
import com.app.models.Usuarios;
import javafx.scene.control.TableCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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


            Button editButton = new Button();
            ImageView iconoEditar = new ImageView(new Image(getClass().getResourceAsStream("/img/edit.png")));
            iconoEditar.setFitWidth(23); // Establecer el ancho deseado
            iconoEditar.setFitHeight(23); // Establecer el alto dese
            editButton.setGraphic(iconoEditar);
            editButton.getStyleClass().add("btn_mod");
            editButton.setOnAction(event -> {
                getTableView().getSelectionModel().select(getIndex());
                abrirModUser();
                
            });

            Button deleteButton = new Button();
            ImageView iconoEliminar = new ImageView(new Image(getClass().getResourceAsStream("/img/elim.png")));
            iconoEliminar.setFitWidth(23); // Establecer el ancho deseado
            iconoEliminar.setFitHeight(23); // Establecer el alto dese
            deleteButton.setGraphic(iconoEliminar);
            deleteButton.getStyleClass().add("btn_eli");
            deleteButton.setOnAction(event -> {
                getTableView().getSelectionModel().select(getIndex());
                DeleteUser();
            });
            setGraphic(new HBox(10, editButton, deleteButton) {{
                setAlignment(Pos.CENTER);
                setSpacing(10);
            }});
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
    // Configuración de Hibernate
    Configuration configuration = new Configuration();
    configuration.configure("hibernate.cfg.xml");
    configuration.addAnnotatedClass(Usuarios.class);

    SessionFactory sessionFactory = configuration.buildSessionFactory();
    Session session = sessionFactory.openSession();

    try {
        session.beginTransaction();

        // Consulta HQL para obtener solo los usuarios activos
        ObservableList<Usuarios> usuariosList = FXCollections.observableArrayList(
            session.createQuery("from Usuarios where activo = 'A'", Usuarios.class).list()
        );

        tableUsuarios.setItems(usuariosList);
        clmName.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        clmRol.setCellValueFactory(new PropertyValueFactory<>("rol"));

        session.getTransaction().commit();
    } catch (Exception e) {
        e.printStackTrace();

        session.getTransaction().rollback();

        // Mostrar mensaje de error en caso de fallo
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
public void archivousuarios() {
    try {
        // Cargar el archivo FXML con el nuevo contenido
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FXMLUsuariosEliminados.fxml"));
        Pane nuevoContenido1 = loader.load();

        // Obtener el controlador del nuevo contenido
        Object controller = loader.getController();

        if (controller instanceof FXMLUsuariosEliminados) {
            FXMLUsuariosEliminados clienteEliminadosController = (FXMLUsuariosEliminados) controller;
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

@FXML
private void DeleteUser() {
    Usuarios selectedUser = tableUsuarios.getSelectionModel().getSelectedItem();
    if (selectedUser != null) {
        try {
            // Configuración de Hibernate
            Configuration configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");
            configuration.addAnnotatedClass(Usuarios.class);
            SessionFactory sessionFactory = configuration.buildSessionFactory();
            Session session = sessionFactory.openSession();
            session.beginTransaction();

            // Actualización del usuario seleccionado a inactivo (N)
            selectedUser.setActivo("N");
            session.update(selectedUser);

            session.getTransaction().commit();

            // Mostrar mensaje de éxito
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setHeaderText(null);
            alert.setContentText("Usuario eliminado correctamente");
            alert.showAndWait();

            cargarTabla();

        } catch (Exception e) {
            e.printStackTrace();

            // En caso de error, mostrar un mensaje de error
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Error al eliminar el usuario");
            alert.showAndWait();
        }
    } else {
        // Si no se ha seleccionado ningún usuario, mostrar un mensaje de advertencia
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Advertencia");
        alert.setHeaderText(null);
        alert.setContentText("Por favor seleccione un usuario de la tabla.");
        alert.showAndWait();
    }
}
}