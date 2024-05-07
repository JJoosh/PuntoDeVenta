package com.app.models;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "Productos")
public class Productos {
    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "Nombre")
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "Categoria_ID")
    private Categoria categoria;

    @Column(name = "Cantidad")
    private BigDecimal cantidad;

    @Column(name = "Precio")
    private BigDecimal precio;

    public Productos() {
    }

    public Productos(String nombre, Categoria categoria, BigDecimal cantidad, BigDecimal precio) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.cantidad = cantidad;
        this.precio = precio;
    }

    // Constructor de copia
    public Productos(Productos producto) {
        this.id = producto.id;
        this.nombre = producto.nombre;
        this.categoria = producto.categoria;
        this.cantidad = producto.cantidad;
        this.precio = producto.precio;
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

    public BigDecimal getInventario() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getInventario'");
    }
}