package com.app.controllers.Inventario;

import com.app.models.Productos;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

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

    private ObservableList<Productos> productosData;



    //LOGICA PARA MANEJAR LOS EVENTOS DE LOS BOTONES



    @FXML
    private void evento(ActionEvent e) {
        System.out.println("Seleccionaste ventas....");
    }

    @FXML
    private void addInventario(ActionEvent e) {
        try {
           
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FXMLProductoNew.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            
            stage.setScene(new Scene(root));
            
            
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }









    //LOGICA QUE SE CARGARA AL INICIAR LA PAGINA DE INVENTARIO 
    

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        agregaraTabla();
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
}