package com.app.models;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import javax.persistence.*;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.app.utils.HibernateUtil;

@Entity
@Table(name = "clientes")
public class Clientes {

    private SimpleIntegerProperty id;
    private SimpleStringProperty nombre;
    private SimpleStringProperty apellido;
    private SimpleIntegerProperty descuento;
    private SimpleStringProperty activo;

    public Clientes() {
        this.id = new SimpleIntegerProperty();
        this.nombre = new SimpleStringProperty();
        this.apellido = new SimpleStringProperty();
        this.descuento = new SimpleIntegerProperty();
        this.activo= new SimpleStringProperty();
        
    }

    public Clientes(int id, String nombre, String apellido, int descuento, String activo) {
        this.id = new SimpleIntegerProperty(id);
        this.nombre = new SimpleStringProperty(nombre);
        this.apellido = new SimpleStringProperty(apellido);
        this.descuento = new SimpleIntegerProperty(descuento);
        this.activo= new SimpleStringProperty(activo);

    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    @Column(name = "nombre")
    public String getNombre() {
        return nombre.get();
    }

    public void setNombre(String nombre) {
        this.nombre.set(nombre);
    }

    public SimpleStringProperty nombreProperty() {
        return nombre;
    }

    @Column(name = "apellido")
    public String getApellido() {
        return apellido.get();
    }

    public void setApellido(String apellido) {
        this.apellido.set(apellido);
    }

    public SimpleStringProperty apellidoProperty() {
        return apellido;
    }

    @Column(name = "descuento")
    public int getDescuento() {
        return descuento.get();
    }

    public void setDescuento(int descuento) {
        this.descuento.set(descuento);
    }

    public SimpleIntegerProperty descuentoProperty() {
        return descuento;
    }


    @Column(name="activo")
    public String getActivo(){
        return activo.get();
    }

    public void setActivo(String newActivo) {
        System.out.println("Cambiando estado activo a: " + newActivo);
        this.activo.set(newActivo);
    
        // Actualizar solo el campo activo en la base de datos
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Query query = session.createQuery("UPDATE Clientes SET activo = :activo WHERE id = :id");
            query.setParameter("activo", newActivo);
            query.setParameter("id", this.getId());
            int result = query.executeUpdate();
            tx.commit();
        } catch (HibernateException e) {
            e.printStackTrace();
        }
    }
    



    

    public void modCliente(int id, String nombre, String apellido, int descuento) {
        System.err.println("Se está cambiando el usuario con el ID: " + id);
    
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
    
            Clientes cliente = session.get(Clientes.class, id);
            if (cliente != null) {
                cliente.setNombre(nombre);
                cliente.setApellido(apellido);
                cliente.setDescuento(descuento);
                session.update(cliente);
                tx.commit();
                System.out.println("Cliente actualizado correctamente.");
            } else {
                System.err.println("Cliente no encontrado.");
            }
        } catch (HibernateException e) {
            e.printStackTrace();
        }
    }
    
}
