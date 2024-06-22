package com.app.controllers.Configuracion;

import java.util.List;

import com.app.models.Usuarios;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.persistence.TypedQuery;

public class FXMLUsuariosEliminados {

    @FXML
    private TableView<Usuarios> usuariosTableView;

    @FXML
    private TableColumn<Usuarios, Long> idColumn;

    @FXML
    private TableColumn<Usuarios, String> nombreColumn;

    @FXML
    private TableColumn<Usuarios, String> contrasenaColumn;

    @FXML
    private TableColumn<Usuarios, String> rolColumn;

    @FXML
    private TableColumn<Usuarios, String> activoColumn;

    @FXML
    private TextField buscarTextField;

    @FXML
    private Button habilitarUsuarioButton;

    private ObservableList<Usuarios> usuariosList;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        contrasenaColumn.setCellValueFactory(new PropertyValueFactory<>("contrasena"));
        rolColumn.setCellValueFactory(new PropertyValueFactory<>("rol"));
        activoColumn.setCellValueFactory(new PropertyValueFactory<>("activo"));

        // Cargar datos desde la base de datos inicialmente
        cargarDatos();

        // Configurar el filtro para la tabla
        configurarFiltro();

        // Configurar el botón para habilitar/deshabilitar usuario
        habilitarUsuarioButton.setOnAction(event -> cambiarEstadoUsuario());
    }

    private void cargarDatos() {
        List<Usuarios> usuarios = obtenerUsuariosInactivos();
        usuariosList = FXCollections.observableArrayList(usuarios);
        usuariosTableView.setItems(usuariosList);
    }

    public List<Usuarios> obtenerUsuariosInactivos() {
        Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.openSession();

        TypedQuery<Usuarios> query = session.createQuery("SELECT u FROM Usuarios u WHERE u.activo = 'N'", Usuarios.class);
        List<Usuarios> usuariosInactivos = query.getResultList();

        session.close();
        sessionFactory.close();

        return usuariosInactivos;
    }

    private void configurarFiltro() {
        buscarTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                // Mostrar todos los usuarios cuando el campo de texto está vacío
                mostrarTablaUsuarios(usuariosList);
            } else {
                // Filtrar usuarios según el valor del campo de texto
                ObservableList<Usuarios> usuariosFiltrados = FXCollections.observableArrayList();

                for (Usuarios usuario : usuariosList) {
                    if (String.valueOf(usuario.getId()).startsWith(newValue) || usuario.getNombreUsuario().toLowerCase().startsWith(newValue.toLowerCase())) {
                        usuariosFiltrados.add(usuario);
                    }
                }

                usuariosTableView.setItems(usuariosFiltrados);
            }
        });
    }

    private void mostrarTablaUsuarios(List<Usuarios> listaUsuarios) {
        usuariosList = FXCollections.observableArrayList(listaUsuarios);
        usuariosTableView.setItems(usuariosList);
    }

    @FXML
    private void cambiarEstadoUsuario() {
        Usuarios usuarioSeleccionado = usuariosTableView.getSelectionModel().getSelectedItem();
        if (usuarioSeleccionado != null) {
            String nuevoEstado = usuarioSeleccionado.getActivo().equals("N") ? "A" : "N";
            usuarioSeleccionado.setActivo(nuevoEstado);

            // Guardar el cambio en la base de datos usando Hibernate
            guardarCambiosEnBaseDeDatos(usuarioSeleccionado);

            // Actualizar la tabla
            usuariosTableView.refresh();
            cargarDatos();
        }
    }

    private void guardarCambiosEnBaseDeDatos(Usuarios usuario) {
        Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        // Actualizar el estado del usuario en la base de datos
        session.update(usuario);

        session.getTransaction().commit();
        session.close();
        sessionFactory.close();
    }
}
