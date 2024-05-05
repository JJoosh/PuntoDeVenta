package com.app.models;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Productos")
public class Productos {



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

    @Column(name = "Cantidad")
    private BigDecimal cantidad;

    @Column(name = "Precio")
    private BigDecimal precio;


    public Productos() {
    }


    public Productos(String nombre, BigDecimal costo, Categoria categoria, BigDecimal cantidad, BigDecimal precio) {
        this.nombre = nombre;
        this.costo=costo;

    }
    public Productos(String nombre, Categoria categoria, BigDecimal cantidad, BigDecimal precio) {
        this.nombre = nombre;

        this.categoria = categoria;
        this.cantidad = cantidad;
        this.precio = precio;
    }

    // Getters y setters

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
}
}
