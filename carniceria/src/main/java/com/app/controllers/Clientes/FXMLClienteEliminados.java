package com.app.controllers.Clientes;

import javax.persistence.TypedQuery;

import com.app.models.Clientes;
import com.app.models.Clientes;
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
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.cell.PropertyValueFactory;

public class FXMLClienteEliminados {
    @FXML
    private TableView<Clientes> clientesTableView;

    @FXML
    private TableColumn<Clientes, Integer> idColumn;

    @FXML
    private TableColumn<Clientes, String> nombreColumn;

    @FXML
    private TableColumn<Clientes, String> apellidoColumn;

    @FXML
    private TableColumn<Clientes, String> telefonoColumn;

    @FXML
    private TableColumn<Clientes, String> estadoColumn;
    

    @FXML
    private TextField buscarTextField;

    @FXML
    private Button habilitarClienteButton;

    private ObservableList<Clientes> clientesList;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        apellidoColumn.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        telefonoColumn.setCellValueFactory(new PropertyValueFactory<>("descuento"));
        estadoColumn.setCellValueFactory(new PropertyValueFactory<>("activo"));

        // Cargar datos desde la base de datos inicialmente
        cargarDatos();

        // Configurar el filtro para la tabla
        configurarFiltro();

        // Configurar el botón para habilitar/deshabilitar cliente
        habilitarClienteButton.setOnAction(event -> cambiarEstadoCliente());
    }

    private void cargarDatos() {
        List<Clientes> clientes = obtenerClientesInactivos();
        clientesList = FXCollections.observableArrayList(clientes);
        clientesTableView.setItems(clientesList);
    }

    public List<Clientes> obtenerClientesInactivos() {
        Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.openSession();

        TypedQuery<Clientes> query = session.createQuery("SELECT c FROM Clientes c WHERE c.activo = 'N'", Clientes.class);
        List<Clientes> clientesInactivos = query.getResultList();

        session.close();
        sessionFactory.close();

        return clientesInactivos;
    }

    private void configurarFiltro() {
        buscarTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                // Mostrar todos los clientes cuando el campo de texto está vacío
                mostrarTablaClientes(clientesList);
            } else {
                // Filtrar clientes según el valor del campo de texto
                ObservableList<Clientes> clientesFiltrados = FXCollections.observableArrayList();

                for (Clientes cliente : clientesList) {
                    if (String.valueOf(cliente.getId()).startsWith(newValue) || cliente.getNombre().toLowerCase().startsWith(newValue.toLowerCase())) {
                        clientesFiltrados.add(cliente);
                    }
                }

                clientesTableView.setItems(clientesFiltrados);
            }
        });
    }

    private void mostrarTablaClientes(List<Clientes> listaClientes) {
        clientesList = FXCollections.observableArrayList(listaClientes);
        clientesTableView.setItems(clientesList);
    }

    @FXML
    private void cambiarEstadoCliente() {
        Clientes clienteSeleccionado = clientesTableView.getSelectionModel().getSelectedItem();
        if (clienteSeleccionado != null) {
            String nuevoEstado = clienteSeleccionado.getActivo().equals("N") ? "A" : "N";
            clienteSeleccionado.setActivo(nuevoEstado);

            // Guardar el cambio en la base de datos usando Hibernate
            guardarCambiosEnBaseDeDatos(clienteSeleccionado);

            // Actualizar la tabla
            clientesTableView.refresh();

            cargarDatos();
        }
    }

    private void guardarCambiosEnBaseDeDatos(Clientes cliente) {
        Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        // Actualizar el estado del cliente en la base de datos
        session.update(cliente);

        session.getTransaction().commit();
        session.close();
        sessionFactory.close();
    }
}
