package com.app.controllers.Inventario;

import com.app.models.Productos;
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
import java.math.BigDecimal;
import java.util.List;

public class FXMLProductosEliminados {

    @FXML
    private TableView<Productos> productosTableView;

    @FXML
    private TableColumn<Productos, Long> codigoColumn;

    @FXML
    private TableColumn<Productos, String> nombreColumn;

    @FXML
    private TableColumn<Productos, BigDecimal> costoColumn;

    @FXML
    private TableColumn<Productos, BigDecimal> precioColumn;

    @FXML
    private TableColumn<Productos, String> categoriaColumn;

    @FXML
    private TableColumn<Productos, String> estadoColumn;

    @FXML
    private TextField buscarTextField;

    @FXML
    private Button cambiarEstadoButton; // Botón para cambiar el estado

    private ObservableList<Productos> productosList;

    @FXML
    public void initialize() {
        codigoColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        costoColumn.setCellValueFactory(new PropertyValueFactory<>("costo"));
        precioColumn.setCellValueFactory(new PropertyValueFactory<>("precio"));
        categoriaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombreCategoria()));
        estadoColumn.setCellValueFactory(new PropertyValueFactory<>("activo"));

        // Cargar datos desde la base de datos inicialmente
        cargarDatos();

        // Configurar el filtro para la tabla
        configurarFiltro();

        // Configurar el botón para cambiar el estado
        cambiarEstadoButton.setOnAction(event -> cambiarEstadoProducto());
    }

    private void cargarDatos() {
        List<Productos> productos = obtenerProductosActivos();
        productosList = FXCollections.observableArrayList(productos);
        productosTableView.setItems(productosList);
    }

    @SuppressWarnings("exports")
    public List<Productos> obtenerProductosActivos() {
        Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.openSession();

        TypedQuery<Productos> query = session.createQuery("SELECT p FROM Productos p WHERE p.activo = 'N'", Productos.class);
        List<Productos> productosActivos = query.getResultList();

        session.close();
        sessionFactory.close();

        return productosActivos;
    }

    private void configurarFiltro() {
        buscarTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                // Mostrar todos los productos cuando el campo de texto está vacío
                mostarTablaProductos(productosList);
            } else {
                // Filtrar productos según el valor del campo de texto
                ObservableList<Productos> productosFiltrados = FXCollections.observableArrayList();
    
                for (Productos producto : productosList) {
                    if (String.valueOf(producto.getId()).startsWith(newValue) || producto.getNombre().toLowerCase().startsWith(newValue.toLowerCase())) {
                        productosFiltrados.add(producto);
                    }
                }
    
                productosTableView.setItems(productosFiltrados);
            }
        });
    }
    

    private void mostarTablaProductos(List<Productos> listaProductos) {
        productosList = FXCollections.observableArrayList(listaProductos);
        productosTableView.setItems(productosList);
    }

    @FXML
    private void cambiarEstadoProducto() {
        Productos productoSeleccionado = productosTableView.getSelectionModel().getSelectedItem();
        if (productoSeleccionado != null) {
            String nuevoEstado = productoSeleccionado.getActivo().equals("N") ? "S" : "N";
            productoSeleccionado.setActivo(nuevoEstado);

            // Guardar el cambio en la base de datos usando Hibernate
            guardarCambiosEnBaseDeDatos(productoSeleccionado);

            // Actualizar la tabla
            productosTableView.refresh();
        }
    }

    private void guardarCambiosEnBaseDeDatos(Productos producto) {
        Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        // Actualizar el estado del producto en la base de datos
        session.update(producto);

        session.getTransaction().commit();
        session.close();
        sessionFactory.close();
    }
}
