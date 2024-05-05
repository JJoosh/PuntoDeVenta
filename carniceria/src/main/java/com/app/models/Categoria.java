package com.app.models;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

@Entity
@Table(name = "Categoria")
public class Categoria {
    private static final Session HibernateUtil = null;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_categoria")
    private Long id;

    @Column(name = "Nombre_categoria")
    private String nombreCategoria;

    @OneToMany(mappedBy = "categoria")
    private List<Productos> productos;

    public Categoria() {
    }

    public Categoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    // Getters y setters

    public Categoria obtenerCategoriaPorId(Long idCategoria) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            // Consulta para obtener la categoría por su ID
            Query<Categoria> query = session.createQuery("FROM Categoria WHERE id = :id", Categoria.class);
            query.setParameter("id", idCategoria);
            Categoria categoria = query.uniqueResult();
            return categoria;
        } finally {
            session.close();
        }
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public List<Productos> getProductos() {
        return productos;
    }

    public void setProductos(List<Productos> productos) {
        this.productos = productos;
    }

    public long getIDconName(String name) {
        long id = 0;
    
        Configuration configuration = new Configuration().configure();
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.openSession();
    
        try {
            // Modifica la consulta para seleccionar el ID de la categoría con el nombre dado
            id = session.createQuery("SELECT c.id FROM Categoria c WHERE c.nombreCategoria = :nombre", Long.class)
                       .setParameter("nombre", name)
                       .uniqueResult(); // Obtiene el ID único de la categoría con el nombre especificado
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
            sessionFactory.close();
        }
    
        return id;
    }
}