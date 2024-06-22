package com.app.controllers.Inventario;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.app.models.Movimientos;
import com.app.models.Productos;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddCantidadController implements Initializable {
    
    @FXML
    private Label id_nameProducto;

    @FXML
    private TextField cantidad;


    

    
    private BigDecimal cantidadactual=new BigDecimal(0);

    private long id=0;

    

    private Stage stage;
    private FXMLInventarioController inventarioController;
     @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        
    } 
    public void setInventarioController(FXMLInventarioController inventarioController) {
        this.inventarioController = inventarioController;
    }
    public void setStage (Stage stage){
        this.stage=stage;
    }
    public void setDatos(String nombre, BigDecimal cantidadactual, long id){
        id_nameProducto.setText("Nombre:"+ nombre);
        this.cantidadactual=cantidadactual;
        this.id=id;
        
    }

    @FXML
    public void ingresar() {
        if (cantidad != null) {
            try {
               BigDecimal cajas= BigDecimal.valueOf( Double.parseDouble(cantidad.getText()));
              
                
                
                // Obtener el producto existente de la base de datos
                Productos producto = obtenerProductoPorId(this.id);
                
                if (producto != null) {
                    producto.actualizarCantidad(id, cajas.add(cantidadactual));
                    
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Correcto");
                    alert.setHeaderText(null);
                    alert.setContentText("Se actualizó correctamente");
                    alert.showAndWait();
                    
                    inventarioController.actualizarTabla();
                    
                    Movimientos movimiento = new Movimientos();
                    movimiento.setIdProducto(producto);
                    movimiento.setTipoMovimiento("Entrada");
                    movimiento.setCantidad(cajas);
                    LocalDateTime fechaHoraActual = LocalDateTime.now();
                    movimiento.setFecha(fechaHoraActual);
                    guardarMovimiento(movimiento);
                    
                    stage.close();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("No se encontró el producto en la base de datos");
                    alert.showAndWait();
                }
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Ingrese una cantidad válida");
                alert.showAndWait();
                System.out.println(e);
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Por favor ingrese una cantidad");
            alert.showAndWait();
        }
    }
    
    private Productos obtenerProductoPorId(long id) {
        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");
        configuration.addAnnotatedClass(Productos.class);
        
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = emf.createEntityManager();
        
        Productos producto = entityManager.find(Productos.class, id);
        
        entityManager.close();
        emf.close();
        
        return producto;
    }
    
    private void guardarMovimiento(Movimientos movimiento) {
        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");
        configuration.addAnnotatedClass(Movimientos.class);
        
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManagerFactory emf = sessionFactory.unwrap(EntityManagerFactory.class);
        EntityManager entityManager = emf.createEntityManager();
        
        entityManager.getTransaction().begin();
        entityManager.persist(movimiento);
        entityManager.getTransaction().commit();
        
        entityManager.close();
        emf.close();
    }
 }
