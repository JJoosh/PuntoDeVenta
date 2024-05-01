package com.app.models;

import javax.persistence.*;
@Entity
@Table(name = "Productos")
public class Productos{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Nombre")
    private String nombre;

    @Column(name = "CategoriaID")
    private int CategoriaID;

    @Column(name = "Cantidad")
    private float cantidad;

    @Column(name="Precio")
    private float Precio;
    public Productos() {
    }

    public Productos(long id,String nombre, int CategoriaID, float Cantidad, float Precio) {
        this.id=id;
        this.nombre = nombre;
        this.CategoriaID= CategoriaID;
        this.cantidad= Cantidad;
        this.Precio=Precio;

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

    public int getCategoria() {
        return CategoriaID;
    }

    public void setCategoria(int categoria) {
        this.CategoriaID = categoria;
    }

    public float getCantidad(){
        return cantidad;
    }

    public void setCantidad(float cantidad){
        this.cantidad=cantidad;
    }

    public float getPrecio(){
        return Precio;
    }

    public void setPrecio(float Precio){
        this.Precio=Precio;
    }

    
}