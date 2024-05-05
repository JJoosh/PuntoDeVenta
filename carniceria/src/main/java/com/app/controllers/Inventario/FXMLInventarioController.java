package com.app.controllers.Inventario;

import com.app.models.Categoria;
import com.app.models.Productos;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import javafx.scene.Parent;

public class FXMLInventarioController implements Initializable {

    @FXML
    private TableView<Productos> tableView;
    @FXML
    private TableColumn<Productos, Long> codigoColumn;
    @FXML
    private TableColumn<Productos, String> descripcionColumn;
    @FXML
    private TableColumn<Productos, BigDecimal> costoColumn;
    @FXML
    private TableColumn<Productos, BigDecimal> precioVentaColumn;
    @FXML
    private TableColumn<Productos, BigDecimal> existenciaColumn;
    @FXML
    private TableColumn<Productos, BigDecimal> inventarioMinimoColumn;
    @FXML
    private TextField idP;
    @FXML
    private ComboBox<String> categorias;

    private ObservableList<Productos> productosData;
    private ObservableList<Productos> productosOriginalData;

    //LOGICA PARA MANEJAR FILTRADO POR ID
    public void buscarforID() {
        idP.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // Crear una nueva lista para almacenar los elementos filtrados
                ObservableList<Productos> filteredItems = FXCollections.observableArrayList();
                
                // Si el texto del TextField está vacío, restaurar la lista original
                if (newValue == null || newValue.isEmpty()) {
                    productosData.clear();
                    productosData.addAll(productosOriginalData);
                    tableView.setItems(productosData);
                } else {
                    // Obtener la categoría seleccionada en el ComboBox
                    String selectedCategory = categorias.getValue();
                    
                    // Iterar sobre la lista original y agregar los elementos que coincidan con el filtro y la categoría seleccionada
                    for (Productos producto : productosOriginalData) {
                        String idString = producto.getId().toString();
                        
                        // Verificar si la categoría no es nula antes de acceder a su nombre
                        String category = producto.getCategoria() != null ? producto.getCategoria().getNombreCategoria() : "";
                        
                        if ((idString.contains(newValue) || idString.startsWith(newValue))
                                && (selectedCategory == null || selectedCategory.equals("Todos") || category.equals(selectedCategory))) {
                            filteredItems.add(producto);
                            categorias.setValue("Todos");
                        }
                    }
                    
                    tableView.setItems(filteredItems);
                }
            }
        });
    }
    
    //LOGICA PARA MANEJAR LOS EVENTOS DE LOS BOTONES
    @FXML
    private void evento(ActionEvent e) { //TODAVIA ESTE EVENTO
        System.out.println("Seleccionaste ventas....");
    }


    @FXML
    private void ModProd() {
        Productos productoSeleccionado = tableView.getSelectionModel().getSelectedItem();
        if (productoSeleccionado != null) {
            
            long id = productoSeleccionado.getId();
            String nombre=productoSeleccionado.getNombre();
            BigDecimal precio=productoSeleccionado.getPrecio();
            BigDecimal Cantidad=productoSeleccionado.getCantidad();
            BigDecimal Costo=productoSeleccionado.getCosto();
            Categoria categoria=productoSeleccionado.getCategoria();
            
            
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FXMLModificarProducto.fxml"));
                Parent root = loader.load();
                
                FXML_ModProducto modProductoController = loader.getController();
                modProductoController.setDatos(nombre, Cantidad, id, precio, categoria.getNombreCategoria());
                Scene scene = new Scene(root);
                
                Stage stage = new Stage();
                stage.setScene(scene);
                
                // Mostrar el escenario
                stage.show();
    
                
            } catch (IOException ex) {
                ex.printStackTrace();
            }
    
        } else {
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("No se ha seleccionado ningún producto.");
            alert.showAndWait();
        }
    }



    @FXML
    private void addInventario(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FXMLProductoNew.fxml"));
            Parent root = loader.load();
            
            FXML_NewProducto newProductoController = loader.getController();
            newProductoController.setInventarioController(this);
            
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void cargarCategorias(ComboBox<String> categorias, int opcion) {
        Configuration configuration = new Configuration().configure();
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.openSession();
    
        try {
            Query<String> query = session.createQuery("SELECT c.nombreCategoria FROM Categoria c", String.class);
            
            ObservableList<String> listaNombresCategorias = FXCollections.observableArrayList();
            categorias.setPromptText("Categorias");
           if (opcion==1)listaNombresCategorias.add("Todos");
            listaNombresCategorias.addAll(query.list());
            ObservableList<String> observableListaNombresCategorias = FXCollections.observableArrayList(listaNombresCategorias);
            categorias.setItems(observableListaNombresCategorias);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
            sessionFactory.close();
        }
    }
    
    //LOGICA PARA FILTRAR EN LA TABLA
    public  void filtarCategorias(){
        Categoria id=new Categoria();
        categorias.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String name) {
                if (name != null) {
                    long idCategoria=0;
                    
                    if(name.equals("Todos")){
                        agregaraTabla();
                    } else {
                        idCategoria=id.getIDconName(name);
                        actualizarTablaF(obtenerProductosF(idCategoria));
                    }
                }
            }
        });
    }

    public void actualizarTablaF( List<Productos> productos) {
        productosData.clear();
        productosData.addAll(productos);
    }

    public List<Productos> obtenerProductosF(long id) {
        Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = emf.createEntityManager();
    
        TypedQuery<Productos> query = entityManager.createQuery("SELECT p FROM Productos p WHERE p.categoria.id = :id", Productos.class);
        query.setParameter("id", id);
    
        List<Productos> productos = query.getResultList();
    
        entityManager.close();
        emf.close();
    
        return productos;
    }
    
    //LOGICA QUE SE CARGARA AL INICIAR LA PAGINA DE INVENTARIO 
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        agregaraTabla();
        cargarCategorias(this.categorias, 1);
        filtarCategorias();
        buscarforID();

        

    }

    public void agregaraTabla(){
        // Configurar las celdas de las columnas
        codigoColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        descripcionColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        costoColumn.setCellValueFactory(new PropertyValueFactory<>("costo"));
        precioVentaColumn.setCellValueFactory(new PropertyValueFactory<>("precio"));
        existenciaColumn.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        inventarioMinimoColumn.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        // Obtener los productos de la base de datos
        List<Productos> productos = obtenerProductos();

        // Crear una lista observable a partir de la lista de productos
        productosOriginalData = FXCollections.observableArrayList(productos);
        productosData = FXCollections.observableArrayList(productos);

        tableView.setItems(productosData);
    }

    private List<Productos> obtenerProductos() {
        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");

        configuration.addAnnotatedClass(Productos.class);
      
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = emf.createEntityManager();

        TypedQuery<Productos> query = entityManager.createQuery("SELECT p FROM Productos p", Productos.class);
        List<Productos> productos = query.getResultList();

        entityManager.close();
        emf.close();

        return productos;
    }

    public void actualizarTabla() {
        List<Productos> productosActualizados = obtenerProductos();
        productosData.clear();
        productosData.addAll(productosActualizados);
    }
}