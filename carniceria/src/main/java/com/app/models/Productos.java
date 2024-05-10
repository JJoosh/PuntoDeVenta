package com.app.models;
import java.math.BigDecimal;
import java.util.List;

import javax.persistence.*;
import java.math.BigDecimal;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

import javafx.scene.control.Alert;




@Entity
@Table(name = "Productos")
public class Productos {
    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "Nombre")
    private String nombre;


    @Column(name = "Costo")
    private BigDecimal costo;


    @ManyToOne
    @JoinColumn(name = "Categoria_ID")
    private Categoria categoria;

    @OneToMany(mappedBy = "id_producto")
    private List<Movimientos> movimientos;

    @Column(name = "Cantidad")
    private BigDecimal cantidad;

    @Column(name = "Precio")
    private BigDecimal precio;

    @Column(name = "ProduBajos_Inventario")
    private BigDecimal productosBajos_inventario;

    @Column(name = "Peso_Caja")
    private BigDecimal peso_caja;

    public Productos(){
        
    }
    
    public Productos(String nombre, Categoria categoria, BigDecimal cantidad, BigDecimal precio) {
        this.nombre = nombre;

        this.categoria = categoria;
        this.cantidad = cantidad;
        this.precio = precio;
    }
    public Productos(Productos producto) {
        this.id = producto.id;
        this.nombre = producto.nombre;
        this.categoria = producto.categoria;
        this.cantidad = producto.cantidad;
        this.precio = producto.precio;
    }

    public Productos(String nombre, BigDecimal costo, Categoria categoria, BigDecimal cantidad, BigDecimal precio) {
        this.nombre = nombre;
        this.costo=costo;

    }
    public Productos(long id, String nombre, BigDecimal costo, Categoria categoria, BigDecimal cantidad, BigDecimal precio,  BigDecimal productosBajos_inventario) {
        this.id=id;
        this.nombre = nombre;
        this.costo=costo;
        this.productosBajos_inventario=productosBajos_inventario;
        this.categoria = categoria;
        this.cantidad = cantidad;
        this.precio = precio;
    }

    // Getters y setters

    public BigDecimal getPesoCaja(){
        return peso_caja;
    }

    public void setPesoCaja(BigDecimal peso_caja){
        this.peso_caja=peso_caja;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    
    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecio() {
        return precio;
    }


    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public BigDecimal getCosto(){
        return costo;
    }

    public void setCosto(BigDecimal Costo){
        this.costo=Costo;
    }

    public BigDecimal getProductosBajos_inventario(){
        return productosBajos_inventario;
    }
    public void setProductosBajos_inventario(BigDecimal productosBajos){
        this.productosBajos_inventario=productosBajos;
    }

    public void modificarProducto(Long id, String nombre, BigDecimal costo, Long id_cat, BigDecimal cantidad, BigDecimal precio, BigDecimal pesoCaja ) {
       
    
        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");
        configuration.addAnnotatedClass(Productos.class);
    
       
        SessionFactory sessionFactory = configuration.buildSessionFactory();
    
        EntityManager entityManager = sessionFactory.createEntityManager();
        
        EntityTransaction transaction = null;
        try {
            transaction = entityManager.getTransaction();
            transaction.begin();
            
            Productos producto = entityManager.find(Productos.class, id);
            if (producto != null) {
                producto.setNombre(nombre);
                producto.setCosto(costo);
                Categoria categoria = entityManager.find(Categoria.class, id_cat);
                producto.setCategoria(categoria);
                producto.setCantidad(cantidad);
                producto.setPrecio(precio);
                producto.setPesoCaja(pesoCaja);
                entityManager.merge(producto);
                
                transaction.commit();
            } 
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                System.out.println(e);
            }
            e.printStackTrace();
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
            if (sessionFactory != null) {
                sessionFactory.close();
            }
        }
    }

    public String getNombreCategoria() {
        if (categoria != null) {
            return categoria.getNombreCategoria();
        }
        return "";
    }

    public void actualizarCantidad(Long id, BigDecimal nuevaCantidad) {
        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");
        configuration.addAnnotatedClass(Productos.class);
    
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        EntityManager entityManager = sessionFactory.createEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = entityManager.getTransaction();
            transaction.begin();
    
            Productos producto = entityManager.find(Productos.class, id);
            if (producto != null) {
                producto.setCantidad(nuevaCantidad);
                entityManager.merge(producto);
                transaction.commit();
            }
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
            if (sessionFactory != null) {
                sessionFactory.close();
            }
        }
    }
    
}